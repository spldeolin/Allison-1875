package com.spldeolin.allison1875.querytransformer.service.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.exception.CuAbsentException;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.QueryTransformerConfig;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParamGenerationDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultGenerationDto;
import com.spldeolin.allison1875.querytransformer.service.FindMapperService;
import com.spldeolin.allison1875.querytransformer.service.GenerateMethodIntoMapperService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-10-10
 */
@Singleton
@Slf4j
public class GenerateMethodIntoMapperServiceImpl implements GenerateMethodIntoMapperService {

    @Inject
    private FindMapperService findMapperService;

    @Inject
    private QueryTransformerConfig config;

    @Inject
    private AntiDuplicationService antiDuplicationService;

    @Override
    public CompilationUnit generate(AstForest astForest, DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            ParamGenerationDto paramGeneration, ResultGenerationDto resultGeneration) {
        ClassOrInterfaceDeclaration mapper = findMapperService.findMapper(astForest, designMeta);
        if (mapper == null) {
            return null;
        }

        String methodName = chainAnalysis.getMethodName();
        methodName = antiDuplicationService.getNewMethodNameIfExist(methodName, mapper);

        CompilationUnit mapperCu = mapper.findCompilationUnit().orElseThrow(() -> new CuAbsentException(mapper));
        for (String anImport : resultGeneration.getImports()) {
            mapperCu.addImport(anImport);
        }
        for (String anImport : paramGeneration.getImports()) {
            mapperCu.addImport(anImport);
        }

        MethodDeclaration method = new MethodDeclaration();
        if (config.getEnableLotNoAnnounce()) {
            method.setJavadocComment(chainAnalysis.getLotNo());
        }
        method.setType(resultGeneration.getResultType());
        method.setName(methodName);
        method.setParameters(new NodeList<>(paramGeneration.getParameters()));
        method.setBody(null);
        mapper.getMembers().add(method);
        return mapperCu;
    }

}