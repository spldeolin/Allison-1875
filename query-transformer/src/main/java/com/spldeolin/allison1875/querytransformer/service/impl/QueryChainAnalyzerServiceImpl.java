package com.spldeolin.allison1875.querytransformer.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.Node.TreeTraversal;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.HashingUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.constant.TokenWordConstant;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMetaDto;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.querytransformer.enums.ChainMethodEnum;
import com.spldeolin.allison1875.querytransformer.enums.ComparisonOperatorEnum;
import com.spldeolin.allison1875.querytransformer.enums.JoinTypeEnum;
import com.spldeolin.allison1875.querytransformer.enums.OrderSequenceEnum;
import com.spldeolin.allison1875.querytransformer.enums.ReturnShapeEnum;
import com.spldeolin.allison1875.querytransformer.exception.IllegalChainException;
import com.spldeolin.allison1875.querytransformer.exception.IllegalDesignException;
import com.spldeolin.allison1875.querytransformer.javabean.AssignmentDto;
import com.spldeolin.allison1875.querytransformer.javabean.Binary;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.JoinClauseDto;
import com.spldeolin.allison1875.querytransformer.javabean.JoinConditionDto;
import com.spldeolin.allison1875.querytransformer.javabean.JoinedPropertyDto;
import com.spldeolin.allison1875.querytransformer.javabean.SearchConditionDto;
import com.spldeolin.allison1875.querytransformer.javabean.SortPropertyDto;
import com.spldeolin.allison1875.querytransformer.javabean.VariableProperty;
import com.spldeolin.allison1875.querytransformer.service.DesignService;
import com.spldeolin.allison1875.querytransformer.service.QueryChainAnalyzerService;
import com.spldeolin.allison1875.support.ByChainPredicate;
import com.spldeolin.allison1875.support.EntityKey;
import com.spldeolin.allison1875.support.OnChainLink;
import com.spldeolin.allison1875.support.OrderChainPredicate;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-10-10
 */
@Singleton
@Slf4j
public class QueryChainAnalyzerServiceImpl implements QueryChainAnalyzerService {

    private static final Pattern matchJoinPropertyNames = Pattern.compile(
            "(left|right|inner|outer)Join\\(\\)\\.(.*?)\\.on\\(\\)");

    @Inject
    private AntiDuplicationService antiDuplicationService;

    @Inject
    private DesignService designService;

    @Inject
    private CommonConfig commonConfig;

