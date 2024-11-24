package com.spldeolin.allison1875.persistencegenerator.service.impl;

import static com.github.javaparser.StaticJavaParser.parseAnnotation;
import static com.github.javaparser.StaticJavaParser.parseParameter;
import static com.github.javaparser.StaticJavaParser.parseType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.atteo.evo.inflector.English;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.exception.CuAbsentException;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.DetectOrGenerateMapperRetval;
import com.spldeolin.allison1875.persistencegenerator.javabean.GenerateMethodToMapperArgs;
import com.spldeolin.allison1875.persistencegenerator.javabean.QueryByKeysDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.TableStructureAnalysisDto;
import com.spldeolin.allison1875.persistencegenerator.service.MapperCoidService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-08-13
 */
@Slf4j
@Singleton
public class MapperCoidServiceImpl implements MapperCoidService {

    @Inject
    private CommonConfig commonConfig;
    
    @Inject
    private PersistenceGeneratorConfig config;

    @Inject
    private AntiDuplicationService antiDuplicationService;

    @Override
    public DetectOrGenerateMapperRetval detectOrGenerateMapper(TableStructureAnalysisDto persistence,
            JavabeanGeneration javabeanGeneration) {

        // find
        List<MethodDeclaration> customMethods = Lists.newArrayList();
        String mapperQualifier = commonConfig.getMapperPackage() + "." + persistence.getMapperName();
        Optional<CompilationUnit> opt = AstForestContext.get().findCu(mapperQualifier);
        ClassOrInterfaceDeclaration mapper;
        if (opt.isPresent()) {
            Optional<TypeDeclaration<?>> primaryType = opt.get().getPrimaryType();
            if (!primaryType.isPresent()) {
                throw new IllegalStateException("primaryType absent.");
            }
            mapper = primaryType.get().asClassOrInterfaceDeclaration();
            if (!mapper.isInterface()) {
                throw new IllegalStateException("primaryType is not a interface.");
            }

            // 删除Mapper中所有声明了NoModifyAnnounce的方法
            for (MethodDeclaration method : mapper.getMethods()) {
                if (JavadocUtils.getComment(method).contains(BaseConstant.NO_MODIFY_ANNOUNCE)) {
                    method.remove();
                }
            }

            customMethods = Lists.newArrayList(mapper.getMethods());
            customMethods.forEach(MethodDeclaration::remove);
        } else {

            // create
            log.info("Mapper文件不存在，创建它。 [{}]", mapperQualifier);
            CompilationUnit cu = new CompilationUnit();
            cu.setStorage(CodeGenerationUtils.fileInPackageAbsolutePath(AstForestContext.get().getSourceRoot(),
                    commonConfig.getMapperPackage(), persistence.getMapperName() + ".java"));
            cu.setPackageDeclaration(commonConfig.getMapperPackage());
            mapper = new ClassOrInterfaceDeclaration();
            String comment = concatMapperDescription(persistence);
            Javadoc javadoc = JavadocUtils.setJavadoc(mapper, comment,
                    commonConfig.getAuthor() + " " + LocalDate.now());
            javadoc.addBlockTag(new JavadocBlockTag(Type.SEE, javabeanGeneration.getJavabeanName()));
            mapper.setPublic(true).setInterface(true).setName(persistence.getMapperName());
            mapper.setInterface(true);
            cu.addType(mapper);
        }

        DetectOrGenerateMapperRetval result = new DetectOrGenerateMapperRetval();
        result.setMapper(mapper);
        result.setMapperCu(mapper.findCompilationUnit().orElseThrow(() -> new CuAbsentException(mapper)));
        result.getCustomMethods().addAll(customMethods);
        return result;
    }

    private String concatMapperDescription(TableStructureAnalysisDto persistence) {
        String result = persistence.getDescrption() + BaseConstant.JAVA_DOC_NEW_LINE + persistence.getTableName();
        if (commonConfig.getEnableLotNoAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE;
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + persistence.getLotNo();
        }
        return result;
    }

