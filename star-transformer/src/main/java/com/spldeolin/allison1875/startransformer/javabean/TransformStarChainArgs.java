package com.spldeolin.allison1875.startransformer.javabean;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-02-13
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransformStarChainArgs {

    BlockStmt block;

    ChainAnalysisDTO analysis;

    MethodCallExpr starChain;

    JavabeanGeneration wholeDTOGeneration;

}