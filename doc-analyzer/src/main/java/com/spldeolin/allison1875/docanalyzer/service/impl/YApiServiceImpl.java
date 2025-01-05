package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.HashingUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.constant.YApiConstant;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeEnumConstantsRetval;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeValidRetval;
import com.spldeolin.allison1875.docanalyzer.dto.EndpointDTO;
import com.spldeolin.allison1875.docanalyzer.dto.JsonPropertyDescriptionValueDTO;
import com.spldeolin.allison1875.docanalyzer.dto.YApiInterfaceListMenuRespDTO;
import com.spldeolin.allison1875.docanalyzer.dto.YApiProjectGetRespDTO;
import com.spldeolin.allison1875.docanalyzer.service.YApiOpenApiService;
import com.spldeolin.allison1875.docanalyzer.service.YApiService;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaTraverseUtils;
import com.spldeolin.allison1875.docanalyzer.util.MarkdownUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 将endpoints同步到YApi
 *
 * @author Deolin 2020-07-26
 */
@Singleton
@Slf4j
public class YApiServiceImpl implements YApiService {

    @Inject
    private CommonConfig commonConfig;

    @Inject
    private DocAnalyzerConfig config;

    @Inject
    private YApiOpenApiService yapiOpenApiService;

    @Override
    public void flushToYApi(List<EndpointDTO> endpoints) {
        YApiProjectGetRespDTO project = yapiOpenApiService.getProject();
        Long projectId = project.getId();

        Set<String> catNames = endpoints.stream().map(EndpointDTO::getCat).collect(Collectors.toSet());
        catNames.add("回收站");
        Set<String> yapiCatNames = this.getYapiCatIdsEachName(projectId).keySet();
        this.createYApiCat(Sets.difference(catNames, yapiCatNames), projectId);

        Map<String, Long> catName2catId = this.getYapiCatIdsEachName(projectId);

        Map<String, JsonNode> yapiUrls = this.listAutoInterfaces(projectId);
        Set<String> analysisUrls = endpoints.stream().map(e -> Joiner.on(" 或 ").join(e.getUrls()))
                .collect(Collectors.toSet());

        // yapi中，在解析出endpoint中找不到url的接口，移动到回收站
        for (String yapiUrl : yapiUrls.keySet()) {
            if (!analysisUrls.contains(yapiUrl)) {
                this.deleteInterface(yapiUrls.get(yapiUrl), catName2catId.get("回收站"));
            }
        }

        // 新增接口
        for (EndpointDTO endpoint : endpoints) {
            List<String> descriptionLines = endpoint.getDescriptionLines();
            String title = Iterables.getFirst(descriptionLines, null);
            String yapiDesc = this.generateEndpointDoc(endpoint);

            String reqJs = this.generateReqOrRespDoc(endpoint.getRequestBodyJsonSchema());
            String respJs = this.generateReqOrRespDoc(endpoint.getResponseBodyJsonSchema());

            JsonNode yapiInterface = this.createYApiInterface(title, Joiner.on(" 或 ").join(endpoint.getUrls()), reqJs,
                    respJs, yapiDesc,
                    endpoint.getHttpMethod(), catName2catId.get(endpoint.getCat()));
            log.info("Endpoint [{}] output to YApi Project [{}]({}...{}), response: {}", endpoint.getUrls(),
                    project.getName(), StringUtils.left(config.getYapiToken(), 6),
                    StringUtils.right(config.getYapiToken(), 6), JsonUtils.toJson(yapiInterface));
        }
    }

