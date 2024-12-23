package com.spldeolin.allison1875.querytransformer.dto;

import java.util.Set;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.persistencegenerator.facade.constant.KeywordConstant;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.PropertyDTO;
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
public class ChainAnalysisDTO {

    String entityQualifier;

    MethodCallExpr chain;

    String methodName;

    KeywordConstant.ChainInitialMethod chainInitialMethod;

    ReturnShapeEnum returnShape;

    Set<PropertyDTO> selectProperties = Sets.newLinkedHashSet();

    Set<SearchConditionDTO> searchConditions = Sets.newLinkedHashSet();

    Set<SortPropertyDTO> sortProperties = Sets.newLinkedHashSet();

    Set<JoinClauseDTO> joinClauses = Sets.newLinkedHashSet();

    Set<AssignmentDTO> assignments = Sets.newLinkedHashSet();

    /**
     * argument作为实际参数输入的binary，用于构建mapper方法的参数列表，由以下元素组成：
     * - 所有assigments
     * - 需要argument的searchConditions
     * - 需要argument不是主表PropertyName的joinConditions
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