    @Override
    public String generateBatchInsertEvenNullMethodToMapper(GenerateMethodToMapperArgs args) {
        String methodName = antiDuplicationService.getNewMethodNameIfExist("batchInsertEvenNull", args.getMapper());
        MethodDeclaration insert = new MethodDeclaration();
        String comment = concatMapperMethodComment(args.getTableStructureAnalysisDto(),
                "批量插入，为null的属性会被作为null插入");
        insert.setJavadocComment(comment);
        insert.setType(PrimitiveType.intType());
        insert.setName(methodName);
        insert.addParameter(StaticJavaParser.parseParameter(
                "@org.apache.ibatis.annotations.Param(\"entities\") List<" + args.getEntityGeneration()
                        .getJavabeanQualifier() + "> entities"));
        insert.setBody(null);
        args.getMapper().getMembers().addLast(insert);
        return methodName;
    }

    @Override
    public String generateBatchInsertMethodToMapper(GenerateMethodToMapperArgs args) {
        String methodName = antiDuplicationService.getNewMethodNameIfExist("batchInsert", args.getMapper());
        MethodDeclaration insert = new MethodDeclaration();
        String comment = concatMapperMethodComment(args.getTableStructureAnalysisDto(), "批量插入");
        insert.setJavadocComment(comment);
        insert.setType(PrimitiveType.intType());
        insert.setName(methodName);
        insert.addParameter(StaticJavaParser.parseParameter(
                "@org.apache.ibatis.annotations.Param(\"entities\") List<" + args.getEntityGeneration()
                        .getJavabeanQualifier() + "> entities"));
        insert.setBody(null);
        args.getMapper().getMembers().addLast(insert);
        return methodName;
    }

    @Override
    public String generateBatchUpdateEvenNullMethodToMapper(GenerateMethodToMapperArgs args) {
        String methodName = antiDuplicationService.getNewMethodNameIfExist("batchUpdateEvenNull", args.getMapper());
        MethodDeclaration update = new MethodDeclaration();
        String comment = concatMapperMethodComment(args.getTableStructureAnalysisDto(),
                "批量根据ID更新数据，为null对应的字段会被更新为null");
        update.setJavadocComment(comment);
        update.setType(PrimitiveType.intType());
        update.setName(methodName);
        update.addParameter(StaticJavaParser.parseParameter(
                "@org.apache.ibatis.annotations.Param(\"entities\") List<" + args.getEntityGeneration()
                        .getJavabeanQualifier() + "> entities"));
        update.setBody(null);
        args.getMapper().getMembers().addLast(update);
        return methodName;
    }

    @Override
    public String generateBatchUpdateMethodToMapper(GenerateMethodToMapperArgs args) {
        String methodName = antiDuplicationService.getNewMethodNameIfExist("batchUpdate", args.getMapper());
        MethodDeclaration update = new MethodDeclaration();
        String comment = concatMapperMethodComment(args.getTableStructureAnalysisDto(), "批量根据ID更新数据");
        update.setJavadocComment(comment);
        update.setType(PrimitiveType.intType());
        update.setName(methodName);
        update.addParameter(StaticJavaParser.parseParameter(
                "@org.apache.ibatis.annotations.Param(\"entities\") List<" + args.getEntityGeneration()
                        .getJavabeanQualifier() + "> entities"));
        update.setBody(null);
        args.getMapper().getMembers().addLast(update);
        return methodName;
    }

    @Override
    public String generateDeleteByKeyMethodToMapper(GenerateMethodToMapperArgs args) {
        String methodName = antiDuplicationService.getNewMethodNameIfExist(
                "deleteBy" + MoreStringUtils.toUpperCamel(args.getKey().getPropertyName()), args.getMapper());
        MethodDeclaration method = new MethodDeclaration();
        String varName = MoreStringUtils.toLowerCamel(args.getKey().getPropertyName());
        String comment = concatMapperMethodComment(args.getTableStructureAnalysisDto(),
                "根据「" + args.getKey().getDescription() + "」删除");
        method.setJavadocComment(comment);
        method.setType(PrimitiveType.intType());
        method.setName(methodName);

        Parameter parameter = parseParameter(args.getKey().getJavaType().getSimpleName() + " " + varName);
        method.addParameter(parameter);
        method.setBody(null);
        args.getMapper().getMembers().addLast(method);
        return methodName;
    }