    private String generateReqOrRespDoc(JsonSchema bodyJsonSchema) {
        if (bodyJsonSchema == null) {
            return "";
        }
        ObjectMapper om = JsonUtils.createObjectMapper();
        try {
            om.writeValueAsString(bodyJsonSchema.getDescription());
        } catch (Exception e) {
            log.info("不再重复toPrettyString [{}]", bodyJsonSchema.getId());
            return JsonUtils.toJson(bodyJsonSchema);
        }

        // jpdv -> Pretty String
        JsonSchemaTraverseUtils.traverse(bodyJsonSchema, (propertyName, jsonSchema, parentJsonSchema, depth) -> {
            JsonPropertyDescriptionValueDTO jpdv = JsonPropertyDescriptionValueDTO.deserialize(
                    jsonSchema.getDescription());
            if (jpdv != null) {

                String refTypeDoc = null;
                if (jpdv.getReferencePath() != null) {
                    refTypeDoc = "复用类型\n" + "\t数据结构同：" + jpdv.getReferencePath();
                }
                String commentDoc = null;
                if (CollectionUtils.isNotEmpty(jpdv.getCommentLines())) {
                    StringBuilder sb = new StringBuilder();
                    for (String line : jpdv.getCommentLines()) {
                        if (StringUtils.isNotBlank(line)) {
                            sb.append("\t").append(line).append("\n");
                        } else {
                            sb.append("\n");
                        }
                    }
                    // sb并不是只有换号符时
                    if (StringUtils.isNotBlank(sb)) {
                        sb.insert(0, "注释\n");
                        commentDoc = sb.deleteCharAt(sb.length() - 1).toString();
                    }
                }
                String validDoc = null;
                if (CollectionUtils.isNotEmpty(jpdv.getValids())) {
                    StringBuilder sb = new StringBuilder("校验项\n");
                    for (AnalyzeValidRetval valid : jpdv.getValids()) {
                        sb.append("\t").append(valid.getValidatorType()).append(valid.getNote()).append("\n");
                    }
                    validDoc = sb.deleteCharAt(sb.length() - 1).toString();
                }
                String dataFormatDoc = null;
                if (StringUtils.isNotEmpty(jpdv.getFormatPattern())) {
                    dataFormatDoc = "格式\n";
                    dataFormatDoc += "\t" + jpdv.getFormatPattern();
                }
                String enumDoc = null;
                if (CollectionUtils.isNotEmpty(jpdv.getAnalyzeEnumConstantsRetvals())) {
                    StringBuilder sb = new StringBuilder("枚举项\n");
                    Map<String, String> catsMap = Maps.newHashMap();
                    for (AnalyzeEnumConstantsRetval ecat : jpdv.getAnalyzeEnumConstantsRetvals()) {
                        catsMap.put(ecat.getCode(), ecat.getTitle());
                    }
                    for (String line : MoreStringUtils.splitLineByLine(JsonUtils.toJsonPrettily(catsMap))) {
                        sb.append("\t").append(line).append("\n");
                    }
                    enumDoc = sb.deleteCharAt(sb.length() - 1).toString();
                }

                String moreDoc = null;
                if (CollectionUtils.isNotEmpty(jpdv.getMoreDocLines())) {
                    StringBuilder sb = new StringBuilder("其他\n");
                    for (String moreDocLine : jpdv.getMoreDocLines()) {
                        sb.append("\t").append(moreDocLine).append("\n");
                    }
                    moreDoc = sb.deleteCharAt(sb.length() - 1).toString();
                }

                jsonSchema.setDescription(Joiner.on("\n\n").skipNulls()
                        .join(refTypeDoc, commentDoc, validDoc, dataFormatDoc, enumDoc, moreDoc));
            }
        });
        return JsonUtils.toJson(bodyJsonSchema);
    }

    private Map<String, Long> getYapiCatIdsEachName(Long projectId) {
        List<YApiInterfaceListMenuRespDTO> cats = yapiOpenApiService.listCats(projectId);
        Map<String, Long> result = Maps.newHashMap();
        for (YApiInterfaceListMenuRespDTO cat : cats) {
            result.put(cat.getName(), cat.getId());
        }
        return result;
    }

    private void createYApiCat(Set<String> catNames, Long projectId) {
        for (String catName : catNames) {
            JsonNode responseBody = yapiOpenApiService.createCat("", catName, projectId);
            log.info("create yapi cat. catName={} rawRespBody={}", catName, JsonUtils.toJson(responseBody));
        }
    }

    private Map<String, JsonNode> listAutoInterfaces(Long projectId) {
        JsonNode jsonNode = yapiOpenApiService.listCatsAsJsonNode(projectId);
        Map<String, JsonNode> result = Maps.newHashMap();
        for (JsonNode data : jsonNode) {
            for (JsonNode interf : data.get("list")) {
                List<String> tags = JsonUtils.toListOfObject(interf.get("tag").toString(), String.class);
                if (tags.contains(YApiConstant.ALLISON_1875_TAG)) {
                    result.put(interf.get("path").asText(), interf);
                }
            }
        }
        return result;
    }

