package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.Set;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMetaDto;
import com.spldeolin.allison1875.querytransformer.enums.ChainMethodEnum;
import com.spldeolin.allison1875.querytransformer.enums.ReturnClassifyEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-12-09
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChainAnalysisDto {

    DesignMetaDto designMeta;

    MethodCallExpr chain;

    String methodName;

    ChainMethodEnum chainMethod;

    ReturnClassifyEnum returnClassify;

    Set<PhraseDto> queryPhrases = Sets.newLinkedHashSet();

    Set<PhraseDto> byPhrases = Sets.newLinkedHashSet();

    Set<PhraseDto> orderPhrases = Sets.newLinkedHashSet();

    Set<JoinDto> joins = Sets.newLinkedHashSet();

    Set<PhraseDto> updatePhrases = Sets.newLinkedHashSet();

    BlockStmt directBlock;

    Boolean isAssigned;

    Boolean isAssignedToType;

    Boolean isByForced;

    String lotNo;

}