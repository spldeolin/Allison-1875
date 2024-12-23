package com.spldeolin.allison1875.handlertransformer.service;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.handlertransformer.enums.DTOTypeEnum;
import com.spldeolin.allison1875.handlertransformer.service.impl.FieldServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(FieldServiceImpl.class)
public interface FieldService {

    void more4SpecialTypeField(FieldDeclaration field, DTOTypeEnum dtoTypeEnum);

}