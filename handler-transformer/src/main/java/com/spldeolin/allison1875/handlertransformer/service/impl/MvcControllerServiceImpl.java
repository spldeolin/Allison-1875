package com.spldeolin.allison1875.handlertransformer.service.impl;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.javabean.GenerateMvcHandlerRetval;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.service.MemberAdderService;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateServiceAndImplRetval;
import com.spldeolin.allison1875.handlertransformer.javabean.InitDecAnalysisDTO;
import com.spldeolin.allison1875.handlertransformer.service.MvcControllerService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-01-12
 */
@Singleton
@Slf4j
public class MvcControllerServiceImpl implements MvcControllerService {

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private HandlerTransformerConfig config;

    @Inject
    private MemberAdderService memberAdderService;

    @Override
    public List<ClassOrInterfaceDeclaration> detectMvcControllers(CompilationUnit cu) {
        return cu.findAll(ClassOrInterfaceDeclaration.class, this::isController);
    }

    private boolean isController(ClassOrInterfaceDeclaration coid) {
        for (AnnotationExpr annotation : coid.getAnnotations()) {
            try {
                ResolvedAnnotationDeclaration resolve = annotation.resolve();
                if (resolve.hasAnnotation(annotationExprService.springController().getNameAsString())
                        || annotationExprService.springController().getNameAsString()
                        .equals(resolve.getQualifiedName())) {
                    return true;
                }
            } catch (Exception e) {
                log.error("annotation [{}] of class [{}] cannot resolve", annotation.getNameAsString(),
                        coid.getNameAsString(), e);
            }
        }
        return false;
    }

    @Override
    public void replaceMvcHandlerToInitDec(InitDecAnalysisDTO initDecAnalysisDTO,
            GenerateServiceAndImplRetval generateServiceAndImplRetval,
            GenerateMvcHandlerRetval generateMvcHandlerRetval) {
        ClassOrInterfaceDeclaration mvcController = initDecAnalysisDTO.getMvcController();

        // 使用handle创建Handler方法，并追加到controller中
        initDecAnalysisDTO.getInitDec().replace(generateMvcHandlerRetval.getMvcHandler());

        for (AnnotationExpr annotationExpr : generateMvcHandlerRetval.getAnnotationsAddingToMvcController()) {
            if (!mvcController.getAnnotations().contains(annotationExpr)) {
                mvcController.addAnnotation(annotationExpr);
            }
        }
    }

}