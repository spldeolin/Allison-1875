package com.spldeolin.allison1875.da.dto;

import java.util.Collection;
import org.apache.commons.collections4.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Joiner;
import com.spldeolin.allison1875.base.util.StringUtils;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-04-27
 */
@Data
@Accessors(chain = true)
@JsonInclude(Include.NON_NULL)
public class JsonPropertyDescriptionValueDto {

    private Collection<String> descriptionLines;

    /**
     * 解析自声明在Field上的校验注解
     *
     * e.g: @NotEmpty private Collection<String> userNames;
     */
    private Collection<ValidatorDto> validators;

    private String jsonFormatPattern;

    public String toStringPrettily() {
        String comment = null;
        if (CollectionUtils.isNotEmpty(descriptionLines)) {
            StringBuilder sb = new StringBuilder();
            for (String line : descriptionLines) {
                if (StringUtils.isNotBlank(line)) {
                    sb.append("\t").append(line).append("\n");
                } else {
                    sb.append("\n");
                }
            }
            // sb并不是只有换号符时
            if (StringUtils.isNotBlank(sb)) {
                sb.insert(0, "注释\n");
                comment = sb.deleteCharAt(sb.length() - 1).toString();
            }
        }
        String validatorInfo = null;
        if (CollectionUtils.isNotEmpty(validators)) {
            StringBuilder sb = new StringBuilder("校验项\n");
            for (ValidatorDto validator : validators) {
                sb.append("\t").append(validator.getValidatorType()).append(validator.getNote()).append("\n");
            }
            validatorInfo = sb.deleteCharAt(sb.length() - 1).toString();
        }
        String format = null;
        if (jsonFormatPattern != null) {
            format = "格式\n";
            format += "\t" + jsonFormatPattern;
        }
        return Joiner.on("\n\n").skipNulls().join(comment, validatorInfo, format);
    }

}