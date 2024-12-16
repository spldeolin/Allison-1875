package com.spldeolin.allison1875.docanalyzer.javabean;

import java.util.List;
import com.github.javaparser.utils.StringEscapeUtils;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.util.JsonUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-04-27
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class JsonPropertyDescriptionValueDTO {

    /**
     * 注释
     */
    final List<String> commentLines = Lists.newArrayList();

    /**
     * 枚举项
     */
    final List<AnalyzeEnumConstantsRetval> analyzeEnumConstantsRetvals = Lists.newArrayList();

    /**
     * 校验项
     */
    final List<AnalyzeValidRetval> valids = Lists.newArrayList();

    /**
     * 格式
     */
    String formatPattern;

    /**
     * 可拓展地对FieldVar进行更多分析，并生成文档。每个元素代表文档的每一行
     */
    final List<String> moreDocLines = Lists.newArrayList();

    /**
     * 所用复用类型的第一次出现在的JsonSchemaNode的path
     */
    String referencePath;

    public String serialize() {
        return JsonUtils.toJson(this);
    }

    public static JsonPropertyDescriptionValueDTO deserialize(String json) {
        if (json == null) {
            return null;
        }
        try {
            return JsonUtils.toObject(json, JsonPropertyDescriptionValueDTO.class);
        } catch (Exception e) {
            log.info("jpdv has been pretty [{}]", StringEscapeUtils.escapeJava(json));
            return null;
        }

    }

}