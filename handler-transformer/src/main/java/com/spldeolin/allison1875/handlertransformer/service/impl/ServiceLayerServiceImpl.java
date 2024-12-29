package com.spldeolin.allison1875.handlertransformer.service.impl;

import java.nio.file.Path;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.dto.AddMethodToServiceArgs;
import com.spldeolin.allison1875.handlertransformer.dto.AddMethodToServiceRetval;
import com.spldeolin.allison1875.handlertransformer.dto.GenerateServiceAndImplArgs;
import com.spldeolin.allison1875.handlertransformer.dto.GenerateServiceAndImplRetval;
import com.spldeolin.allison1875.handlertransformer.dto.InitDecAnalysisDTO;
import com.spldeolin.allison1875.handlertransformer.service.ServiceLayerService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-01-10
 */
@Singleton
@Slf4j
public class ServiceLayerServiceImpl implements ServiceLayerService {

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private CommonConfig commonConfig;

    @Inject
    private HandlerTransformerConfig config;

    @Inject
    private AntiDuplicationService antiDuplicationService;

    @Inject
    private ImportExprService importExprService;

    @Override
    public MethodDeclaration generateServiceMethod(InitDecAnalysisDTO initDecAnalysisDTO, String reqBodyDTOType,
            List<VariableDeclarator> reqParams, String respBodyDTOType) {
        MethodDeclaration method = new MethodDeclaration();
        method.addAnnotation(annotationExprService.javaOverride());
        method.setPublic(true);
        if (respBodyDTOType != null) {
            method.setType(respBodyDTOType);
        } else {
            method.setType(new VoidType());
        }
        method.setName(initDecAnalysisDTO.getMvcHandlerMethodName());
        if (reqBodyDTOType != null) {
            method.addParameter(reqBodyDTOType, "req");
        }
        for (VariableDeclarator vd : reqParams) {
            method.addParameter(new Parameter(vd.getType(), vd.getName()));
        }

        BlockStmt body = new BlockStmt();
        if (respBodyDTOType != null) {
            body.addStatement(StaticJavaParser.parseStatement("return null;"));
        }
        method.setBody(body);

        return method;
    }

    @Override
    public AddMethodToServiceRetval addMethodToService(AddMethodToServiceArgs args) {
        GenerateServiceAndImplRetval serviceRetval = args.getGenerateServiceAndImplRetval();

        // 方法名去重
        String serviceMethodName = antiDuplicationService.getNewMethodNameIfExist(
                args.getServiceMethod().getNameAsString(), serviceRetval.getService());
        args.getServiceMethod().setName(serviceMethodName);

        // 将方法 添加到 Service
        MethodDeclaration serviceMethodImpl = args.getServiceMethod();
        MethodDeclaration serviceMethod = new MethodDeclaration().setType(serviceMethodImpl.getType())
                .setName(serviceMethodImpl.getName()).setParameters(serviceMethodImpl.getParameters());
        serviceMethod.setBody(null);
        ClassOrInterfaceDeclaration service = serviceRetval.getService();
        service.addMember(serviceMethod);
        log.info("Method [{}] append to Service [{}]", serviceMethod.getName(), service.getName());

        // 将方法 添加到 每个ServiceImpl
        ClassOrInterfaceDeclaration serviceImpl = serviceRetval.getServiceImpl();
        serviceImpl.addMember(serviceMethodImpl.clone());
        log.info("Method [{}] append to Service Impl [{}]", serviceMethodImpl.getName(), serviceImpl.getName());

        importExprService.extractQualifiedTypeToImport(serviceRetval.getServiceCu());
        importExprService.extractQualifiedTypeToImport(serviceRetval.getServiceImplCu());

        AddMethodToServiceRetval result = new AddMethodToServiceRetval();
        result.setMethodName(serviceMethod.getNameAsString());
        result.getFlushes().add(FileFlush.build(serviceRetval.getServiceCu()));
        result.getFlushes().add(FileFlush.build(serviceRetval.getServiceImplCu()));
        return result;
    }

    @Override
    public GenerateServiceAndImplRetval generateServiceAndImpl(GenerateServiceAndImplArgs args) {
        String serviceName =
                MoreStringUtils.toUpperCamel(args.getInitDecAnalysisDTO().getMvcHandlerMethodName()) + "Service";

        Path sourceRoot = AstForestContext.get().getSourceRoot();
        CompilationUnit serviceCu = new CompilationUnit();
        serviceCu.setPackageDeclaration(commonConfig.getServicePackage());
        importExprService.copyImports(args.getControllerCu(), serviceCu);
        ClassOrInterfaceDeclaration service = new ClassOrInterfaceDeclaration();
        String comment = concatServiceDescription(args.getInitDecAnalysisDTO());
        JavadocUtils.setJavadoc(service, comment, commonConfig.getAuthor());
        service.setPublic(true).setStatic(false).setInterface(true).setName(serviceName);
        Path absolutePath = CodeGenerationUtils.fileInPackageAbsolutePath(sourceRoot, commonConfig.getServicePackage(),
                service.getName() + ".java");

        // anti-duplication
        absolutePath = antiDuplicationService.getNewPathIfExist(absolutePath);
        serviceName = FilenameUtils.getBaseName(absolutePath.toString());
        service.setName(serviceName);

        serviceCu.setTypes(new NodeList<>(service));
        serviceCu.setStorage(absolutePath);
        log.info("generate Service [{}]", service.getName());

        CompilationUnit serviceImplCu = new CompilationUnit();
        serviceImplCu.setPackageDeclaration(commonConfig.getServiceImplPackage());
        importExprService.copyImports(args.getControllerCu(), serviceImplCu);
        ClassOrInterfaceDeclaration serviceImpl = new ClassOrInterfaceDeclaration();
        JavadocUtils.setJavadoc(serviceImpl, comment, commonConfig.getAuthor());
        serviceImpl.addAnnotation(annotationExprService.lombokSlf4J());
        serviceImpl.addAnnotation(annotationExprService.springService());
        String serviceImplName = service.getNameAsString() + "Impl";
        serviceImpl.setPublic(true).setStatic(false).setInterface(false).setName(serviceImplName).addImplementedType(
                service.getFullyQualifiedName().orElseThrow(
                        () -> new Allison1875Exception("Node '" + service.getName() + "' has no Qualifier")));
        serviceImplCu.setTypes(new NodeList<>(serviceImpl));
        absolutePath = CodeGenerationUtils.fileInPackageAbsolutePath(sourceRoot, commonConfig.getServiceImplPackage(),
                serviceImpl.getName() + ".java");

        // anti-duplication
        absolutePath = antiDuplicationService.getNewPathIfExist(absolutePath);
        serviceImpl.setName(FilenameUtils.getBaseName(absolutePath.toString()));

        serviceImplCu.setStorage(absolutePath);
        log.info("generate ServiceImpl [{}]", serviceImpl.getName());

        GenerateServiceAndImplRetval result = new GenerateServiceAndImplRetval();
        result.setService(service);
        result.setServiceCu(serviceCu);
        result.setServiceImpl(serviceImpl);
        result.setServiceImplCu(serviceImplCu);
        result.setServiceVarName(MoreStringUtils.toLowerCamel(service.getNameAsString()));
        result.setServiceQualifier(commonConfig.getServicePackage() + "." + serviceName);
        return result;
    }

    private String concatServiceDescription(InitDecAnalysisDTO initDecAnalysis) {
        String result = "";
        if (commonConfig.getEnableLotNoAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION
                    + initDecAnalysis.getLotNo();
        }
        return result;
    }

}