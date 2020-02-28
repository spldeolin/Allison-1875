package com.spldeolin.allison1875.si.dto;

import java.time.LocalDateTime;
import com.github.javaparser.ast.Node;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.util.ast.Cus;
import com.spldeolin.allison1875.base.util.ast.Locations;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-02-22
 */
@Data
@Accessors(chain = true)
public class LawlessDto {

    private Integer no;

    private String sourceCode;

    /**
     * - type qualifier
     * - field qualifier
     * - method qualifier
     * - or else null
     */
    private String qualifier;

    private String statuteNo;

    private String message;

    private String author;

    private String fixer;

    private LocalDateTime fixedAt;

    public LawlessDto(Node node, String qualifier) {
        sourceCode = Locations.getRelativePathWithLineNo(node);
        this.qualifier = qualifier;
        author = Cus.getAuthor(node.findCompilationUnit().orElseThrow(CuAbsentException::new));
    }

    public LawlessDto(Node node) {
        sourceCode = Locations.getRelativePathWithLineNo(node);
        author = Cus.getAuthor(node.findCompilationUnit().orElseThrow(CuAbsentException::new));
    }

}