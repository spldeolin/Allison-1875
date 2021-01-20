package com.spldeolin.allison1875.persistencegenerator.processor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.CollectionUtils;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;

/**
 * @author Deolin 2020-07-18
 */
@Singleton
public class MapperXmlProc {

    @Inject
    private AnchorProc anchorProc;

    public MapperXmlProc process(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper,
            Path mapperXmlDirectory, Collection<Collection<String>> sourceCodes) throws IOException {
        // find
        File mapperXmlFile = mapperXmlDirectory.resolve(persistence.getMapperName() + ".xml").toFile();

        if (!mapperXmlFile.exists()) {
            // create new File
            Collection<String> sourceCodeLines = Lists.newArrayList();
            sourceCodeLines.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            sourceCodeLines.add("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis"
                    + ".org/dtd/mybatis-3-mapper.dtd\">");
            sourceCodeLines.add(String.format("<mapper namespace=\"%s\">",
                    mapper.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new)));
            sourceCodeLines.add("</mapper>");
            FileUtils.writeLines(mapperXmlFile, sourceCodeLines);
        }

        List<String> newLines = Lists.newArrayList();

        String content = FileUtils.readFileToString(mapperXmlFile, StandardCharsets.UTF_8);
        List<String> lines = MoreStringUtils.splitLineByLine(content);
        List<String> generatedLines = getGeneratedLines(sourceCodes);

        if (content.contains(BaseConstant.BY_ALLISON_1875)) {
            boolean inAnchorRange = false;
            for (String line : lines) {
                if (!inAnchorRange) {
                    if (line.contains(BaseConstant.BY_ALLISON_1875)) {
                        // 从 范围外 进入
                        inAnchorRange = true;
                    } else {
                        newLines.add(line);
                    }
                } else {
                    if (line.contains(BaseConstant.BY_ALLISON_1875)) {
                        // 从 范围内 离开
                        inAnchorRange = false;
                        newLines.addAll(generatedLines);
                    }
                }
            }
        } else {
            Collections.reverse(lines);
            for (String line : lines) {
                newLines.add(line);
                if (line.contains("</mapper>")) {
                    Collections.reverse(generatedLines);
                    newLines.addAll(generatedLines);
                }
            }
            Collections.reverse(newLines);
        }

        String top = generatedLines.get(0);
        String bottom = Iterables.getLast(generatedLines);
        int topIndex = newLines.indexOf(top);
        if (topIndex > 1) {
            if (StringUtils.isNotBlank(newLines.get(topIndex - 1))) {
                newLines.add(topIndex, "");
            }
        }
        int bottomIndex = newLines.indexOf(bottom);
        if (bottomIndex < newLines.size() + 1) {
            if (StringUtils.isNotBlank(newLines.get(bottomIndex + 1))) {
                newLines.add(bottomIndex + 1, "");
            }
        }

        String leftAnchor = anchorProc.createLeftAnchor(mapperXmlFile, newLines);
        String rightAnchor = anchorProc.createRightAnchor(mapperXmlFile, newLines);

        String finalContent = Joiner.on(System.lineSeparator()).join(newLines).replace("${leftAnchor}", leftAnchor)
                .replace("${rightAnchor}", rightAnchor);

        FileUtils.writeStringToFile(mapperXmlFile, finalContent, StandardCharsets.UTF_8);
        return this;
    }

    private List<String> getGeneratedLines(Collection<Collection<String>> sourceCodes) {
        List<String> auto = Lists.newArrayList();
        auto.add(BaseConstant.SINGLE_INDENT + Constant.PROHIBIT_MODIFICATION_XML_BEGIN);
        for (Collection<String> sourceCode : sourceCodes) {
            if (CollectionUtils.isNotEmpty(sourceCode)) {
                for (String line : sourceCode) {
                    if (StringUtils.isNotBlank(line)) {
                        auto.add(BaseConstant.SINGLE_INDENT + line);
                    } else {
                        auto.add("");
                    }
                }
            }
        }
        if (auto.get(auto.size() - 1).equals("")) {
            auto.remove(auto.size() - 1);
        }
        auto.add(BaseConstant.SINGLE_INDENT + Constant.PROHIBIT_MODIFICATION_XML_END);
        return auto;
    }

}