package com.spldeolin.allison1875.handlertransformer.handle;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.spldeolin.allison1875.base.builder.SingleMethodServiceCuBuilder;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;

/**
 * @author Deolin 2021-01-10
 */
public interface CreateHandlerHandle {

    MethodDeclaration createHandler(CompilationUnit handlerCu, FirstLineDto firstLineDto, String serviceParamType,
            String serviceResultType, SingleMethodServiceCuBuilder serviceCuBuilder);

}