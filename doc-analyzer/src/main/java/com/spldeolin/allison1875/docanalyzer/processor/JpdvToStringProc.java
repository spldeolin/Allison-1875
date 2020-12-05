package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.Map;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.util.CollectionUtils;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.handle.MoreJpdvAnalysisHandle;
import com.spldeolin.allison1875.docanalyzer.javabean.EnumCodeAndTitleDto;
import com.spldeolin.allison1875.docanalyzer.javabean.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.javabean.ValidatorDto;

/**
 * @author Deolin 2020-12-02
 */
public class JpdvToStringProc {

    private final MoreJpdvAnalysisHandle moreJpdvAnalysisHandle;

    private final DocAnalyzerConfig docAnalyzerConfig;

    public JpdvToStringProc(MoreJpdvAnalysisHandle moreJpdvAnalysisHandle, DocAnalyzerConfig docAnalyzerConfig) {
        this.moreJpdvAnalysisHandle = moreJpdvAnalysisHandle;
        this.docAnalyzerConfig = docAnalyzerConfig;
    }

    public String toString(JsonPropertyDescriptionValueDto jpdv) {
        if (jpdv == null) {
            return null;
        }


        if (jpdv.getIsFieldCrossingValids()) {
            StringBuilder sb = new StringBuilder(64);
            for (ValidatorDto valid : jpdv.getValids()) {
                sb.append(valid.getValidatorType()).append(valid.getNote()).append("\n");
            }
            return sb.toString();
        } else {

            String ref = null;
            if (jpdv.getReferencePath() != null) {
                ref = "复用类型\n" + "\t数据结构同：" + jpdv.getReferencePath();
            }
            String comment = null;
            if (CollectionUtils.isNotEmpty(jpdv.getDescriptionLines())) {
                StringBuilder sb = new StringBuilder();
                for (String line : jpdv.getDescriptionLines()) {
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
            String validInfo = null;
            if (CollectionUtils.isNotEmpty(jpdv.getValids())) {
                StringBuilder sb = new StringBuilder("校验项\n");
                for (ValidatorDto valid : jpdv.getValids()) {
                    sb.append("\t").append(valid.getValidatorType()).append(valid.getNote()).append("\n");
                }
                validInfo = sb.deleteCharAt(sb.length() - 1).toString();
            }
            String format = null;
            if (jpdv.getJsonFormatPattern() != null) {
                format = "格式\n";
                format += "\t" + jpdv.getJsonFormatPattern();
            }
            String enumInfo = null;
            if (CollectionUtils.isNotEmpty(jpdv.getEcats())) {
                StringBuilder sb = new StringBuilder("枚举项\n");
                Map<String, String> catsMap = Maps.newHashMap();
                for (EnumCodeAndTitleDto ecat : jpdv.getEcats()) {
                    catsMap.put(ecat.getCode(), ecat.getTitle());
                }
                for (String line : StringUtils.splitLineByLine(JsonUtils.toJsonPrettily(catsMap))) {
                    sb.append("\t").append(line).append("\n");
                }
                enumInfo = sb.deleteCharAt(sb.length() - 1).toString();
            }
            String extra = moreJpdvAnalysisHandle.moreJpdvToString(jpdv.getMore(), docAnalyzerConfig);

            return Joiner.on("\n\n").skipNulls().join(ref, comment, validInfo, format, enumInfo, extra);
        }

    }

}