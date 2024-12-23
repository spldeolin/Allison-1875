package com.spldeolin.allison1875.extension.satisficing.handlertransformer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.handlertransformer.enums.DTOTypeEnum;
import com.spldeolin.allison1875.handlertransformer.service.FieldService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-01-29
 */
@Singleton
@Slf4j
public class FieldServiceImpl2 implements FieldService {

    @Override
    public void more4SpecialTypeField(FieldDeclaration field, DTOTypeEnum dtoType) {
        if (Lists.newArrayList("Date", "LocalDate", "LocalTime", "LocalDateTime")
                .contains(field.getCommonType().toString())) {
            if (!field.getAnnotationByName("JsonFormat").isPresent()) {
                field.addAnnotation(StaticJavaParser.parseAnnotation(
                        "@com.fasterxml.jackson.annotation.JsonFormat(pattern = \"yyyy-MM-dd HH:mm:ss\", timezone = "
                                + "\"Asia/Shanghai\")"));
            }
        }
        boolean isRespOrInResp = Lists.newArrayList(DTOTypeEnum.RESP_DTO, DTOTypeEnum.NEST_DTO_IN_RESP)
                .contains(dtoType);
        if (field.getCommonType().toString().equals("Long") && isRespOrInResp) {
            if (!field.getAnnotationByName("JsonSerialize").isPresent()) {
                field.addAnnotation(StaticJavaParser.parseAnnotation(
                        "@com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson"
                                + ".databind.ser.std.ToStringSerializer.class)"));
            }
        }
    }

}