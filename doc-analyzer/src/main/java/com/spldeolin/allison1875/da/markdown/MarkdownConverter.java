package com.spldeolin.allison1875.da.markdown;

import java.util.Collection;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.da.dto.EndpointDto;
import com.spldeolin.allison1875.da.dto.PropertyDto;
import com.spldeolin.allison1875.da.dto.ValidatorDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-18
 */
@Log4j2
public class MarkdownConverter {

    private static final String horizontalLine = "-";

    public void convert(Collection<EndpointDto> endpoints) {
        for (EndpointDto endpoint : endpoints) {
            EndpointVo vo = new EndpointVo();
            // 概要
            vo.setUri(Joiner.on("\n").join(endpoint.getUrls()));
            vo.setHttpMethod(Joiner.on("\n").join(endpoint.getHttpMethods()));
            vo.setDescription(Joiner.on("\n").join(endpoint.getDescriptionLines()));

            // 参数结构
            vo.setRequestBodySituation(endpoint.getRequestBodySituation().getValue());
            vo.setRequestBodyJsonSchema(JsonUtils.toJsonPrettily(endpoint.getRequestBodyJsonSchema()));
            if (endpoint.getRequestBodyProperties() != null) {
                boolean anyObjectLiekTypeExistInRequestBody = false;
                Collection<RequestBodyPropertyVo> propVos = Lists.newArrayList();
                for (PropertyDto dto : endpoint.getRequestBodyProperties()) {
                    RequestBodyPropertyVo propVo = new RequestBodyPropertyVo();
                    propVo.setPath(dto.getPath());
                    propVo.setName(dto.getName());
                    propVo.setDescription(emptyToHorizontalLine(dto.getDescription()));
                    String fullJsonType = dto.getJsonType().getValue();
                    if (Boolean.TRUE.equals(dto.getIsFloat())) {
                        fullJsonType += " (float)";
                    }
                    if (StringUtils.isNotBlank(dto.getDatetimePattern())) {
                        fullJsonType += " (" + dto.getDatetimePattern() + ")";
                    }
                    propVo.setDetailedJsonType(fullJsonType);
                    propVo.setValidators(convertValidators(dto.getValidators()));
                    if (dto.getJsonType().isObjectLike()) {
                        anyObjectLiekTypeExistInRequestBody = true;
                    }
                    propVos.add(propVo);
                }
                vo.setRequestBodyProperties(propVos);
                vo.setAnyObjectLiekTypeExistInRequestBody(anyObjectLiekTypeExistInRequestBody);
                vo.setAnyValidatorsExist(propVos.stream().anyMatch(one -> !horizontalLine.equals(one.getValidators())));
            }

            // 返回值结构
            vo.setResponseBodySituation(endpoint.getResponseBodySituation().getValue());
            vo.setResponseBodyJsonSchema(JsonUtils.toJsonPrettily(endpoint.getResponseBodyJsonSchema()));
            if (endpoint.getResponseBodyProperties() != null) {
                Collection<ResponseBodyPropertyVo> propVos = Lists.newArrayList();
                boolean anyObjectLiekTypeExistInResponseBody = false;
                for (PropertyDto dto : endpoint.getResponseBodyProperties()) {
                    ResponseBodyPropertyVo propVo = new ResponseBodyPropertyVo();
                    propVo.setPath(dto.getPath());
                    propVo.setName(dto.getName());
                    propVo.setDescription(emptyToHorizontalLine(dto.getDescription()));
                    String fullJsonType = dto.getJsonType().getValue();
                    if (Boolean.TRUE.equals(dto.getIsFloat())) {
                        fullJsonType += " (float)";
                    }
                    if (StringUtils.isNotBlank(dto.getDatetimePattern())) {
                        fullJsonType += " (" + dto.getDatetimePattern() + ")";
                    }
                    propVo.setDetailedJsonType(fullJsonType);
                    if (dto.getJsonType().isObjectLike()) {
                        anyObjectLiekTypeExistInResponseBody = true;
                    }
                    propVos.add(propVo);
                }
                vo.setResponseBodyProperties(propVos);
                vo.setAnyObjectLiekTypeExistInResponseBody(anyObjectLiekTypeExistInResponseBody);
            }

            vo.setAuthor(endpoint.getAuthor());
            vo.setSourceCode(endpoint.getSourceCode());

            String uriFirstLine = StringUtils.splitLineByLine(vo.getUri()).get(0);
            String description = vo.getDescription();
            String fileName =
                    Iterables.getFirst(StringUtils.splitLineByLine(description), uriFirstLine).replace('/', '-')
                            + ".md";
            String groupNames = endpoint.getCat();
            // 暂时移除输出到本地的功能
//            File dir = Paths.get(DocAnalyzerConfig.getInstance().getDocOutputDirectoryPath())
//                    .resolve(groupNames.replace('.', File.separatorChar)).toFile();
//            if (!dir.exists()) {
//                if (!dir.mkdirs()) {
//                    log.warn("mkdirs [{}] failed.", dir);
//                    continue;
//                }
//            }
//            File output = dir.toPath().resolve(fileName).toFile();
//            try {
//                FreeMarkerPrinter.printToFile(vo, output);
//            } catch (FreeMarkerPrintExcpetion e) {
//                log.warn("FreeMarkerPrinter print failed.", e);
//            }
        }

    }

    private String emptyToHorizontalLine(String linkName) {
        if (StringUtils.isEmpty(linkName)) {
            return horizontalLine;
        }
        return linkName;
    }

    private String convertValidators(Collection<ValidatorDto> validators) {
        StringBuilder result = new StringBuilder(64);

        if (validators != null && validators.size() > 0) {
            for (ValidatorDto validator : validators) {
                result.append("　");
                result.append(validator.getValidatorType());
                result.append(validator.getNote());
            }
        }

        if (result.length() == 0) {
            return horizontalLine;
        } else {
            return result.toString();
        }
    }

}