package com.spldeolin.allison1875.handlertransformer.service.impl;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.builder.SingleMethodServiceCuBuilder;
import com.spldeolin.allison1875.handlertransformer.javabean.CreateServiceMethodHandleResult;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.ReqDtoRespDtoInfo;
import com.spldeolin.allison1875.handlertransformer.service.CreateServiceMethodService;
import com.spldeolin.allison1875.handlertransformer.service.ServiceService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-01-12
 */
@Singleton
@Log4j2
public class ServiceServiceImpl implements ServiceService {

    @Inject
    private HandlerTransformerConfig handlerTransformerConfig;

    @Inject
    private CreateServiceMethodService createServiceMethodService;

    @Override
    public SingleMethodServiceCuBuilder generateServiceWithImpl(CompilationUnit cu, FirstLineDto firstLineDto,
            ReqDtoRespDtoInfo reqDtoRespDtoInfo) {
        SingleMethodServiceCuBuilder serviceBuilder = new SingleMethodServiceCuBuilder();
        serviceBuilder.sourceRoot(Locations.getStorage(cu).getSourceRoot());
        serviceBuilder.servicePackageDeclaration(handlerTransformerConfig.getServicePackage());
        serviceBuilder.implPackageDeclaration(handlerTransformerConfig.getServiceImplPackage());
        serviceBuilder.importDeclarations(cu.getImports());
        List<String> imports = Lists.newArrayList("java.util.Collection",
                handlerTransformerConfig.getPageTypeQualifier());
        if (reqDtoRespDtoInfo.getReqDtoQualifier() != null) {
            imports.add(reqDtoRespDtoInfo.getReqDtoQualifier());
        }
        if (reqDtoRespDtoInfo.getRespDtoQualifier() != null) {
            imports.add(reqDtoRespDtoInfo.getRespDtoQualifier());
        }
        serviceBuilder.importDeclarationsString(imports);
        serviceBuilder.javadoc(null, handlerTransformerConfig.getAuthor());
        serviceBuilder.serviceName(MoreStringUtils.upperFirstLetter(firstLineDto.getHandlerName()) + "Service");

        // 使用handle创建service实现方法
        CreateServiceMethodHandleResult creation = createServiceMethodService.createMethodImpl(firstLineDto,
                reqDtoRespDtoInfo.getParamType(), reqDtoRespDtoInfo.getResultType());
        serviceBuilder.method(creation.getServiceMethod());
        serviceBuilder.importDeclarationsString(creation.getAppendImports());

        Saves.add(serviceBuilder.buildService());
        log.info("generate Service [{}].", serviceBuilder.getService().getNameAsString());

        return serviceBuilder;
    }

}