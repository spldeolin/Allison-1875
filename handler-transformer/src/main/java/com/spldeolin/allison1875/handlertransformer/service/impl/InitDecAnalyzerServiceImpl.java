package com.spldeolin.allison1875.handlertransformer.service.impl;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.util.HashingUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.handlertransformer.javabean.InitDecAnalysisDTO;
import com.spldeolin.allison1875.handlertransformer.service.InitDecAnalyzerService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-01-23
 */
@Singleton
@Slf4j
public class InitDecAnalyzerServiceImpl implements InitDecAnalyzerService {

    @Override
    public InitDecAnalysisDTO analyzeInitDec(CompilationUnit mvcControllerCu, ClassOrInterfaceDeclaration mvcController,
            InitializerDeclaration initDec) {
        InitDecAnalysisDTO result = new InitDecAnalysisDTO();
        result.setInitDec(initDec);
        for (Statement stmt : initDec.getBody().getStatements()) {
            stmt.ifExpressionStmt(exprStmt -> exprStmt.getExpression().ifVariableDeclarationExpr(vde -> {
                for (VariableDeclarator vd : vde.getVariables()) {
                    if (vd.getInitializer().isPresent()) {
                        Expression i = vd.getInitializer().get();
                        if (StringUtils.equalsAny(vd.getNameAsString(), "handler", "h")) {
                            if (i.isStringLiteralExpr()) {
                                if (result.getMvcHandlerUrl() == null) {
                                    result.setMvcHandlerUrl(i.asStringLiteralExpr().getValue());
                                } else {
                                    log.warn("'handler' [{}] duplicate declaration, ignore.", i);
                                }
                            } else {
                                log.warn("'handler' [{}] is not String Literal, ignore.", i);
                            }
                        }
                        if (StringUtils.equalsAny(vd.getNameAsString(), "desc", "d")) {
                            if (i.isStringLiteralExpr()) {
                                if (result.getMvcHandlerDescription() == null) {
                                    result.setMvcHandlerDescription(i.asStringLiteralExpr().getValue());
                                } else {
                                    log.warn("'handler' [{}] duplicate declaration, ignore.", i);
                                }
                            } else {
                                log.warn("'desc' [{}] is not String Literal, ignore.", i);
                            }
                        }
                    }
                }
            }));
        }
        if (StringUtils.isBlank(result.getMvcHandlerUrl())) {
            log.warn("'handler' [{}] is blank, ignore", result.getMvcHandlerUrl());
            return null;
        }
        if (StringUtils.isBlank(result.getMvcHandlerDescription())) {
            result.setMvcHandlerDescription("未指定描述");
        }
        result.setMvcHandlerMethodName(MoreStringUtils.toLowerCamel(result.getMvcHandlerUrl()));
        result.setMvcControllerCu(mvcControllerCu);
        result.setMvcController(mvcController);
        String hash = StringUtils.upperCase(HashingUtils.hashString(result.toString()));
        result.setLotNo(String.format("HT%s-%s", Allison1875.SHORT_VERSION, hash));
        return result;
    }

}