    @Override
    public String generateInsertOrUpdateMethodToMapper(GenerateMethodToMapperArgs args) {
        String methodName = antiDuplicationService.getNewMethodNameIfExist("insertOrUpdate", args.getMapper());
        MethodDeclaration insert = new MethodDeclaration();
        String comment = concatMapperMethodComment(args.getTableStructureAnalysisDto(),
                "尝试插入，若指定了id并存在，则更新，即INSERT ON DUPLICATE KEY UPDATE");
        insert.setJavadocComment(comment);
        insert.setType(PrimitiveType.intType());
        insert.setName(methodName);
        insert.addParameter(args.getEntityGeneration().getJavabeanQualifier(), "entity");
        insert.setBody(null);
        args.getMapper().getMembers().addLast(insert);
        return methodName;
    }

    @Override
    public String generateInsertMethodToMapper(GenerateMethodToMapperArgs args) {
        String methodName = antiDuplicationService.getNewMethodNameIfExist("insert", args.getMapper());
        MethodDeclaration insert = new MethodDeclaration();
        String comment = concatMapperMethodComment(args.getTableStructureAnalysisDto(), "插入");
        insert.setJavadocComment(comment);
        insert.setType(PrimitiveType.intType());
        insert.setName(methodName);
        insert.addParameter(args.getEntityGeneration().getJavabeanQualifier(), "entity");
        insert.setBody(null);
        args.getMapper().getMembers().addLast(insert);
        return methodName;
    }

    @Override
    public String generateListAllMethodToMapper(GenerateMethodToMapperArgs args) {
        String methodName = null;
        if (CollectionUtils.isNotEmpty(args.getTableStructureAnalysisDto().getIdProperties())) {
            methodName = antiDuplicationService.getNewMethodNameIfExist("listAll", args.getMapper());
            MethodDeclaration listAll = new MethodDeclaration();
            String comment = concatMapperMethodComment(args.getTableStructureAnalysisDto(), "获取全部");
            listAll.setType(
                    StaticJavaParser.parseType("List<" + args.getEntityGeneration().getJavabeanQualifier() + ">"));
            listAll.setName(methodName);
            listAll.setJavadocComment(comment);
            listAll.setBody(null);
            args.getMapper().getMembers().addLast(listAll);
        }
        return methodName;
    }

    @Override
    public String generateQueryByEntityMethodToMapper(GenerateMethodToMapperArgs args) {
        String methodName = antiDuplicationService.getNewMethodNameIfExist("queryByEntity", args.getMapper());
        MethodDeclaration queryByEntity = new MethodDeclaration();
        String comment = concatMapperMethodComment(args.getTableStructureAnalysisDto(), "根据实体内的属性查询");
        queryByEntity.setType(parseType("java.util.List<" + args.getEntityGeneration().getJavabeanQualifier() + ">"));
        queryByEntity.setName(methodName);
        queryByEntity.addParameter(args.getEntityGeneration().getJavabeanQualifier(), "entity");
        queryByEntity.setBody(null);
        queryByEntity.setJavadocComment(comment);
        args.getMapper().getMembers().addLast(queryByEntity);
        return methodName;
    }

    @Override
    public String generateQueryByIdMethodToMapper(GenerateMethodToMapperArgs args) {
        String methodName = null;
        if (CollectionUtils.isNotEmpty(args.getTableStructureAnalysisDto().getIdProperties())) {
            methodName = antiDuplicationService.getNewMethodNameIfExist("queryById", args.getMapper());
            MethodDeclaration queryById = new MethodDeclaration();
            String comment = concatMapperMethodComment(args.getTableStructureAnalysisDto(), "根据ID查询");
            queryById.setType(new ClassOrInterfaceType().setName(args.getEntityGeneration().getJavabeanQualifier()));
            queryById.setName(methodName);

            if (args.getTableStructureAnalysisDto().getIdProperties().size() == 1) {
                PropertyDto onlyPk = Iterables.getOnlyElement(args.getTableStructureAnalysisDto().getIdProperties());
                String varName = MoreStringUtils.toLowerCamel(onlyPk.getPropertyName());
                Parameter parameter = parseParameter(onlyPk.getJavaType().getSimpleName() + " " + varName);
                queryById.addParameter(parameter);
            } else {
                for (PropertyDto pk : args.getTableStructureAnalysisDto().getIdProperties()) {
                    String varName = MoreStringUtils.toLowerCamel(pk.getPropertyName());
                    Parameter parameter = parseParameter(
                            "@org.apache.ibatis.annotations.Param(\"" + varName + "\")" + pk.getJavaType()
                                    .getSimpleName() + " " + varName);
                    queryById.addParameter(parameter);
                }
            }
            queryById.setJavadocComment(comment);
            queryById.setBody(null);
            args.getMapper().getMembers().addLast(queryById);
        }
        return methodName;
    }

