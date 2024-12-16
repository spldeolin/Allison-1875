package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.docanalyzer.javabean.AnalyzeMvcHandlerRetval;
import com.spldeolin.allison1875.docanalyzer.javabean.MvcHandlerDTO;
import com.spldeolin.allison1875.docanalyzer.service.MvcHandlerAnalyzerService;
import com.spldeolin.allison1875.docanalyzer.util.MethodQualifierUtils;

/**
 * @author Deolin 2020-12-04
 */
@Singleton
public class MvcHandlerAnalyzerServiceImpl implements MvcHandlerAnalyzerService {

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public AnalyzeMvcHandlerRetval analyzeMvcHandler(ClassOrInterfaceDeclaration mvcControllerCoid,
            MvcHandlerDTO mvcHandler) {
        AnalyzeMvcHandlerRetval result = new AnalyzeMvcHandlerRetval();
        result.setCat(mvcHandler.getCat().trim());
        result.setHandlerSimpleName(mvcControllerCoid.getName() + "_" + mvcHandler.getMd().getName());
        result.setDescriptionLines(ananlyzeMethodDesc(mvcHandler));
        result.setIsDeprecated(isDeprecated(mvcControllerCoid, mvcHandler.getMd()));
        result.setAuthor(JavadocUtils.getAuthor(mvcHandler.getMd()));
        result.setSourceCode(MethodQualifierUtils.getTypeQualifierWithMethodName(mvcHandler.getMd()));
        return result;
    }

    protected boolean isDeprecated(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        return annotationExprService.isAnnotated(Deprecated.class.getName(), handler)
                || annotationExprService.isAnnotated(Deprecated.class.getName(), controller);
    }

    protected List<String> ananlyzeMethodDesc(MvcHandlerDTO mvcHandler) {
        // 可拓展为分析Swagger注解等
        return JavadocUtils.getCommentAsLines(mvcHandler.getMd());
    }

}