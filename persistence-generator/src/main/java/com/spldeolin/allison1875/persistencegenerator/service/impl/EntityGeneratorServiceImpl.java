package com.spldeolin.allison1875.persistencegenerator.service.impl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.javabean.FieldArg;
import com.spldeolin.allison1875.common.javabean.JavabeanArg;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.service.JavabeanGeneratorService;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.TableStructureAnalysisDto;
import com.spldeolin.allison1875.persistencegenerator.service.EntityGeneratorService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-07-18
 */
@Singleton
@Slf4j
public class EntityGeneratorServiceImpl implements EntityGeneratorService {

    @Inject
    private CommonConfig commonConfig;
    
    @Inject
    private PersistenceGeneratorConfig config;

    @Inject
    private JavabeanGeneratorService javabeanGeneratorService;

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public JavabeanGeneration generateEntity(TableStructureAnalysisDto persistence) {
        JavabeanArg arg = new JavabeanArg();
        arg.setAstForest(AstForestContext.get());
        arg.setPackageName(commonConfig.getEntityPackage());
        arg.setClassName(persistence.getEntityName());
        arg.setDescription(concatEntityDescription(persistence));
        arg.setAuthor(commonConfig.getAuthor());
        arg.setIsJavabeanSerializable(commonConfig.getIsJavabeanSerializable());
        arg.setIsJavabeanCloneable(commonConfig.getIsJavabeanCloneable());
        arg.setMore4Javabean((cu, javabean) -> {
            // 追加父类，并追加EqualsAndHashCode注解（如果需要的话）
            if (config.getSuperEntity() != null) {
                javabean.addExtendedType(config.getSuperEntity().getName());
                javabean.addAnnotation(annotationExprService.lombokEqualsAndHashCode());
                javabean.getAnnotations().removeIf(anno -> anno.getNameAsString().equals("Accessors"));
            }
        });
        for (PropertyDto property : persistence.getProperties()) {
            if (getSuperEntityFieldNames().contains(property.getColumnName())) {
                continue;
            }
            FieldArg fieldArg = new FieldArg();
            fieldArg.setDescription(cancatPropertyDescription(property));
            fieldArg.setTypeQualifier(property.getJavaType().getQualifier());
            fieldArg.setFieldName(property.getPropertyName());
            arg.getFieldArgs().add(fieldArg);
        }
        arg.setJavabeanExistenceResolution(config.getEntityExistenceResolution());
        return javabeanGeneratorService.generate(arg);
    }

    private List<String> getSuperEntityFieldNames() {
        if (config.getSuperEntity() == null) {
            return Lists.newArrayList();
        }
        return Arrays.stream(config.getSuperEntity().getDeclaredFields()).map(Field::getName)
                .collect(Collectors.toList());
    }

    private String concatEntityDescription(TableStructureAnalysisDto persistence) {
        String result = persistence.getDescrption() + BaseConstant.JAVA_DOC_NEW_LINE + persistence.getTableName();
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

    private String cancatPropertyDescription(PropertyDto property) {
        String result = property.getDescription();
        result += BaseConstant.JAVA_DOC_NEW_LINE + property.getColumnName();
        if (property.getLength() != null && property.getLength() != 0) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + "长度：" + property.getLength();
        }
        if (property.getNotnull()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + "不能为null";
        }
        if (property.getDefaultV() != null) {
            String defaultV = property.getDefaultV();
            if (!"CURRENT_TIMESTAMP".equals(defaultV)) {
                defaultV = "'" + defaultV + "'";
            }
            result += BaseConstant.JAVA_DOC_NEW_LINE + "默认：" + defaultV;
        }
        return result;
    }

}