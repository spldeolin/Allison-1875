package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;
import lombok.Getter;

/**
 * 根据外键删除
 *
 * 表中每有几个外键，这个Proc就生成几个方法，以_id结尾的字段算作外键
 *
 * @author Deolin 2020-07-19
 */
public class DeleteByFkXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    @Getter
    private Collection<String> sourceCodeLines;

    public DeleteByFkXmlProc(PersistenceDto persistence) {
        this.persistence = persistence;
    }

    public DeleteByFkXmlProc process() {
        String deletedSql = PersistenceGeneratorConfig.getInstace().getDeletedSql();
        if (deletedSql != null) {
            if (persistence.getFkProperties().size() > 0) {
                sourceCodeLines = Lists.newArrayList();
                for (PropertyDto fk : persistence.getFkProperties()) {
                    Element stmt = new DefaultElement("update");
                    stmt.addAttribute("id", "deleteBy" + StringUtils.upperFirstLetter(fk.getPropertyName()));
                    stmt.addAttribute("parameterType", getParameterType(fk));
                    stmt.addText(Constant.newLine).addText(Constant.singleIndent);
                    stmt.addText("UPDATE ").addText(persistence.getTableName());
                    stmt.addText(Constant.newLine).addText(Constant.singleIndent);
                    stmt.addText("SET ").addText(deletedSql);
                    stmt.addText(Constant.newLine).addText(Constant.singleIndent);
                    stmt.addText("WHERE ");
                    stmt.addText(Constant.newLine).addText(Constant.singleIndent);
                    if (PersistenceGeneratorConfig.getInstace().getNotDeletedSql() != null) {
                        stmt.addText(PersistenceGeneratorConfig.getInstace().getNotDeletedSql());
                        stmt.addText(Constant.newLine).addText(Constant.singleIndent);
                        stmt.addText("AND ");
                    }
                    stmt.addText(fk.getColumnName() + " = #{" + fk.getPropertyName() + "}");
                    stmt.addText(Constant.newLine);
                    sourceCodeLines.addAll(StringUtils.splitLineByLine(Dom4jUtils.toSourceCode(stmt)));
                }

            }
        }
        return this;
    }

}