    private void deleteInterface(JsonNode jsonNode, Long recycleBinCatId) {
        if (recycleBinCatId.equals(jsonNode.get("catid").asLong())) {
            // 已在"回收站"分类中
            return;
        }
        Long id = jsonNode.get("_id").asLong();

        JsonNode respNode = yapiOpenApiService.getEndpoint(id);

        Map<String, Object> body = Maps.newHashMap();
        body.put("id", id);
        body.put("catid", recycleBinCatId);
        List<String> tags = JsonUtils.toListOfObject(jsonNode.get("tag").toString(), String.class);
        tags.add(YApiConstant.DELETE_TAG);
        body.put("tag", tags);

        String desc = "";
        JsonNode descNode = respNode.get("desc");
        if (descNode != null) {
            desc = descNode.asText();
        }
        String deleteMessage = MarkdownUtils.convertToHtml("> 该接口已被删除，或是它的URL已被更改，**禁止调用**\n");
        deleteMessage = MoreStringUtils.replaceLast(deleteMessage, "<strong>",
                "<span style='background:black;color:#FFD9E6'>");
        deleteMessage = MoreStringUtils.replaceLast(deleteMessage, "</strong>", "</span>");

        body.put("desc", deleteMessage + desc);
        body.put("token", config.getYapiToken());
        JsonNode jsonNode1 = yapiOpenApiService.updateEndpoint(body);
        log.info(JsonUtils.toJson(jsonNode1));
    }

    private JsonNode createYApiInterface(String title, String url, String requestBodyJsonSchema,
            String responseBodyJsonSchema, String description, String httpMethod, Long catId) {
        Map<String, Object> form = Maps.newHashMap();
        form.put("title", title);
        form.put("path", url);
        form.put("status", "done");
        form.put("req_body_type", "json");
        form.put("req_body_is_json_schema", true);
        form.put("req_body_other", requestBodyJsonSchema);
        form.put("res_body_type", "json");
        form.put("res_body_is_json_schema", true);
        form.put("res_body", responseBodyJsonSchema);
        form.put("switch_notice", true);
        form.put("message", "1");
        form.put("tag", Lists.newArrayList(YApiConstant.ALLISON_1875_TAG));
        form.put("desc", MarkdownUtils.convertToHtml(description));
        form.put("method", httpMethod);
        form.put("catid", catId);
        form.put("token", config.getYapiToken());
        JsonNode responseBody = yapiOpenApiService.createOrUpdateEndpoint(form);
        return responseBody;
    }

    protected String generateEndpointDoc(EndpointDTO endpoint) {
        String deprecatedNode = null;
        if (endpoint.getIsDeprecated()) {
            deprecatedNode = "> 该接口已被开发者标记为**已废弃**，不建议调用";
        }

        String comment = null;
        if (CollectionUtils.isNotEmpty(endpoint.getDescriptionLines())) {
            StringBuilder sb = new StringBuilder();
            for (String line : endpoint.getDescriptionLines()) {
                if (StringUtils.isNotBlank(line)) {
                    sb.append(line).append("\n");
                } else {
                    sb.append("\n");
                }
            }
            // sb并不是只有换号符时
            if (StringUtils.isNotBlank(sb)) {
                sb.insert(0, "##### 注释\n");
                comment = StringEscapeUtils.escapeHtml4(sb.deleteCharAt(sb.length() - 1).toString());
            }
        }

        String developer = "##### 开发者\n";
        if (StringUtils.isNotBlank(endpoint.getAuthor())) {
            developer += endpoint.getAuthor();
        } else {
            developer += "未知的开发者";
        }

        String code = "##### 源码\n";
        code += endpoint.getSourceCode();

        String allison1875Announce = "";
        if (commonConfig.getEnableNoModifyAnnounce() || commonConfig.getEnableLotNoAnnounce()) {
            allison1875Announce += BaseConstant.NEW_LINE + "---";
            if (commonConfig.getEnableNoModifyAnnounce()) {
                allison1875Announce += BaseConstant.NEW_LINE + BaseConstant.NO_MODIFY_ANNOUNCE;
            }
            if (commonConfig.getEnableLotNoAnnounce()) {
                if (commonConfig.getEnableNoModifyAnnounce()) {
                    allison1875Announce += " ";
                } else {
                    allison1875Announce += BaseConstant.NEW_LINE;
                }
                String hash = StringUtils.upperCase(HashingUtils.hashString(endpoint.toString()));
                allison1875Announce +=
                        BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + String.format("DA%s-%s", Allison1875.SHORT_VERSION,
                                hash);
            }
        }

        return Joiner.on('\n').skipNulls().join(deprecatedNode, comment, developer, code, allison1875Announce)
                + generateMoreDoc(endpoint);
    }

    protected String generateMoreDoc(EndpointDTO endpoint) {
        return "";
    }

}