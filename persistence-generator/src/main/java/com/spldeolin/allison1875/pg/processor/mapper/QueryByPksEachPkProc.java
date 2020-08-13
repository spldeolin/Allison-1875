package com.spldeolin.allison1875.pg.processor.mapper;

import static com.github.javaparser.StaticJavaParser.parseParameter;
import static com.github.javaparser.StaticJavaParser.parseType;

import org.atteo.evo.inflector.English;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.google.common.collect.Iterables;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.pg.constant.Constant;
import com.spldeolin.allison1875.pg.javabean.PersistenceDto;
import com.spldeolin.allison1875.pg.javabean.PropertyDto;
import lombok.Getter;

/**
 * 根据主键列表查询，并把结果集以主键为key，映射到Map中
 *
 * 表是联合主键时，这个Proc不生成方法
 *
 * @author Deolin 2020-07-18
 */
public class QueryByPksEachPkProc extends MapperProc {

    private final PersistenceDto persistence;

    private final ClassOrInterfaceDeclaration mapper;

    @Getter
    private Boolean generateOrNot = true;

    public QueryByPksEachPkProc(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        this.persistence = persistence;
        this.mapper = mapper;
    }

    public QueryByPksEachPkProc process() {
        if (persistence.getPkProperties().size() == 1) {
            if (super.existDeclared(mapper, "queryByIdsEachId")) {
                generateOrNot = false;
                return this;
            }
            MethodDeclaration queryByIdsEachId = new MethodDeclaration();
            queryByIdsEachId.setJavadocComment(
                    new JavadocComment("根据ID查询，并以ID为key映射到Map" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
            Imports.ensureImported(mapper, "org.apache.ibatis.annotations.MapKey");
            Imports.ensureImported(mapper, "java.util.Map");
            Imports.ensureImported(mapper, "java.util.Collection");
            Imports.ensureImported(mapper, "org.apache.ibatis.annotations.Param");
            PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getPkProperties());
            String varName = StringUtils.lowerFirstLetter(onlyPk.getPropertyName());
            String pkTypeName = onlyPk.getJavaType().getSimpleName();
            queryByIdsEachId.setType(parseType(
                    "@MapKey(\"" + varName + "\")" + "Map<" + pkTypeName + ", " + persistence.getEntityName() + ">"));
            queryByIdsEachId.setName("queryByIdsEachId");
            String varsName = English.plural(varName);
            queryByIdsEachId.addParameter(
                    parseParameter("@Param(\"" + varsName + "\") Collection<" + pkTypeName + "> " + varsName));
            queryByIdsEachId.setBody(null);
            mapper.getMembers().addLast(queryByIdsEachId);
        }
        return this;
    }

}