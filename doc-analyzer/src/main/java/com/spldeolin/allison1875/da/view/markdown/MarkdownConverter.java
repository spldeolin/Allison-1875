package com.spldeolin.allison1875.da.view.markdown;

import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.exception.FreeMarkerPrintExcpetion;
import com.spldeolin.allison1875.da.core.domain.ApiDomain;
import com.spldeolin.allison1875.da.core.domain.BodyFieldDomain;
import com.spldeolin.allison1875.da.core.domain.ValidatorDomain;
import com.spldeolin.allison1875.da.core.enums.BodyTypeEnum;
import com.spldeolin.allison1875.da.core.enums.NumberFormatTypeEnum;
import com.spldeolin.allison1875.da.core.enums.StringFormatTypeEnum;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-18
 */
@Log4j2
public class MarkdownConverter {

    public void convert(Collection<ApiDomain> apis) {
        for (ApiDomain api : apis) {
            SimpleMdOutputVo vo = new SimpleMdOutputVo();
            vo.setUri(Iterables.getFirst(api.uri(), ""));
            vo.setDescription(api.description());
            vo.setIsRequestBodyNone(BodyTypeEnum.none == api.requestBodyType());
            vo.setIsRequestBodyChaos(BodyTypeEnum.chaos == api.requestBodyType());
            vo.setIsResponseBodyNone(BodyTypeEnum.none == api.responseBodyType());
            vo.setIsResponseBodyChaos(BodyTypeEnum.chaos == api.responseBodyType());
            vo.setAuthor(emptyToDefault(api.author(), "未知开发者"));
            vo.setLocation(api.codeSourceLocation());

            if (!vo.getIsRequestBodyChaos() && !vo.getIsRequestBodyNone()) {
                Collection<RequestBodyFieldVo> fieldVos = Lists.newArrayList();
                for (BodyFieldDomain field : api.listRequestBodyFieldsFlatly()) {
                    RequestBodyFieldVo fieldVo = new RequestBodyFieldVo();
                    fieldVo.setLinkName(surroundMdCodeStyleForListLinkNamePart(field.linkName()));
                    fieldVo.setDescription(nullToEmpty(field.description()));
                    fieldVo.setJsonTypeAndFormats(converterTypeAndFormat(field));
                    fieldVo.setValidators(convertValidators(field.nullable(), field.validators()));
                    fieldVos.add(fieldVo);
                }
                vo.setRequestBodyFields(fieldVos);
                vo.setAnyValidatorsExist(fieldVos.stream().anyMatch(fieldVo -> !"".equals(fieldVo.getValidators())));
            }
            if (!vo.getIsResponseBodyChaos() && !vo.getIsResponseBodyNone()) {
                Collection<ResponseBodyFieldVo> fieldVos = Lists.newArrayList();
                for (BodyFieldDomain field : api.listResponseBodyFieldsFlatly()) {
                    ResponseBodyFieldVo fieldVo = new ResponseBodyFieldVo();
                    fieldVo.setLinkName(surroundMdCodeStyleForListLinkNamePart(field.linkName()));
                    fieldVo.setDescription(nullToEmpty(field.description()));
                    fieldVo.setJsonTypeAndFormats(converterTypeAndFormat(field));
                    fieldVos.add(fieldVo);
                }
                vo.setResponseBodyFields(fieldVos);
            }

            try {
                FreeMarkerPrinter.print(vo, vo.getDescription());
            } catch (FreeMarkerPrintExcpetion e) {
                log.warn("FreeMarkerPrinter print failed.", e);
            }
        }

    }

    private String surroundMdCodeStyleForListLinkNamePart(String linkName) {
        if (StringUtils.isEmpty(linkName)) {
            return "`-`";
        }
        int i = linkName.lastIndexOf('.');
        if (i == -1) {
            return "`" + linkName + "`";
        }
        return Joiner.on("").join(linkName.substring(0, i + 1), "`", linkName.substring(i + 1), "`");
    }

    private Collection<String> converterTypeAndFormat(BodyFieldDomain field) {
        Collection<String> result = Lists.newArrayList();
        result.add(field.jsonType().getValue());
        String stringFormat = field.stringFormat();
        if (stringFormat != null && !StringFormatTypeEnum.normal.getValue().equals(stringFormat)) {
            result.add(stringFormat);
        }
        NumberFormatTypeEnum numberFormat = field.numberFormat();
        if (numberFormat != null) {
            result.add(numberFormat.getValue());
        }
        return result;
    }

    private String convertValidators(Boolean nullable, Collection<ValidatorDomain> validators) {
        StringBuilder result = new StringBuilder(64);

        if (Boolean.FALSE.equals(nullable)) {
            result.append("必填");
            result.append("　");
        }

        if (validators != null && validators.size() > 0) {
            Collection<String> parts = Lists.newLinkedList();
            validators.forEach(validator -> {
                parts.add(validator.validatorType().getDescription());
                parts.add(validator.note());
            });
            Joiner.on("　").skipNulls().appendTo(result, parts);
        }

        if (result.length() == 0) {
            return "";
        } else {
            return result.toString();
        }
    }

    private String nullToEmpty(String s) {
        if (s == null) {
            s = "";
        }
        return s;
    }

    private String emptyToDefault(String s, String defaultString) {
        if (StringUtils.isEmpty(s)) {
            return defaultString;
        }
        return s;
    }

}