    @Override
    public ChainAnalysisDto analyzeQueryChain(MethodCallExpr queryChain, DesignMetaDto designMeta, AstForest astForest)
            throws IllegalChainException {
        String chainCode = queryChain.toString();
        String betweenCode = chainCode.substring(chainCode.indexOf(".") + 1, chainCode.lastIndexOf("."));
        String designQualifier = designMeta.getDesignQualifier();

        ChainMethodEnum chainMethod;
        if (betweenCode.startsWith("query(")) {
            chainMethod = ChainMethodEnum.query;
        } else if (betweenCode.startsWith("update(")) {
            chainMethod = ChainMethodEnum.update;
        } else if (betweenCode.startsWith("drop(")) {
            chainMethod = ChainMethodEnum.drop;
        } else {
            throw new IllegalChainException("chainMethod is none of query, update or drop");
        }

        String methodName = this.analyzeSpecifiedMethodName(chainMethod, queryChain, designMeta);

        ReturnShapeEnum returnShape;
        String keyPropertyName = null;
        if (queryChain.getNameAsString().equals("one")) {
            returnShape = ReturnShapeEnum.one;
        } else if (queryChain.getNameAsString().equals("many")) {
            if (CollectionUtils.isEmpty(queryChain.getArguments())) {
                returnShape = ReturnShapeEnum.many;
            } else if (queryChain.getArgument(0).asFieldAccessExpr().getScope().toString().equals("Each")) {
                returnShape = ReturnShapeEnum.each;
                keyPropertyName = queryChain.getArgument(0).asFieldAccessExpr().getNameAsString();
            } else if (queryChain.getArgument(0).asFieldAccessExpr().getScope().toString().equals("MultiEach")) {
                returnShape = ReturnShapeEnum.multiEach;
                keyPropertyName = queryChain.getArgument(0).asFieldAccessExpr().getNameAsString();
            } else {
                throw new IllegalChainException("many() argument is none of each nor multiEach");
            }
        } else if (queryChain.getNameAsString().equals("count")) {
            returnShape = ReturnShapeEnum.count;
        } else {
            returnShape = null;
        }
        log.info("chainMethod={} returnShape={}", chainMethod, returnShape);

        Set<PropertyDto> selectProperties = Sets.newLinkedHashSet();
        Set<SearchConditionDto> searchConditions = Sets.newLinkedHashSet();
        Set<SortPropertyDto> sortProperties = Sets.newLinkedHashSet();
        Set<JoinClauseDto> joinClauses = Sets.newLinkedHashSet();
        Set<AssignmentDto> assignments = Sets.newLinkedHashSet();
        Map<String/*joinedEntityDesignQualifier*/, Set<JoinConditionDto>> joinConditions = Maps.newHashMap();

        // 防Cond中的字段名重复（分析where和update中使用）
        List<String> antiVarNameDuplInCond = Lists.newArrayList();
        // 防Record中的字段名重复（分析select col和joined col中使用）
        List<String> antiVarNameDuplInRecord = Lists.newArrayList();

        ClassOrInterfaceDeclaration joinDesign = designService.detectDesignOrJoinDesign(astForest,
                commonConfig.getDesignPackage() + ".JoinDesign");
        List<String> propertyNameFromJoinDesign = joinDesign.getMembers().stream()
                .filter(BodyDeclaration::isClassOrInterfaceDeclaration)
                .map(BodyDeclaration::asClassOrInterfaceDeclaration).flatMap(coid -> coid.getFields().stream())
                .map(fd -> fd.getVariable(0).getNameAsString()).distinct().collect(Collectors.toList());

        for (FieldAccessExpr fae : queryChain.findAll(FieldAccessExpr.class, TreeTraversal.POSTORDER)) {
            if (!designMeta.getProperties().containsKey(fae.getNameAsString()) && !propertyNameFromJoinDesign.contains(
                    fae.getNameAsString())) {
                // 例如：XxxxDesign.query("xx").by().privilegeCode.in(Lists.newArrayList(OneTypeEnum.FIRST.getCode()))
                // .many();，其中的OneTypeEnum.FIRST应当被跳过
                continue;
            }

            String describe;
            try {
                describe = fae.calculateResolvedType().describe();
            } catch (Exception e) {
                log.warn("fail to resolve, fae={}", fae);
                continue;
            }

            // 对应SELECT子句中的col_name
            if (describe.startsWith(designQualifier + ".QueryChain")) {
                selectProperties.add(designMeta.getProperties().get(fae.getNameAsString()));
                antiVarNameDuplInRecord.add(fae.getNameAsString());
            }

            // 对应WHERE子句中的binary
            if (describe.startsWith(ByChainPredicate.class.getName() + "<" + designQualifier + ".NextableByChainReturn")
                    && fae.getParentNode().isPresent()) {
                MethodCallExpr parent = (MethodCallExpr) fae.getParentNode().get();
                ComparisonOperatorEnum predicate = ComparisonOperatorEnum.of(parent.getNameAsString());
                SearchConditionDto searchCond = new SearchConditionDto();
                searchCond.setProperty(designMeta.getProperties().get(fae.getNameAsString()));
                searchCond.setVarName(fae.getNameAsString());
                searchCond.setComparisonOperator(predicate);
                if (CollectionUtils.isNotEmpty(parent.getArguments())) {
                    String varName = antiDuplicationService.getNewElementIfExist(fae.getNameAsString(),
                            antiVarNameDuplInCond);
                    antiVarNameDuplInCond.add(varName);
                    searchCond.setVarName(varName);
                    searchCond.setArgument(parent.getArgument(0));
                }
                searchConditions.add(searchCond);
            }

            // 对应ORDER BY子句中的col_name和ASC / DESC
            if (describe.startsWith(OrderChainPredicate.class.getName())) {
                MethodCallExpr parent = (MethodCallExpr) fae.getParentNode().get();
                OrderSequenceEnum predicate = OrderSequenceEnum.of(parent.getNameAsString());
                SortPropertyDto phrase = new SortPropertyDto();
                phrase.setPropertyName(fae.getNameAsString());
                phrase.setOrderSequence(predicate);
                sortProperties.add(phrase);
            }

            // 对应每个JOIN子句的ON子句的每个binary
            if (describe.startsWith(
                    OnChainLink.class.getName() + "<" + commonConfig.getDesignPackage() + ".JoinDesign.Join")
                    && fae.getParentNode().isPresent()) {
                MethodCallExpr parent = (MethodCallExpr) fae.getParentNode().get();
                ComparisonOperatorEnum predicate = ComparisonOperatorEnum.of(parent.getNameAsString());
                JoinConditionDto joinCond = new JoinConditionDto();
                // 这里以joinedDesignQualifier作为联系，便不再获取JoinedDesignMeta了，没有后者也就无法获取到PropertyDto，所以只能先行setPropertyName
                joinCond.setProperty(new PropertyDto().setPropertyName(fae.getNameAsString()));
                joinCond.setVarName(fae.getNameAsString());
                joinCond.setComparisonOperator(predicate);
                if (CollectionUtils.isNotEmpty(parent.getArguments())) {
                    if (!parent.getArgument(0).calculateResolvedType().describe()
                            .startsWith(EntityKey.class.getName() + "<")) {
                        String varName = antiDuplicationService.getNewElementIfExist(fae.getNameAsString(),
                                antiVarNameDuplInCond);
                        antiVarNameDuplInCond.add(varName);
                        joinCond.setVarName(varName);
                        joinCond.setArgument(parent.getArgument(0));
                    } else {
                        // 说明是MyEntityDesign.myProperty，无需anti-dupl，但需要在onPhrase记录propertyName4Comparing，应该不能用PhraseDTO了
                        joinCond.setComparedProperty(new PropertyDto().setPropertyName(
                                parent.getArgument(0).asFieldAccessExpr().getNameAsString()));
                    }
                }
                String designMarkerQualifier = fae.resolve().asField().declaringType().asClass().getAllInterfaces()
                        .get(0).describe();
                String joinedDesignQualifier = MoreStringUtils.splitAndGetLastPart(designMarkerQualifier, ".")
                        .replace('_', '.');
                joinConditions.computeIfAbsent(joinedDesignQualifier, v -> Sets.newLinkedHashSet()).add(joinCond);
            }
        }

        // JOIN部分
        Matcher matcher = matchJoinPropertyNames.matcher(chainCode);
        while (matcher.find()) {
            // 对应JOIN子句的tbl_name
            String joinedEntityWithProperty = matcher.group(2);
            String entityName = joinedEntityWithProperty.split("\\.")[0];
            ClassOrInterfaceDeclaration joinedDesign = designService.detectDesignOrJoinDesign(astForest,
                    this.getJoinedDesignQualifier(joinDesign, entityName));
            DesignMetaDto joinedDesignMeta = designService.findDesignMeta(joinedDesign);

            // 对应SELECT子句中的col_name（join表的col）
            Set<JoinedPropertyDto> joinedProperties = Sets.newLinkedHashSet();
            Stream<String> joinedPropertyNames = this.extractPropertyNames(joinedEntityWithProperty, joinedDesignMeta);
            joinedPropertyNames.forEach(joinedPropertyName -> {
                String varName = StringUtils.uncapitalize(entityName) + StringUtils.capitalize(joinedPropertyName);
                varName = antiDuplicationService.getNewElementIfExist(varName, antiVarNameDuplInRecord);
                antiVarNameDuplInRecord.add(varName);
                JoinedPropertyDto joinedProperty = new JoinedPropertyDto();
                joinedProperty.setProperty(joinedDesignMeta.getProperties().get(joinedPropertyName));
                joinedProperty.setVarName(varName);
                joinedProperties.add(joinedProperty);
            });

            // 对应每个JOIN子句的ON子句的每个binary
            Set<JoinConditionDto> joinConds = joinConditions.get(joinedDesignMeta.getDesignQualifier());
            for (JoinConditionDto joinCond : joinConds) {
                joinCond.setProperty(joinedDesignMeta.getProperties().get(joinCond.getProperty().getPropertyName()));
                if (joinCond.getComparedProperty() != null) {
                    joinCond.setComparedProperty(
                            designMeta.getProperties().get(joinCond.getComparedProperty().getPropertyName()));
                }
            }

            JoinClauseDto joinClause = new JoinClauseDto();
            joinClause.setJoinType(JoinTypeEnum.of(matcher.group(1)));
            joinClause.setJoinedDesignMeta(joinedDesignMeta);
            joinClause.setJoinedProperties(joinedProperties);
            joinClause.setJoinConditions(joinConds);
            joinClauses.add(joinClause);
        }

        // 如果终结方法是Each或者MultiEach，确保queryPhrases中必须包含each的key
        if (keyPropertyName != null && CollectionUtils.isNotEmpty(selectProperties) && selectProperties.stream()
                .map(PropertyDto::getPropertyName).collect(Collectors.toList()).contains(keyPropertyName)) {
            log.warn("Each or MultiEach Key [{}] is not declared in Query Phrases [{}], auto add in", keyPropertyName,
                    selectProperties);
            selectProperties.add(designMeta.getProperties().get(keyPropertyName));
        }

        // update set assignment
        for (MethodCallExpr mce : queryChain.findAll(MethodCallExpr.class, TreeTraversal.POSTORDER)) {
            String describe;
            try {
                describe = mce.calculateResolvedType().describe();
            } catch (Exception e) {
                log.warn("fail to resolve, mce={}", mce);
                continue;
            }
            if (describe.startsWith(designQualifier + ".NextableUpdateChain")) {
                AssignmentDto assignment = new AssignmentDto();
                assignment.setProperty(designMeta.getProperties().get(mce.getNameAsString()));
                String varName = antiDuplicationService.getNewElementIfExist(mce.getNameAsString(),
                        antiVarNameDuplInCond);
                antiVarNameDuplInCond.add(varName);
                assignment.setVarName(varName);
                assignment.setArgument(mce.getArgument(0));
                assignments.add(assignment);
            }
        }

        // mapper方法的参数字段
        Set<Binary> binaries = Sets.newLinkedHashSet(assignments);
        for (SearchConditionDto searchCond : searchConditions) {
            if (searchCond.getArgument() != null) {
                binaries.add(searchCond);
            }
        }
        for (JoinClauseDto joinClause : joinClauses) {
            for (JoinConditionDto joinCond : joinClause.getJoinConditions()) {
                if (joinCond.getArgument() != null) {
                    binaries.add(joinCond);
                }
            }
        }

        // mapper方法的返回值字段
        Set<VariableProperty> returnProps = Sets.newLinkedHashSet();
        for (PropertyDto selectProp : selectProperties) {
            returnProps.add(new VariableProperty() {
                @Override
                public PropertyDto getProperty() {
                    return selectProp;
                }

                @Override
                public String getVarName() {
                    return selectProp.getPropertyName();
                }
            });
        }
        for (JoinClauseDto joinClause : joinClauses) {
            returnProps.addAll(joinClause.getJoinedProperties());
        }

        log.info("selectProperties={}", JsonUtils.toJsonPrettily(selectProperties));
        log.info("searchConditions={}", JsonUtils.toJsonPrettily(searchConditions));
        log.info("sortProperties={}", JsonUtils.toJsonPrettily(sortProperties));
        log.info("joinClauses={}", JsonUtils.toJsonPrettily(joinClauses));
        log.info("assignments={}", JsonUtils.toJsonPrettily(assignments));
        log.info("binaries={}", JsonUtils.toJsonPrettily(binaries));
        log.info("returnProps={}", JsonUtils.toJsonPrettily(returnProps));

        ChainAnalysisDto result = new ChainAnalysisDto();
        result.setEntityQualifier(designMeta.getEntityQualifier());
        result.setMethodName(methodName);
        result.setChainMethod(chainMethod);
        result.setReturnShape(returnShape);
        result.setSelectProperties(selectProperties);
        result.setSearchConditions(searchConditions);
        result.setSortProperties(sortProperties);
        result.setJoinClauses(joinClauses);
        result.setAssignments(assignments);
        result.setBinariesAsArgs(binaries);
        result.setPropertiesAsResult(returnProps);
        result.setChain(queryChain);
        result.setIsByForced(chainCode.contains("." + TokenWordConstant.BY_FORCED_METHOD_NAME + "()"));
        String hash = StringUtils.upperCase(HashingUtils.hashString(result.toString()));
        result.setLotNo(String.format("QT%s-%s", Allison1875.SHORT_VERSION, hash));
        return result;
    }

