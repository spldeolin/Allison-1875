package com.spldeolin.allison1875.persistencegenerator.service.impl;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.StringEscapeUtils;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.exception.PrimaryTypeAbsentException;
import com.spldeolin.allison1875.common.exception.QualifierAbsentException;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.util.HashingUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.facade.constant.TokenWordConstant;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMetaDto;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.GenerateDesignArgs;
import com.spldeolin.allison1875.persistencegenerator.javabean.GenerateDesignRetval;
import com.spldeolin.allison1875.persistencegenerator.javabean.GenerateJoinDesignArgs;
import com.spldeolin.allison1875.persistencegenerator.javabean.TableStructureAnalysisDto;
import com.spldeolin.allison1875.persistencegenerator.service.DesignGeneratorService;
import com.spldeolin.allison1875.support.EntityKey;
import com.spldeolin.allison1875.support.OnChainLink;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-10-06
 */
@Singleton
@Slf4j
public class DesignGeneratorServiceImpl implements DesignGeneratorService {

    @Inject
    private CommonConfig commonConfig;

    @Inject
    private PersistenceGeneratorConfig config;

    @Inject
    private ImportExprService importExprService;

    @Override
    public Optional<CompilationUnit> generateJoinDesign(GenerateJoinDesignArgs args) {
        TableStructureAnalysisDto tableStructureAnalysis = args.getTableStructureAnalysis();
        JavabeanGeneration entityGeneration = args.getEntityGeneration();
        String entityName = entityGeneration.getJavabeanName();

        if (!config.getEnableGenerateDesign()) {
            return Optional.empty();
        }

        NodeList<TypeParameter> typeParams = new NodeList<>(new TypeParameter("MQCM"), new TypeParameter("ME"));

        CompilationUnit cu = args.getJoinDesignCu();
        if (cu == null) {
            String designName = "JoinDesign";
            cu = AstForestContext.get().findCu(commonConfig.getDesignPackage() + "." + designName).orElseGet(() -> {
                CompilationUnit designCu = new CompilationUnit();
                Path designPath = CodeGenerationUtils.fileInPackageAbsolutePath(AstForestContext.get().getSourceRoot(),
                        commonConfig.getDesignPackage(), designName + ".java");
                log.info("Join Design absent, create it, path={}", designPath);
                designCu.setStorage(designPath);
                designCu.setPackageDeclaration(commonConfig.getDesignPackage());
                designCu.addImport(OnChainLink.class.getName());
                designCu.addImport(EntityKey.class.getName());
                designCu.addOrphanComment(new LineComment("@formatter:" + "off"));
                ClassOrInterfaceDeclaration designCoid = new ClassOrInterfaceDeclaration();
                JavadocComment javadoc = new JavadocComment(
                        concatJoinDesignDescription(args.getTableStructureAnalysis()));
                designCoid.setJavadocComment(javadoc);
                designCoid.addAnnotation(StaticJavaParser.parseAnnotation("@SuppressWarnings(\"all\")"));
                designCoid.setPublic(true).setInterface(false).setName(designName).setTypeParameters(typeParams);
                designCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                        "private final static UnsupportedOperationException e = new UnsupportedOperationException"
                                + "();"));
                designCoid.addMember(StaticJavaParser.parseBodyDeclaration("private " + designName + "() {}"));
                designCu.addType(designCoid);
                designCu.addOrphanComment(new LineComment(""));
                return designCu;
            });
        }
        TypeDeclaration<?> design = cu.getPrimaryType()
                .orElseThrow(() -> new PrimaryTypeAbsentException("JoinDesign PrimaryType absent"));

        FieldDeclaration joinedEntityField = StaticJavaParser.parseBodyDeclaration(
                String.format("public Join%s<MQCM, ME> %s = %s.ett;", entityName, entityName,
                        args.getDesignQualifier().replace('.', '_'))).asFieldDeclaration();
        design.getFieldByName(entityName).ifPresent(Node::remove);
        design.addMember(joinedEntityField);

        ClassOrInterfaceDeclaration joinEntityCoid = new ClassOrInterfaceDeclaration();
        joinEntityCoid.setPublic(true).setStatic(true).setName("Join" + entityName).setTypeParameters(typeParams);
        for (PropertyDto property : tableStructureAnalysis.getProperties()) {
            joinEntityCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                    String.format("public Join%s<MQCM, ME> %s;", entityName, property.getPropertyName())));
        }
        joinEntityCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public Join%sOn<MQCM, ME> on() { throw e; }", entityName)));
        joinEntityCoid.addMember(
                StaticJavaParser.parseBodyDeclaration(String.format("private Join%s(Object o) {}", entityName)));
        design.getMembers().stream().filter(BodyDeclaration::isClassOrInterfaceDeclaration)
                .map(BodyDeclaration::asClassOrInterfaceDeclaration)
                .filter(coid -> coid.getName().equals(joinEntityCoid.getName())).findAny().ifPresent(Node::remove);
        design.addMember(joinEntityCoid);

        ClassOrInterfaceDeclaration joinEntityOnCoid = new ClassOrInterfaceDeclaration();
        joinEntityOnCoid.setPublic(true).setStatic(true).setName("Join" + entityName + "On")
                .setTypeParameters(typeParams).addImplementedType(args.getDesignQualifier().replace('.', '_'));
        for (PropertyDto property : tableStructureAnalysis.getProperties()) {
            joinEntityOnCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                    String.format("public OnChainLink<MQCM, %s, EntityKey<ME, %s>> %s;",
                            property.getJavaType().getQualifier(), property.getJavaType().getQualifier(),
                            property.getPropertyName())));
        }
        joinEntityOnCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public Join%sOnOpened<MQCM, ME> open() { throw e; }", entityName)));
        joinEntityOnCoid.addMember(
                StaticJavaParser.parseBodyDeclaration(String.format("private Join%sOn(Object o) {}", entityName)));
        design.getMembers().stream().filter(BodyDeclaration::isClassOrInterfaceDeclaration)
                .map(BodyDeclaration::asClassOrInterfaceDeclaration)
                .filter(coid -> coid.getName().equals(joinEntityOnCoid.getName())).findAny().ifPresent(Node::remove);
        design.addMember(joinEntityOnCoid);

        ClassOrInterfaceDeclaration joinEntityOnOpenedCoid = new ClassOrInterfaceDeclaration();
        joinEntityOnOpenedCoid.setPublic(true).setStatic(true).setName("Join" + entityName + "OnOpened")
                .setTypeParameters(typeParams).addImplementedType(args.getDesignQualifier().replace('.', '_'));
        for (PropertyDto property : tableStructureAnalysis.getProperties()) {
            joinEntityOnOpenedCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                    String.format("public OnChainLink<Join%sOnOpenedClosable<MQCM, ME>, %s, EntityKey<ME, %s>> %s;",
                            entityName, property.getJavaType().getQualifier(), property.getJavaType().getQualifier(),
                            property.getPropertyName())));
        }
        design.getMembers().stream().filter(BodyDeclaration::isClassOrInterfaceDeclaration)
                .map(BodyDeclaration::asClassOrInterfaceDeclaration)
                .filter(coid -> coid.getName().equals(joinEntityOnOpenedCoid.getName())).findAny()
                .ifPresent(Node::remove);
        design.addMember(joinEntityOnOpenedCoid);

        ClassOrInterfaceDeclaration joinEntityOnOpenedClosableCoid = new ClassOrInterfaceDeclaration();
        joinEntityOnOpenedClosableCoid.setPublic(true).setStatic(true).setName("Join" + entityName + "OnOpenedClosable")
                .setTypeParameters(typeParams).addExtendedType("Join" + entityName + "OnOpened<MQCM, ME>")
                .addImplementedType(args.getDesignQualifier().replace('.', '_'));
        joinEntityOnOpenedClosableCoid.addMember(
                StaticJavaParser.parseBodyDeclaration("public MQCM close() { throw e; }"));
        design.getMembers().stream().filter(BodyDeclaration::isClassOrInterfaceDeclaration)
                .map(BodyDeclaration::asClassOrInterfaceDeclaration)
                .filter(coid -> coid.getName().equals(joinEntityOnOpenedClosableCoid.getName())).findAny()
                .ifPresent(Node::remove);
        design.addMember(joinEntityOnOpenedClosableCoid);

        ClassOrInterfaceDeclaration designQualifierMarker = new ClassOrInterfaceDeclaration();
        designQualifierMarker.setPrivate(true).setStatic(true).setInterface(true)
                .setName(args.getDesignQualifier().replace('.', '_'));
        designQualifierMarker.addMember(
                StaticJavaParser.parseBodyDeclaration(String.format("Join%s ett = null;", entityName)));
        design.getMembers().stream().filter(BodyDeclaration::isClassOrInterfaceDeclaration)
                .map(BodyDeclaration::asClassOrInterfaceDeclaration)
                .filter(coid -> coid.getName().equals(designQualifierMarker.getName())).findAny()
                .ifPresent(Node::remove);
        design.addMember(designQualifierMarker);

        importExprService.extractQualifiedTypeToImport(cu);

        cu.removeOrphanComment(cu.getOrphanComments().get(cu.getOrphanComments().size() - 1));
        cu.addOrphanComment(new LineComment(HashingUtils.hashTypeDeclaration(design)));

        return Optional.of(cu);
    }

    @Override
    public GenerateDesignRetval generateDesign(GenerateDesignArgs args) {
        TableStructureAnalysisDto tableStructureAnalysis = args.getTableStructureAnalysis();
        JavabeanGeneration entityGeneration = args.getEntityGeneration();

        if (!config.getEnableGenerateDesign()) {
            return new GenerateDesignRetval();
        }

        String designName = concatDesignName(tableStructureAnalysis);
        Path designPath = CodeGenerationUtils.fileInPackageAbsolutePath(AstForestContext.get().getSourceRoot(),
                commonConfig.getDesignPackage(), designName + ".java");

        List<PropertyDto> properties = tableStructureAnalysis.getProperties();
        LinkedHashMap<String, PropertyDto> propertiesByName = Maps.newLinkedHashMap();

        CompilationUnit cu = new CompilationUnit();
        cu.setStorage(designPath);
        cu.setPackageDeclaration(commonConfig.getDesignPackage());
        for (PropertyDto property : properties) {
            propertiesByName.put(property.getPropertyName(), property);
        }
        cu.addOrphanComment(new LineComment("@formatter:" + "off"));
        ClassOrInterfaceDeclaration designCoid = new ClassOrInterfaceDeclaration();
        Javadoc javadoc = entityGeneration.getCoid().getJavadoc().orElse(new Javadoc(new JavadocDescription()));
        designCoid.setJavadocComment(javadoc);
        designCoid.addAnnotation(StaticJavaParser.parseAnnotation("@SuppressWarnings(\"all\")"));
        designCoid.setPublic(true).setInterface(false).setName(designName);
        designCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "private final static UnsupportedOperationException e = new UnsupportedOperationException();"));
        designCoid.addMember(StaticJavaParser.parseBodyDeclaration("private " + designName + "() {}"));
        designCoid.addMember(
                StaticJavaParser.parseBodyDeclaration("public static QueryChain query(String methodName) {throw e;}"));
        designCoid.addMember(StaticJavaParser.parseBodyDeclaration("public static QueryChain query() {throw e;}"));
        designCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "public static UpdateChain update(String methodName) {throw e;}"));
        designCoid.addMember(StaticJavaParser.parseBodyDeclaration("public static UpdateChain update() {throw e;}"));
        designCoid.addMember(
                StaticJavaParser.parseBodyDeclaration("public static DropChain drop(String methodName) {throw e;}"));
        designCoid.addMember(StaticJavaParser.parseBodyDeclaration("public static DropChain drop() {throw e;}"));

        ClassOrInterfaceDeclaration queryChainMethodsCoid = new ClassOrInterfaceDeclaration();
        queryChainMethodsCoid.setPublic(true).setStatic(true).setName("QueryChainMethods");
        queryChainMethodsCoid.addMember(
                StaticJavaParser.parseBodyDeclaration("public ByChainReturn<NextableByChainReturn> by() { throw e; }"));
        queryChainMethodsCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public ByChainReturn<NextableByChainReturn> %s() { throw e; }",
                        TokenWordConstant.BY_FORCED_METHOD_NAME)));
        queryChainMethodsCoid.addMember(
                StaticJavaParser.parseBodyDeclaration("public OrderChain order() { throw e; }"));
        queryChainMethodsCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "public java.util.List<" + entityGeneration.getJavabeanQualifier() + "> many() { throw e; }"));
        queryChainMethodsCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public <P> java.util.Map<P, %s> many(Each<P> property) { throw e; }",
                        entityGeneration.getJavabeanName())));
        queryChainMethodsCoid.addMember(StaticJavaParser.parseBodyDeclaration(String.format(
                "public <P> com.google.common.collect.Multimap<P, %s> many(MultiEach<P> property) { throw e; }",
                entityGeneration.getJavabeanName())));
        queryChainMethodsCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public %s one() { throw e; }", entityGeneration.getJavabeanName())));
        queryChainMethodsCoid.addMember(StaticJavaParser.parseBodyDeclaration("public int count() { throw e; }"));
        queryChainMethodsCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public JoinDesign<QueryChainMethods, %s> leftJoin() { throw e; }",
                        entityGeneration.getJavabeanName())));
        queryChainMethodsCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public JoinDesign<QueryChainMethods, %s> rightJoin() { throw e; }",
                        entityGeneration.getJavabeanName())));
        queryChainMethodsCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public JoinDesign<QueryChainMethods, %s> innerJoin() { throw e; }",
                        entityGeneration.getJavabeanName())));
        queryChainMethodsCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public JoinDesign<QueryChainMethods, %s> outerJoin() { throw e; }",
                        entityGeneration.getJavabeanName())));
        designCoid.addMember(queryChainMethodsCoid);

        ClassOrInterfaceDeclaration queryChainCoid = new ClassOrInterfaceDeclaration();
        queryChainCoid.setPublic(true).setStatic(true).setInterface(false).setName("QueryChain")
                .addExtendedType("QueryChainMethods");
        queryChainCoid.addMember(StaticJavaParser.parseBodyDeclaration("private QueryChain () {}"));
        for (PropertyDto property : properties) {
            FieldDeclaration field = StaticJavaParser.parseBodyDeclaration(
                    "public QueryChain " + property.getPropertyName() + ";").asFieldDeclaration();
            field.setJavadocComment(property.getDescription());
            queryChainCoid.addMember(field);
        }
        designCoid.addMember(queryChainCoid);

        ClassOrInterfaceDeclaration updateChainCoid = new ClassOrInterfaceDeclaration();
        updateChainCoid.setPublic(true).setInterface(true).setName("UpdateChain");
        for (PropertyDto property : properties) {
            updateChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                            "NextableUpdateChain " + property.getPropertyName() + "(" + property.getJavaType().getQualifier()
                                    + " " + property.getPropertyName() + ");").asMethodDeclaration()
                    .setJavadocComment(property.getDescription()));
        }
        designCoid.addMember(updateChainCoid);

        ClassOrInterfaceDeclaration nextableUpdateChainCoid = new ClassOrInterfaceDeclaration();
        nextableUpdateChainCoid.setPublic(true).setInterface(true).setName("NextableUpdateChain")
                .addExtendedType("UpdateChain");
        nextableUpdateChainCoid.addMember(StaticJavaParser.parseBodyDeclaration("int over();"));
        nextableUpdateChainCoid.addMember(
                StaticJavaParser.parseBodyDeclaration("ByChainReturn<NextableByChainVoid> by();"));
        nextableUpdateChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("ByChainReturn<NextableByChainVoid> %s();", TokenWordConstant.BY_FORCED_METHOD_NAME)));
        designCoid.addMember(nextableUpdateChainCoid);

        ClassOrInterfaceDeclaration dropChainCoid = new ClassOrInterfaceDeclaration();
        dropChainCoid.setPublic(true).setInterface(true).setName("DropChain");
        dropChainCoid.addMember(StaticJavaParser.parseBodyDeclaration("int over();"));
        dropChainCoid.addMember(StaticJavaParser.parseBodyDeclaration("ByChainReturn<NextableByChainVoid> by();"));
        dropChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("ByChainReturn<NextableByChainVoid> %s();", TokenWordConstant.BY_FORCED_METHOD_NAME)));
        designCoid.addMember(dropChainCoid);

        ClassOrInterfaceDeclaration byChainReturnCode = new ClassOrInterfaceDeclaration();
        byChainReturnCode.setPublic(true).setStatic(true).setInterface(false).setName("ByChainReturn")
                .addTypeParameter("NEXT");
        for (PropertyDto property : properties) {
            byChainReturnCode.addMember(StaticJavaParser.parseBodyDeclaration(
                            "public com.spldeolin.allison1875.support.ByChainPredicate<NEXT, " + property.getJavaType()
                                    .getSimpleName() + "> " + property.getPropertyName() + ";").asFieldDeclaration()
                    .setJavadocComment(property.getDescription()));
        }
        designCoid.addMember(byChainReturnCode);

        ClassOrInterfaceDeclaration nextableByChainReturnCoid = new ClassOrInterfaceDeclaration();
        nextableByChainReturnCoid.setPublic(true).setStatic(true).setInterface(false).setName("NextableByChainReturn");
        for (PropertyDto property : properties) {
            nextableByChainReturnCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                            "public ByChainPredicate<NextableByChainReturn, " + property.getJavaType().getSimpleName() + "> "
                                    + property.getPropertyName() + ";").asFieldDeclaration()
                    .setJavadocComment(property.getDescription()));
        }
        nextableByChainReturnCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "public java.util.List<" + entityGeneration.getJavabeanQualifier() + "> many() { throw e; }"));
        nextableByChainReturnCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public <P> java.util.Map<P, %s> many(Each<P> property) { throw e; }",
                        entityGeneration.getJavabeanName())));
        nextableByChainReturnCoid.addMember(StaticJavaParser.parseBodyDeclaration(String.format(
                "public <P> com.google.common.collect.Multimap<P, %s> many(MultiEach<P> property) { throw e; }",
                entityGeneration.getJavabeanName())));
        nextableByChainReturnCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "public " + entityGeneration.getJavabeanName() + " one() { throw e; }"));
        nextableByChainReturnCoid.addMember(StaticJavaParser.parseBodyDeclaration("public int count() { throw e; }"));
        nextableByChainReturnCoid.addMember(
                StaticJavaParser.parseBodyDeclaration("public OrderChain order() { throw e; }"));
        designCoid.addMember(nextableByChainReturnCoid);

        ClassOrInterfaceDeclaration nextableByChainVoidCoid = new ClassOrInterfaceDeclaration();
        nextableByChainVoidCoid.setPublic(true).setStatic(true).setInterface(false).setName("NextableByChainVoid");
        for (PropertyDto property : properties) {
            nextableByChainVoidCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                            "public ByChainPredicate<NextableByChainVoid, " + property.getJavaType().getSimpleName() + "> "
                                    + property.getPropertyName() + ";").asFieldDeclaration()
                    .setJavadocComment(property.getDescription()));
        }
        nextableByChainVoidCoid.addMember(StaticJavaParser.parseBodyDeclaration("public int over() { throw e; }"));
        designCoid.addMember(nextableByChainVoidCoid);

        ClassOrInterfaceDeclaration orderChainCoid = new ClassOrInterfaceDeclaration();
        orderChainCoid.setPublic(true).setStatic(true).setInterface(false).setName("OrderChain");
        for (PropertyDto property : properties) {
            orderChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                            "public com.spldeolin.allison1875.support.OrderChainPredicate<NextableOrderChain> "
                                    + property.getPropertyName() + ";").asFieldDeclaration()
                    .setJavadocComment(property.getDescription()));
        }
        designCoid.addMember(orderChainCoid);

        ClassOrInterfaceDeclaration nextableOrderChainCoid = new ClassOrInterfaceDeclaration();
        nextableOrderChainCoid.setPublic(true).setStatic(true).setInterface(false).setName("NextableOrderChain")
                .addExtendedType("OrderChain");
        nextableOrderChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "public java.util.List<" + entityGeneration.getJavabeanQualifier() + "> many() { throw e; }"));
        nextableOrderChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public <P> java.util.Map<P, %s> many(Each<P> property) { throw e; }",
                        entityGeneration.getJavabeanName())));
        nextableOrderChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(String.format(
                "public <P> com.google.common.collect.Multimap<P, %s> many(MultiEach<P> property) { throw e; }",
                entityGeneration.getJavabeanName())));
        nextableOrderChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "public " + entityGeneration.getJavabeanName() + " one() { throw e; }"));
        nextableOrderChainCoid.addMember(StaticJavaParser.parseBodyDeclaration("public int count() { throw e; }"));
        designCoid.addMember(nextableOrderChainCoid);

        ClassOrInterfaceDeclaration eachCoid = new ClassOrInterfaceDeclaration();
        eachCoid.setPublic(true).setStatic(false).setInterface(true).setName("Each").addTypeParameter("P");
        for (PropertyDto property : tableStructureAnalysis.getProperties()) {
            eachCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                    String.format("Each<%s> %s = (Each<%s>) new Object();", property.getJavaType().getSimpleName(),
                            property.getPropertyName(), property.getJavaType().getSimpleName())));
        }
        designCoid.addMember(eachCoid);

        ClassOrInterfaceDeclaration multiEachCoid = new ClassOrInterfaceDeclaration();
        multiEachCoid.setPublic(true).setStatic(false).setInterface(true).setName("MultiEach").addTypeParameter("P");
        for (PropertyDto property : tableStructureAnalysis.getProperties()) {
            multiEachCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                    String.format("MultiEach<%s> %s = (MultiEach<%s>) new Object();",
                            property.getJavaType().getSimpleName(), property.getPropertyName(),
                            property.getJavaType().getSimpleName())));
        }
        designCoid.addMember(multiEachCoid);

        for (PropertyDto property : tableStructureAnalysis.getProperties()) {
            designCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                    "public static com.spldeolin.allison1875.support.EntityKey<" + entityGeneration.getJavabeanName()
                            + "," + property.getJavaType().getSimpleName() + "> " + property.getPropertyName() + ";"));
        }

        DesignMetaDto meta = new DesignMetaDto();
        meta.setDesignQualifier(commonConfig.getDesignPackage() + "." + designName);
        meta.setDesignName(designName);
        meta.setEntityQualifier(entityGeneration.getJavabeanQualifier());
        meta.setEntityName(entityGeneration.getJavabeanName());
        meta.setMapperQualifier(args.getMapper().getFullyQualifiedName()
                .orElseThrow(() -> new QualifierAbsentException(args.getMapper())));
        meta.setMapperName(args.getMapper().getNameAsString());
        meta.setMapperPaths(commonConfig.getMapperXmlDirs().stream()
                .map(one -> one + File.separator + tableStructureAnalysis.getMapperName() + ".xml")
                .collect(Collectors.toList()));
        if (tableStructureAnalysis.getIsDeleteFlagExist()) {
            meta.setNotDeletedSql(config.getNotDeletedSql());
        }
        meta.setProperties(propertiesByName);
        meta.setTableName(tableStructureAnalysis.getTableName());
        String metaJson = JsonUtils.toJson(meta);
        designCoid.addFieldWithInitializer("String", TokenWordConstant.META_FIELD_NAME,
                StaticJavaParser.parseExpression("\"" + StringEscapeUtils.escapeJava(metaJson) + "\""));
        cu.addType(designCoid);

        importExprService.extractQualifiedTypeToImport(cu);

        cu.addOrphanComment(new LineComment(HashingUtils.hashTypeDeclaration(designCoid)));

        return new GenerateDesignRetval().setDesignFile(FileFlush.build(cu))
                .setDesignQualifer(commonConfig.getDesignPackage() + "." + designName);
    }

    @Override
    public String concatDesignName(TableStructureAnalysisDto persistence) {
        return MoreStringUtils.toUpperCamel(persistence.getTableName()) + "Design";
    }

    private String concatJoinDesignDescription(TableStructureAnalysisDto persistence) {
        String result = "";
        if (commonConfig.getEnableNoModifyAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.NO_MODIFY_ANNOUNCE;
        }
        if (commonConfig.getEnableLotNoAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + persistence.getLotNo();
        }
        return result;
    }

}