    @Override
    public String generateQueryByIdsEachIdMethodToMapper(GenerateMethodToMapperArgs args) {
        String methodName = null;
        if (args.getTableStructureAnalysisDto().getIdProperties().size() == 1) {
            methodName = antiDuplicationService.getNewMethodNameIfExist("queryByIdsEachId", args.getMapper());
            MethodDeclaration queryByIdsEachId = new MethodDeclaration();
            String comment = concatMapperMethodComment(args.getTableStructureAnalysisDto(),
                    "根据多个ID查询，并以ID作为key映射到Map");
            PropertyDto onlyPk = Iterables.getOnlyElement(args.getTableStructureAnalysisDto().getIdProperties());
            String varName = MoreStringUtils.toLowerCamel(onlyPk.getPropertyName());
            String pkTypeName = onlyPk.getJavaType().getSimpleName();
            queryByIdsEachId.addAnnotation(
                    parseAnnotation("@org.apache.ibatis.annotations.MapKey(\"" + varName + "\")"));
            queryByIdsEachId.setType(parseType(
                    "java.util.Map<" + pkTypeName + ", " + args.getEntityGeneration().getJavabeanQualifier() + ">"));
            queryByIdsEachId.setName(methodName);
            String varsName = English.plural(varName);
            queryByIdsEachId.addParameter(parseParameter(
                    "@org.apache.ibatis.annotations.Param(\"" + varsName + "\") List<" + pkTypeName + "> " + varsName));
            queryByIdsEachId.setBody(null);
            queryByIdsEachId.setJavadocComment(comment);
            args.getMapper().getMembers().addLast(queryByIdsEachId);
        }
        return methodName;
    }

    @Override
    public String generateQueryByIdsMethodToMapper(GenerateMethodToMapperArgs args) {
        String methodName = null;
        if (args.getTableStructureAnalysisDto().getIdProperties().size() == 1) {
            methodName = antiDuplicationService.getNewMethodNameIfExist("queryByIds", args.getMapper());
            MethodDeclaration queryByIds = new MethodDeclaration();
            String comment = concatMapperMethodComment(args.getTableStructureAnalysisDto(), "根据多个ID查询");
            PropertyDto onlyPk = Iterables.getOnlyElement(args.getTableStructureAnalysisDto().getIdProperties());
            queryByIds.setType(parseType("java.util.List<" + args.getEntityGeneration().getJavabeanQualifier() + ">"));
            queryByIds.setName(methodName);
            String varsName = English.plural(MoreStringUtils.toLowerCamel(onlyPk.getPropertyName()));
            Parameter parameter = parseParameter(
                    "@org.apache.ibatis.annotations.Param(\"" + varsName + "\") java.util.List<" + onlyPk.getJavaType()
                            .getSimpleName() + "> " + varsName);
            queryByIds.addParameter(parameter);
            queryByIds.setBody(null);
            queryByIds.setJavadocComment(comment);
            args.getMapper().getMembers().addLast(queryByIds);
        }
        return methodName;
    }

    @Override
    public String generateQueryByKeyMethodToMapper(GenerateMethodToMapperArgs generateMethodToMapper) {
        String methodName = antiDuplicationService.getNewMethodNameIfExist(
                "queryBy" + MoreStringUtils.toUpperCamel(generateMethodToMapper.getKey().getPropertyName()),
                generateMethodToMapper.getMapper());
        MethodDeclaration method = new MethodDeclaration();
        String comment = concatMapperMethodComment(generateMethodToMapper.getTableStructureAnalysisDto(),
                "根据「" + generateMethodToMapper.getKey().getDescription() + "」查询");
        method.setType(parseType(
                "java.util.List<" + generateMethodToMapper.getEntityGeneration().getJavabeanQualifier() + ">"));
        method.setName(methodName);
        String varName = MoreStringUtils.toLowerCamel(generateMethodToMapper.getKey().getPropertyName());
        Parameter parameter = parseParameter(
                generateMethodToMapper.getKey().getJavaType().getSimpleName() + " " + varName);
        method.addParameter(parameter);
        method.setBody(null);
        method.setJavadocComment(comment);
        generateMethodToMapper.getMapper().getMembers().addLast(method);
        return methodName;
    }