    private Stream<String> extractPropertyNames(String joinedEntityWithProperty, DesignMetaDto joinedEntityMeta) {
        String joinedPropertiesText = StringUtils.substringAfter(joinedEntityWithProperty, ".");
        Stream<String> joinedPropertyNames;
        if (!joinedPropertiesText.isEmpty()) {
            joinedPropertyNames = Arrays.stream(joinedPropertiesText.split("\\.")).distinct();
        } else {
            joinedPropertyNames = Lists.newArrayList(joinedEntityMeta.getProperties().keySet()).stream();
        }
        return joinedPropertyNames;
    }

    private String getJoinedDesignQualifier(ClassOrInterfaceDeclaration joinDesign, String entityName) {
        return joinDesign.getFieldByName(entityName)
                .orElseThrow(() -> new IllegalDesignException("Entity Field is absent in JoinDesign")).getVariable(0)
                .getInitializer().orElseThrow(() -> new IllegalDesignException("Initializer is absent in Entity Field"))
                .asFieldAccessExpr().getScope().asNameExpr().getNameAsString().replace('_', '.');
    }

    private String analyzeSpecifiedMethodName(ChainMethodEnum chainMethod, MethodCallExpr chain,
            DesignMetaDto designMeta) {
        MethodCallExpr queryMce = chain.findAll(MethodCallExpr.class,
                mce -> StringUtils.equalsAny(mce.getNameAsString(), "query", "update", "drop")).get(0);
        NodeList<Expression> arguments = queryMce.getArguments();
        if (CollectionUtils.isEmpty(arguments)) {
            String defaultMethodName = chainMethod.name() + StringUtils.removeEnd(designMeta.getEntityName(), "Entity");
            log.info("Method name not specified in Query Chain, hence default '{}' is used", defaultMethodName);
            return defaultMethodName;
        }
        String methodName = arguments.get(0).asStringLiteralExpr().getValue().trim();
        return methodName;
    }

}