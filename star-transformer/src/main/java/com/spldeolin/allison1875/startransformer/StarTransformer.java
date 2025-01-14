package com.spldeolin.allison1875.startransformer;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
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
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.startransformer.exception.IllegalChainException;
import com.spldeolin.allison1875.startransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.startransformer.javabean.TransformStarChainArgs;
import com.spldeolin.allison1875.startransformer.service.StarChainService;
import com.spldeolin.allison1875.startransformer.service.StarChainTransformerService;
import com.spldeolin.allison1875.startransformer.service.WholeDtoService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2023-05-05
 */
@Singleton
@Slf4j
public class StarTransformer implements Allison1875MainService {

    @Inject
    private StarChainService starChainService;

    @Inject
    private WholeDtoService wholeDtoService;

    @Inject
    private StarChainTransformerService starChainTransformerService;

    @Inject
    private ImportExprService importExprService;

    @Override
    public void process(AstForest astForest) {
        List<FileFlush> flushes = Lists.newArrayList();

        for (CompilationUnit cu : astForest) {
            boolean anyTransformed = false;
            LexicalPreservingPrinter.setup(cu);

            for (BlockStmt block : cu.findAll(BlockStmt.class)) {
                for (MethodCallExpr starChain : starChainService.detectStarChains(block)) {

                    // analyze chain
                    ChainAnalysisDto analysis;
                    try {
                        analysis = starChainService.analyzeStarChain(starChain, astForest);
                        log.info("Star Chain analyzed, analysis={}", analysis);
                    } catch (IllegalChainException e) {
                        log.error("fail to analyze Star Chain, starChain={}", starChain, e);
                        continue;
                    }

                    // generate WholeDto
                    JavabeanGeneration wholeDtoGeneration;
                    try {
                        wholeDtoGeneration = wholeDtoService.generateWholeDto(astForest, analysis);
                        log.info("Whole DTO generated, name={} path={}", wholeDtoGeneration.getJavabeanName(),
                                wholeDtoGeneration.getPath());
                    } catch (Exception e) {
                        log.error("fail to generate Whole DTO, analysis={}", analysis, e);
                        continue;
                    }
                    flushes.add(wholeDtoGeneration.getFileFlush());

                    // transform Query Chain and replace Star Chain
                    TransformStarChainArgs args = new TransformStarChainArgs();
                    args.setBlock(block);
                    args.setAnalysis(analysis);
                    args.setStarChain(starChain);
                    args.setWholeDtoGeneration(wholeDtoGeneration);
                    try {
                        starChainTransformerService.transformStarChain(args);
                        log.info("Star Chain transformed");
                    } catch (Exception e) {
                        log.error("fail to transformStarChain Star Chain, starAnalysis={}", analysis, e);
                    }

                    importExprService.extractQualifiedTypeToImport(cu);
                    anyTransformed = true;
                }
            }
            if (anyTransformed) {
                flushes.add(FileFlush.buildLexicalPreserving(cu));
            }
        }

        // flush
        if (CollectionUtils.isNotEmpty(flushes)) {
            flushes.forEach(FileFlush::flush);
            log.info(BaseConstant.REMEMBER_REFORMAT_CODE_ANNOUNCE);
        } else {
            log.warn("no valid Chain transformed");
        }
    }

}