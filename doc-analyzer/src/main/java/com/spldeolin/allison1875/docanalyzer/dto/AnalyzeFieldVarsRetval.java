package com.spldeolin.allison1875.docanalyzer.dto;

import java.util.List;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-02-24
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnalyzeFieldVarsRetval {

    /**
     * 注释
     */
    final List<String> commentLines = Lists.newArrayList();

    /**
     * 枚举项分析结果
     */
    final List<AnalyzeEnumConstantsRetval> analyzeEnumConstantsRetvals = Lists.newArrayList();

    /**
     * 可拓展地对FieldVar进行更多分析，并生成文档。每个元素代表文档的每一行
     */
    final List<String> moreDocLines = Lists.newArrayList();

    public AnalyzeFieldVarsRetval() {
    }

    public AnalyzeFieldVarsRetval(String comment) {
        commentLines.add(comment);
    }

}