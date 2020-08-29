package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

import static com.github.javaparser.StaticJavaParser.parseParameter;
import static com.github.javaparser.StaticJavaParser.parseType;

import org.atteo.evo.inflector.English;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import lombok.Getter;

/**
 * 根据外键列表查询，表中每有几个外键，这个Proc就生成几个方法
 *
 * 以_id结尾的字段算作外键
 *
 * @author Deolin 2020-08-08
 */
public class QueryByKeysProc extends MapperProc {

    private final PersistenceDto persistence;

    @Getter
    private final PropertyDto key;

    private final ClassOrInterfaceDeclaration mapper;

    @Getter
    private String methodName;

    @Getter
    private String varsName;

    public QueryByKeysProc(PersistenceDto persistence, PropertyDto key, ClassOrInterfaceDeclaration mapper) {
        this.persistence = persistence;
        this.key = key;
        this.mapper = mapper;
    }

    public QueryByKeysProc process() {
        if (PersistenceGeneratorConfig.getInstance().getDisableQueryByKeys()) {
            return this;
        }
        methodName = calcMethodName(mapper,
                "queryBy" + English.plural(StringUtils.upperFirstLetter(key.getPropertyName())));
        MethodDeclaration method = new MethodDeclaration();
        method.setJavadocComment(
                new JavadocComment("根据多个" + key.getDescription() + "查询" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
        Imports.ensureImported(mapper, "java.util.List");
        method.setType(parseType("List<" + persistence.getEntityName() + ">"));
        method.setName(methodName);
        String typeName = "Collection<" + key.getJavaType().getSimpleName() + ">";
        varsName = English.plural(StringUtils.lowerFirstLetter(key.getPropertyName()));
        String paramAnno = "@Param(\"" + varsName + "\")";
        Parameter parameter = parseParameter(paramAnno + " " + typeName + " " + varsName);
        method.addParameter(parameter);
        method.setBody(null);
        mapper.getMembers().addLast(method);
        return this;
    }

}