    @Override
    public QueryByKeysDto generateQueryByKeysMethodToMapper(GenerateMethodToMapperArgs args) {
        String methodName = antiDuplicationService.getNewMethodNameIfExist(
                "queryBy" + English.plural(MoreStringUtils.toUpperCamel(args.getKey().getPropertyName())),
                args.getMapper());
        MethodDeclaration method = new MethodDeclaration();
        String comment = concatMapperMethodComment(args.getTableStructureAnalysisDto(),
                "根据多个「" + args.getKey().getDescription() + "」查询");
        method.setType(parseType("java.util.List<" + args.getEntityGeneration().getJavabeanQualifier() + ">"));
        method.setName(methodName);
        String typeName = "java.util.List<" + args.getKey().getJavaType().getSimpleName() + ">";
        String varsName = English.plural(MoreStringUtils.toLowerCamel(args.getKey().getPropertyName()));
        String paramAnno = "@org.apache.ibatis.annotations.Param(\"" + varsName + "\")";
        Parameter parameter = parseParameter(paramAnno + " " + typeName + " " + varsName);
        method.addParameter(parameter);
        method.setBody(null);
        method.setJavadocComment(comment);
        args.getMapper().getMembers().addLast(method);
        return new QueryByKeysDto().setKey(args.getKey()).setMethodName(methodName).setVarsName(varsName);
    }

    @Override
    public String generateUpdateByIdEvenNullMethodToMapper(GenerateMethodToMapperArgs args) {
        String methodName = null;
        if (CollectionUtils.isNotEmpty(args.getTableStructureAnalysisDto().getIdProperties())) {
            methodName = antiDuplicationService.getNewMethodNameIfExist("updateByIdEvenNull", args.getMapper());
            MethodDeclaration updateByIdEvenNull = new MethodDeclaration();
            String comment = concatMapperMethodComment(args.getTableStructureAnalysisDto(),
                    "根据ID更新数据，为null属性对应的字段会被更新为null");
            updateByIdEvenNull.setJavadocComment(comment);
            updateByIdEvenNull.setType(PrimitiveType.intType());
            updateByIdEvenNull.setName(methodName);
            updateByIdEvenNull.addParameter(args.getEntityGeneration().getJavabeanQualifier(), "entity");
            updateByIdEvenNull.setBody(null);
            args.getMapper().getMembers().addLast(updateByIdEvenNull);
        }
        return methodName;
    }

    @Override
    public String generateUpdateByIdMethodToMapper(GenerateMethodToMapperArgs args) {
        String methodName = null;
        if (CollectionUtils.isNotEmpty(args.getTableStructureAnalysisDto().getIdProperties())) {
            methodName = antiDuplicationService.getNewMethodNameIfExist("updateById", args.getMapper());
            MethodDeclaration updateById = new MethodDeclaration();
            String comment = concatMapperMethodComment(args.getTableStructureAnalysisDto(),
                    "根据ID更新数据，忽略值为null的属性");
            updateById.setJavadocComment(comment);
            updateById.setType(PrimitiveType.intType());
            updateById.setName(methodName);
            updateById.addParameter(args.getEntityGeneration().getJavabeanQualifier(), "entity");
            updateById.setBody(null);
            args.getMapper().getMembers().addLast(updateById);
        }
        return methodName;
    }

    private String concatMapperMethodComment(TableStructureAnalysisDto persistence, String methodDescription) {
        String result = methodDescription;
        if (commonConfig.getEnableNoModifyAnnounce() || commonConfig.getEnableLotNoAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE;
        }
        if (commonConfig.getEnableNoModifyAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.NO_MODIFY_ANNOUNCE;
        }
        if (commonConfig.getEnableLotNoAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + persistence.getLotNo();
        }
        return result;
    }

}