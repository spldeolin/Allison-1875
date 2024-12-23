package com.spldeolin.allison1875.extension.satisficing.common;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.Type;
import com.spldeolin.allison1875.common.dto.GenerateMvcHandlerArgs;
import com.spldeolin.allison1875.common.dto.GenerateMvcHandlerRetval;
import com.spldeolin.allison1875.common.service.impl.MvcHandlerGeneratorServiceImpl;
import com.spldeolin.satisficing.api.RequestResult;

/**
 * @author Deolin 2024-02-17
 */
public class MvcHandlerGeneratorServiceImpl2 extends MvcHandlerGeneratorServiceImpl {

    @Override
    public GenerateMvcHandlerRetval generateMvcHandler(GenerateMvcHandlerArgs args) {
        GenerateMvcHandlerRetval result = super.generateMvcHandler(args);
        Type returnType = result.getMvcHandler().getType();
        if (returnType.isVoidType()) {

            returnType.replace(StaticJavaParser.parseType(RequestResult.class.getName() + "<Void>"));
            result.getMvcHandler().getBody().get().getStatements()
                    .add(StaticJavaParser.parseStatement("return RequestResult.success();"));
        } else {
            returnType.replace(StaticJavaParser.parseType(
                    String.format(RequestResult.class.getName() + "<%s>", returnType)));
            Expression returnExpr = result.getMvcHandler().getBody().get().getStatements().get(0).asReturnStmt()
                    .getExpression().get();
            returnExpr.replace(
                    StaticJavaParser.parseExpression(String.format("RequestResult.success(%s)", returnExpr)));
        }
        return result;
    }

}