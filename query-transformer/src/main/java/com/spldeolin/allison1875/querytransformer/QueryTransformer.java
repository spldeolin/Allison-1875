package com.spldeolin.allison1875.querytransformer;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node.TreeTraversal;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.exception.IllegalChainException;
import com.spldeolin.allison1875.querytransformer.exception.IllegalDesignException;
import com.spldeolin.allison1875.querytransformer.exception.SameNameTerminationMethodException;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParamGenerationDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultGenerationDto;
import com.spldeolin.allison1875.querytransformer.service.AnalyzeChainService;
import com.spldeolin.allison1875.querytransformer.service.AppendAutowiredMapperService;
import com.spldeolin.allison1875.querytransformer.service.DesignService;
import com.spldeolin.allison1875.querytransformer.service.DetectQueryChainService;
import com.spldeolin.allison1875.querytransformer.service.GenerateMethodIntoMapperService;
import com.spldeolin.allison1875.querytransformer.service.GenerateMethodXmlService;
import com.spldeolin.allison1875.querytransformer.service.GenerateParamService;
import com.spldeolin.allison1875.querytransformer.service.GenerateResultService;
import com.spldeolin.allison1875.querytransformer.service.ReplaceDesignService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-10-06
 */
@Singleton
@Slf4j
public class QueryTransformer implements Allison1875MainService {

    @Inject
    private DetectQueryChainService detectQueryChainService;

    @Inject
    private AnalyzeChainService analyzeChainService;

    @Inject
    private GenerateMethodXmlService generateMethodXmlService;

    @Inject
    private GenerateMethodIntoMapperService generateMethodIntoMapperService;

    @Inject
    private GenerateParamService generateParameterService;

    @Inject
    private GenerateResultService transformResultService;

    @Inject
    private ReplaceDesignService replaceDesignService;

    @Inject
    private AppendAutowiredMapperService appendAutowiredMapperService;

    @Inject
    private DesignService designService;

    @Inject
    private ImportExprService importExprService;

    @Override
    public void process(AstForest astForest) {
        List<FileFlush> flushes = Lists.newArrayList();

        for (CompilationUnit cu : astForest) {
            boolean anyTransformed = false;
            LexicalPreservingPrinter.setup(cu);

            if (CollectionUtils.isEmpty(cu.findAll(BlockStmt.class))) {
                continue;
            }
            for (BlockStmt directBlock : cu.findAll(BlockStmt.class, TreeTraversal.POSTORDER)) {
                for (MethodCallExpr queryChain : detectQueryChainService.detect(directBlock)) {

                    ClassOrInterfaceDeclaration design;
                    DesignMeta designMeta;
                    try {
                        design = designService.findDesign(astForest, queryChain);
                        designMeta = designService.parseDesignMeta(design);
                        log.info("Design found and Design Meta parsed, designName={} designMeta={}", design.getName(),
                                designMeta);
                    } catch (IllegalDesignException e) {
                        log.error("fail to find Design or parse Design Meta, queryChain={}", queryChain, e);
                        continue;
                    } catch (SameNameTerminationMethodException e) {
                        continue;
                    }

                    // analyze queryChain
                    ChainAnalysisDto analysis;
                    try {
                        analysis = analyzeChainService.analyze(queryChain, design, designMeta);
                        log.info("Query Chain analyzed, analysis={}", analysis);
                    } catch (IllegalChainException e) {
                        log.error("fail to analyze Query Chain, queryChain={}", queryChain, e);
                        continue;
                    }
                    analysis.setDirectBlock(directBlock);

                    // generate Parameter
                    ParamGenerationDto paramGeneration;
                    try {
                        paramGeneration = generateParameterService.generate(analysis, designMeta, astForest);
                        log.info("Param generated, generation={}", paramGeneration);
                    } catch (Exception e) {
                        log.error("fail to generate param analysis={} designMeta={}", analysis, designMeta, e);
                        continue;
                    }
                    if (paramGeneration.getCondFlush() != null) {
                        flushes.add(paramGeneration.getCondFlush());
                    }

                    // generate Result Type
                    ResultGenerationDto resultGeneration;
                    try {
                        resultGeneration = transformResultService.generate(analysis, designMeta, astForest);
                        log.info("Result generated, generation={}", resultGeneration);
                    } catch (Exception e) {
                        log.error("fail to generate result analysis={} designMeta={}", analysis, designMeta, e);
                        continue;
                    }
                    if (resultGeneration.getFlush() != null) {
                        flushes.add(resultGeneration.getFlush());
                    }

                    // generate Method into Mapper
                    generateMethodIntoMapperService.generate(astForest, designMeta, analysis, paramGeneration,
                            resultGeneration).ifPresent(flushes::add);

                    // generate Method into mapper.xml
                    List<FileFlush> xmlFlushes = generateMethodXmlService.generate(astForest, designMeta, analysis,
                            paramGeneration, resultGeneration);
                    flushes.addAll(xmlFlushes);

                    // append autowired mapper
                    appendAutowiredMapperService.append(queryChain, designMeta);

                    // transform Query Design
                    replaceDesignService.replace(designMeta, analysis, paramGeneration, resultGeneration);

                    anyTransformed = true;
                }
            }
            if (anyTransformed) {
                importExprService.extractQualifiedTypeToImport(cu);
                flushes.add(FileFlush.buildLexicalPreserving(cu));
            }
        }

        // write all to file
        if (CollectionUtils.isNotEmpty(flushes)) {
            flushes.forEach(FileFlush::flush);
            log.info(BaseConstant.REMEMBER_REFORMAT_CODE_ANNOUNCE);
        } else {
            log.warn("no valid Chain transformed");
        }
    }

}