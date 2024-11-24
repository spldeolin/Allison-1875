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

    /**
     * argument作为实际参数输入的binary，用于构建mapper方法的参数列表，由以下元素组成：
     * - 所有assigments
     * - 需要argument的searchConditions
     * - 需要非EntityKey的argument的joinConditions
     */
    Set<Binary> binariesAsArgs;

    /**
     * propertyName作为返回类型字段输出的property与其varName，用于构建mapper方法的返回值，由以下元素组成：
     * - 所有selectProperties
     * - 所有joinClause的所有joinedProperty
     */
    Set<VariableProperty> propertiesAsResult;

    BlockStmt directBlock;

    Boolean isAssigned;

    Boolean isAssignedToType;

    Boolean isByForced;

    String lotNo;

}