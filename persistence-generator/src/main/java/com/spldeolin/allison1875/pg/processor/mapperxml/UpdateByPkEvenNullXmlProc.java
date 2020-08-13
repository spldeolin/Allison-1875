package com.spldeolin.allison1875.pg.processor.mapperxml;

import java.util.Collection;
import java.util.stream.Collectors;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.google.common.base.Strings;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.pg.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.pg.javabean.PersistenceDto;
import com.spldeolin.allison1875.pg.processor.mapper.UpdateByPkEvenNullProc;
import com.spldeolin.allison1875.pg.util.Dom4jUtils;
import lombok.Getter;

/**
 * 根据主键更新，即便属性的值为null，也更新为null
 *
 * @author Deolin 2020-07-19
 */
public class UpdateByPkEvenNullXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private final String entityName;

    private final UpdateByPkEvenNullProc updateByPkEvenNullProc;

    @Getter
    private Collection<String> sourceCodeLines;

    public UpdateByPkEvenNullXmlProc(PersistenceDto persistence, String entityName,
            UpdateByPkEvenNullProc updateByPkEvenNullProc) {
        this.persistence = persistence;
        this.entityName = entityName;
        this.updateByPkEvenNullProc = updateByPkEvenNullProc;
    }

    public UpdateByPkEvenNullXmlProc process() {
        if (updateByPkEvenNullProc.getGenerateOrNot() && persistence.getPkProperties().size() > 0) {
            Element stmt = new DefaultElement("update");
            stmt.addAttribute("id", "updateByIdEvenNull");
            stmt.addAttribute("parameterType", entityName);
            newLineWithIndent(stmt);
            stmt.addText("UPDATE ").addText(persistence.getTableName());
            newLineWithIndent(stmt);
            stmt.addText("SET ");
            newLineWithIndent(stmt);
            stmt.addText(BaseConstant.SINGLE_INDENT);
            stmt.addText(persistence.getNonPkProperties().stream()
                    .map(npk -> npk.getColumnName() + " = #{" + npk.getPropertyName() + "}").collect(Collectors
                            .joining(", " + BaseConstant.NEW_LINE + Strings.repeat(BaseConstant.SINGLE_INDENT, 2))));
            newLineWithIndent(stmt);
            stmt.addText("WHERE ");
            newLineWithIndent(stmt);
            if (PersistenceGeneratorConfig.getInstace().getNotDeletedSql() != null) {
                stmt.addText(PersistenceGeneratorConfig.getInstace().getNotDeletedSql());
                newLineWithIndent(stmt);
                stmt.addText("AND ");
            }
            stmt.addText(persistence.getPkProperties().stream()
                    .map(pk -> pk.getColumnName() + " = #{" + pk.getPropertyName() + "}")
                    .collect(Collectors.joining(" AND ")));
            sourceCodeLines = Dom4jUtils.toSourceCodeLines(stmt);
        }
        return this;
    }

}