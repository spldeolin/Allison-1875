package com.spldeolin.allison1875.pg.processor.mapperxml;

import java.util.Collection;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.google.common.collect.Iterables;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.pg.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.pg.javabean.PersistenceDto;
import com.spldeolin.allison1875.pg.javabean.PropertyDto;
import com.spldeolin.allison1875.pg.util.Dom4jUtils;
import lombok.Getter;

/**
 * 这个Proc生成2中方法：
 * 1. 根据主键列表查询
 * 2. 根据主键列表查询，并把结果集以主键为key，映射到Map中
 *
 * @author Deolin 2020-07-19
 */
public class QueryByPksXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private final String tagId;

    @Getter
    private Collection<String> sourceCodeLines;

    public QueryByPksXmlProc(PersistenceDto persistence, String tagId) {
        this.persistence = persistence;
        this.tagId = tagId;
    }

    public QueryByPksXmlProc process() {
        if (persistence.getPkProperties().size() == 1) {
            PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getPkProperties());
            Element stmt = new DefaultElement("select");
            stmt.addAttribute("id", tagId);
            addParameterType(stmt, onlyPk);
            stmt.addAttribute("resultMap", "all");
            newLineWithIndent(stmt);
            stmt.addText("SELECT");
            stmt.addElement("include").addAttribute("refid", "all");
            newLineWithIndent(stmt);
            stmt.addText("FROM ").addText(persistence.getTableName());
            newLineWithIndent(stmt);
            stmt.addText("WHERE ");
            newLineWithIndent(stmt);
            if (PersistenceGeneratorConfig.getInstace().getNotDeletedSql() != null) {
                stmt.addText(PersistenceGeneratorConfig.getInstace().getNotDeletedSql());
                newLineWithIndent(stmt);
                stmt.addText("AND ");
            }
            stmt.addText(onlyPk.getColumnName()).addText(" IN (");
            stmt.addElement("foreach").addAttribute("collection", "ids").addAttribute("item", "id")
                    .addAttribute("separator", ",").addText("#{id}");
            stmt.addText(")");
            sourceCodeLines = StringUtils.splitLineByLine(Dom4jUtils.toSourceCode(stmt));
        }
        return this;
    }

}