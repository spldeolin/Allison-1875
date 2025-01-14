package com.spldeolin.allison1875.common.ast;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2023-05-26
 */
@Slf4j
public class FileFlush {

    private File src;

    private String newContent;

    private FileFlush() {
    }

    public static FileFlush build(CompilationUnit cu) {
        FileFlush result = new FileFlush();
        result.src = CompilationUnitUtils.getCuAbsolutePath(cu).toFile();
        result.newContent = cu.toString();
        return result;
    }

    public static FileFlush buildLexicalPreserving(CompilationUnit cu) {
        FileFlush result = new FileFlush();
        result.src = CompilationUnitUtils.getCuAbsolutePath(cu).toFile();
        result.newContent = LexicalPreservingPrinter.print(cu);
        return result;
    }

    public static FileFlush build(File src, String newContent) {
        FileFlush result = new FileFlush();
        result.src = src;
        result.newContent = newContent;
        return result;
    }

    public void flush() {
        try {
            FileUtils.writeStringToFile(src, newContent, StandardCharsets.UTF_8);
            log.info("File flushed, path={}", src.getPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}