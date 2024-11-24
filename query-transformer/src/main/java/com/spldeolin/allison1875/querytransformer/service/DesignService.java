package com.spldeolin.allison1875.querytransformer.service;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMetaDto;
import com.spldeolin.allison1875.querytransformer.javabean.ReplaceDesignArgs;
import com.spldeolin.allison1875.querytransformer.service.impl.DesignServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(DesignServiceImpl.class)
public interface DesignService {

    ClassOrInterfaceDeclaration detectDesignOrJoinDesign(String qualifier);

    DesignMetaDto findDesignMeta(MethodCallExpr designChain);

    DesignMetaDto findDesignMeta(ClassOrInterfaceDeclaration design);

    void replaceDesign(ReplaceDesignArgs args);

}