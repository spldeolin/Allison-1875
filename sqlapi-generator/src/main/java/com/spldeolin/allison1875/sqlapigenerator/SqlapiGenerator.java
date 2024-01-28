package com.spldeolin.allison1875.sqlapigenerator;

import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.sqlapigenerator.javabean.CoidsOnTrackDto;
import com.spldeolin.allison1875.sqlapigenerator.javabean.ControllerMethodGenerationDto;
import com.spldeolin.allison1875.sqlapigenerator.javabean.MapperMethodGenerationDto;
import com.spldeolin.allison1875.sqlapigenerator.javabean.ServiceMethodGenerationDto;
import com.spldeolin.allison1875.sqlapigenerator.service.AddAutowiredService;
import com.spldeolin.allison1875.sqlapigenerator.service.AddMethodService;
import com.spldeolin.allison1875.sqlapigenerator.service.GenerateMethodService;
import com.spldeolin.allison1875.sqlapigenerator.service.ListCoidsOnTrackService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-01-20
 */
@Slf4j
public class SqlapiGenerator implements Allison1875MainService {

    @Inject
    private ListCoidsOnTrackService listCoidsOnTrackService;

    @Inject
    private GenerateMethodService generateMethodService;

    @Inject
    private AddAutowiredService addAutowiredService;

    @Inject
    private AddMethodService addMethodService;

    @Override
    public void process(AstForest astForest) {
        List<FileFlush> flushes = Lists.newArrayList();

        CoidsOnTrackDto coidsOnTrack = listCoidsOnTrackService.listCoidsOnTrack(astForest);
        log.info("list coidsOnTrack={}", coidsOnTrack);

        // 生成Mapper方法，以及方法签名所需的Javabean
        MapperMethodGenerationDto mapperMethodGeneration = generateMethodService.generateMapperMethod(coidsOnTrack);
        flushes.addAll(mapperMethodGeneration.getFlushes());

        // 将Mapper方法追加到Mapper
        LexicalPreservingPrinter.setup(coidsOnTrack.getMapperCu());
        coidsOnTrack.getMapperCu().addImport(mapperMethodGeneration.getParamTypeQualifier());
        coidsOnTrack.getMapperCu().addImport(mapperMethodGeneration.getResultTypeQualifier());
        addMethodService.addMethodToCoid(mapperMethodGeneration.getMethod(), coidsOnTrack.getMapper());
        flushes.add(FileFlush.buildLexicalPreserving(coidsOnTrack.getMapperCu()));

        // 生成Mapper xml方法
        List<String> xmlMethodCodeLines = generateMethodService.generateMapperXmlMethod(mapperMethodGeneration);

        // 将Mapper xml方法追加到Mapper xml
        FileFlush xmlFlush = addMethodService.addMethodToXml(xmlMethodCodeLines);
        flushes.add(xmlFlush);

        if (coidsOnTrack.getService() != null) {
            // 生成Service方法，以及方法签名所需的Javabean
            ServiceMethodGenerationDto serviceMethodGeneration = generateMethodService.generateServiceMethod(
                    coidsOnTrack);
            flushes.addAll(serviceMethodGeneration.getFlushes());

            // 将Service方法追加到Service
            LexicalPreservingPrinter.setup(coidsOnTrack.getServiceCu());
            addMethodService.addMethodToCoid(serviceMethodGeneration.getMethod(), coidsOnTrack.getService());
            for (ClassOrInterfaceDeclaration serviceImpl : coidsOnTrack.getServiceImpls()) {
                addAutowiredService.ensureAuwired(coidsOnTrack.getMapper(), serviceImpl);
                addMethodService.addMethodToCoid(serviceMethodGeneration.getMethodImpl(), serviceImpl);
            }
            flushes.add(FileFlush.buildLexicalPreserving(coidsOnTrack.getServiceCu()));

            if (coidsOnTrack.getController() != null) {
                // 生成Controller方法，以及方法签名所需的Javabean
                ControllerMethodGenerationDto controllerMethodGeneration =
                        generateMethodService.generateControllerMethod(
                        coidsOnTrack);
                flushes.add(controllerMethodGeneration.getFlush());

                // 将Controller方法追加到Controller
                LexicalPreservingPrinter.setup(coidsOnTrack.getControllerCu());
                addAutowiredService.ensureAuwired(coidsOnTrack.getService(), coidsOnTrack.getController());
                addMethodService.addMethodToCoid(controllerMethodGeneration.getMethod(), coidsOnTrack.getController());
                flushes.add(FileFlush.buildLexicalPreserving(coidsOnTrack.getControllerCu()));
            }
        }

        // flush
        if (CollectionUtils.isNotEmpty(flushes)) {
            flushes.forEach(FileFlush::flush);
            log.info("# REMEBER REFORMAT CODE #");
        } else {
            log.warn("nothing happened");
        }
    }

}