package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeEnumConstantsRetval;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeValidRetval;
import com.spldeolin.allison1875.docanalyzer.dto.EndpointDTO;
import com.spldeolin.allison1875.docanalyzer.dto.JsonPropertyDescriptionValueDTO;
import com.spldeolin.allison1875.docanalyzer.service.MarkdownService;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaTraverseUtils;
import com.spldeolin.allison1875.docanalyzer.util.LoadClassUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2023-12-11
 */
@Singleton
@Slf4j
public class MarkdownServiceImpl implements MarkdownService {

    private static final EasyRandom er = initEasyRandom();

    @Inject
    private DocAnalyzerConfig config;

    @Override
    public void flushToMarkdown(List<EndpointDTO> endpoints) {
        Multimap<String/*cat*/, EndpointDTO> endpointMap = ArrayListMultimap.create();
        endpoints.forEach(e -> endpointMap.put(e.getCat(), e));

        for (String cat : endpointMap.keySet()) {
            StringBuilder content = new StringBuilder();
            for (EndpointDTO endpoint : endpointMap.get(cat)) {
                content.append(this.generateEndpointDoc(endpoint));
            }

            File dir = config.getMarkdownDir();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File md = new File(dir.getPath() + File.separator + cat + ".md");
            try {
                FileUtils.writeStringToFile(md, content.toString(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            log.info("create markdown file. file={}", md);
        }
    }

    protected String generateEndpointDoc(EndpointDTO endpoint) {
        StringBuilder result = new StringBuilder(64);
        String title = Iterables.getFirst(endpoint.getDescriptionLines(), null);
        result.append("## ").append(title).append("\n");

        if (endpoint.getDescriptionLines().size() > 1) {
            endpoint.getDescriptionLines().stream().skip(1).forEach(line -> result.append(line).append("\n\n"));
        }

        result.append("### URL\n");
        String urlsText = endpoint.getUrls().stream().map(e -> "`" + e + "`").collect(Collectors.joining(" 或 "));
        result.append(endpoint.getHttpMethod().toUpperCase()).append(urlsText).append("\n");

        result.append("### Request Body（application/json）\n");
        result.append(this.generateReqOrRespDoc(endpoint, true));

        result.append("### Response Body（application/json）\n");
        result.append(this.generateReqOrRespDoc(endpoint, false));

        // 生成cURL
//        result.append(this.generateCurl(endpoint, astForest));

        // 生成返回值示例
//        result.append(this.generateRespSample(endpoint, astForest));

        result.append(this.generateMoreDoc(endpoint));

        // markdown语法的分隔线
        result.append("\n---\n");
        return result.toString();
    }

    private String generateRespSample(EndpointDTO endpoint) {
        if (config.getEnableResponseBodySample() && endpoint.getResponseBodyDescribe() != null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        String fakeRespJson = fakeJsonByDescribe(endpoint.getResponseBodyDescribe());
        if (fakeRespJson != null) {
            result.append("### Response Body的示例\n");
            result.append("```json\n");
            result.append(fakeRespJson);
            result.append("\n```\n");
        }
        return result.toString();
    }

    private String generateCurl(EndpointDTO endpoint) {
        if (config.getEnableCurl() && endpoint.getRequestBodyDescribe() != null) {
            return "";
        }
        StringBuilder result = new StringBuilder(64);
        String fakeReqJson = fakeJsonByDescribe(endpoint.getRequestBodyDescribe());
        if (fakeReqJson != null) {
            result.append("### cURL\n");
            result.append("```shell\n");
            result.append(String.format(
                    "curl --request %s --url 'http://localhost:8080%s' --header " + "'content-type:application/json' "
                            + "--data '", endpoint.getHttpMethod().toUpperCase(), endpoint.getUrls().get(0)));
            result.append(fakeReqJson);
            result.append("'\n```\n");
        }
        return result.toString();
    }

    private String fakeJsonByDescribe(String describe) {
        try {
            Object fakeDTO = er.nextObject(LoadClassUtils.loadClass(describe, AstForestContext.get().getClassLoader()));
            return JsonUtils.toJsonPrettily(fakeDTO);
        } catch (Exception e) {
            log.error("fail to fake json, describe={}", describe, e);
            throw new Allison1875Exception(e);
        }
    }

    protected String generateReqOrRespDoc(EndpointDTO endpoint, boolean isReqBody) {
        JsonSchema rootJsonSchema;
        if (isReqBody) {
            if (endpoint.getRequestBodyJsonSchema() == null) {
                return "无需Request Body\n";
            }
            rootJsonSchema = endpoint.getRequestBodyJsonSchema();
        } else {
            if (endpoint.getResponseBodyJsonSchema() == null) {
                return "没有Response Body\n";
            }
            rootJsonSchema = endpoint.getResponseBodyJsonSchema();
        }

        StringBuilder content = new StringBuilder(64);

        if (!rootJsonSchema.isObjectSchema() && !rootJsonSchema.isArraySchema()) {
            // TODO 解析javadoc中的@return，作为它的注释。优先级低，这样的写法非常少
            content.append("| | ").append(rootJsonSchema.getType().value()).append(" | | |\n");
        }

        JsonSchemaTraverseUtils.traverse(rootJsonSchema, (propertyName, jsonSchema, parentJsonSchema, depth) -> {
            if (jsonSchema.isArraySchema()) {
                return;
            }
            // 如果parent是arrayNode，从parrent获取jpdv，否则从自身node获取jpdv
            JsonPropertyDescriptionValueDTO jpdv;
            if (parentJsonSchema.isArraySchema()) {
                jpdv = JsonPropertyDescriptionValueDTO.deserialize(parentJsonSchema.getDescription());
            } else {
                jpdv = JsonPropertyDescriptionValueDTO.deserialize(jsonSchema.getDescription());
            }

            if (jpdv == null) {
                // root schema 为 object arrary
                jpdv = new JsonPropertyDescriptionValueDTO();
            }

            content.append("|");
            // 字段名
            content.append(StringUtils.repeat("- ", depth)).append(propertyName);
            content.append("|");
            // JSON类型
            if (jpdv.getReferencePath() != null) {
                content.append("Object");
            } else {
                content.append(StringUtils.capitalize(jsonSchema.getType().value()));
            }
            if (parentJsonSchema.isArraySchema()) {
                content.append(" Array");
            }
            if (jpdv.getReferencePath() != null) {
                content.append("<br>数据结构同：").append(jpdv.getReferencePath());
            }
            content.append("|");
            // 注释
            content.append(Joiner.on("<br>").join(jpdv.getCommentLines()));
            content.append("|");
            // 其他 - 校验项
            StringBuilder validDoc = null;
            if (isReqBody) {
                validDoc = new StringBuilder();
                if (CollectionUtils.isNotEmpty(jpdv.getValids())) {
                    if (jpdv.getValids().size() == 1) {
                        validDoc.append(jpdv.getValids().get(0).getValidatorType())
                                .append(jpdv.getValids().get(0).getNote());
                    } else {
                        for (int i = 0; i < jpdv.getValids().size(); i++) {
                            AnalyzeValidRetval validator = jpdv.getValids().get(i);
                            validDoc.append(i + 1).append(". ").append(validator.getValidatorType())
                                    .append(validator.getNote()).append("<br>");
                        }
                        validDoc.delete(validDoc.length() - 4, validDoc.length());
                    }
                }
            }

            // 其他 - 枚举项
            StringBuilder enumDoc = null;
            if (CollectionUtils.isNotEmpty(jpdv.getAnalyzeEnumConstantsRetvals())) {
                enumDoc = new StringBuilder();
                for (AnalyzeEnumConstantsRetval enumConstant : jpdv.getAnalyzeEnumConstantsRetvals()) {
                    enumDoc.append(enumConstant.getCode()).append(" : ").append(enumConstant.getTitle()).append("<br>");
                }
                enumDoc.delete(enumDoc.length() - 4, enumDoc.length());
            }
            // 其他 - 格式
            String formatDoc = null;
            if (StringUtils.isNotEmpty(jpdv.getFormatPattern())) {
                formatDoc = "格式：" + jpdv.getFormatPattern();
            }
            // 其他 - 更多
            String moreDoc = null;
            if (CollectionUtils.isNotEmpty(jpdv.getMoreDocLines())) {
                moreDoc = Joiner.on("<br>").join(jpdv.getMoreDocLines());
            }
            content.append(Joiner.on("<br><br>").skipNulls().join(validDoc, enumDoc, formatDoc, moreDoc));
            content.append("|");
            content.append("\n");
        });

        content.insert(0, "| --- | --- | --- | --- |\n");
        content.insert(0, "| 字段名 | JSON类型 | 注释 | 其他 |\n");
        return content.toString();
    }

    private static EasyRandom initEasyRandom() {
        EasyRandomParameters erp = new EasyRandomParameters();
        erp.collectionSizeRange(2, 2);
        erp.stringLengthRange(1, 8);
        erp.randomizationDepth(5);
        erp.ignoreRandomizationErrors(true);
        erp.randomize(String.class, () -> RandomStringUtils.randomAlphanumeric(8));
        erp.randomize(Integer.class, () -> RandomUtils.nextInt(0, 99999));
        erp.randomize(Long.class, () -> RandomUtils.nextLong(1000000000L, 9999999999L));
        erp.randomize(BigDecimal.class,
                () -> new BigDecimal(RandomStringUtils.randomNumeric(5) + "." + RandomStringUtils.randomNumeric(2)));
        erp.randomize(Object.class, () -> "x");
        return new EasyRandom(erp);
    }

    protected String generateMoreDoc(EndpointDTO endpoint) {
        return "";
    }

}
