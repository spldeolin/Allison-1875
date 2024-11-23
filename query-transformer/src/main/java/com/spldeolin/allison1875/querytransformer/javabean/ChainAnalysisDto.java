package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.Set;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.querytransformer.enums.ChainMethodEnum;
import com.spldeolin.allison1875.querytransformer.enums.ReturnShapeEnum;
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

    String entityQualifier;

    MethodCallExpr chain;

    String methodName;

    ChainMethodEnum chainMethod;

    ReturnShapeEnum returnShape;

    Set<PropertyDto> selectProperties = Sets.newLinkedHashSet();

    Set<SearchConditionDto> searchConditions = Sets.newLinkedHashSet();

    Set<SortPropertyDto> sortProperties = Sets.newLinkedHashSet();

    Set<JoinClauseDto> joinClauses = Sets.newLinkedHashSet();

    Set<AssignmentDto> assignments = Sets.newLinkedHashSet();

    BlockStmt directBlock;

    Boolean isAssigned;

    Boolean isAssignedToType;

    Boolean isByForced;

    String lotNo;

}