package com.spldeolin.allison1875.common.util;

import java.io.File;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.spldeolin.allison1875.common.exception.CompilationUnitParseException;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-12-06
 */
@Log4j2
public class CompilationUnitUtils {

    private CompilationUnitUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static CompilationUnit parseJava(File javaFile) throws CompilationUnitParseException {
        if (!javaFile.exists()) {
            throw new CompilationUnitParseException(String.format("javaFile [%s] not exists", javaFile));
        }
        try {
            CompilationUnit cu = StaticJavaParser.parse(javaFile);
            log.debug("CompilationUnit@{} <- SourceCode {}", cu.hashCode(),
                    LocationUtils.getStorage(cu).getSourceRoot().relativize(LocationUtils.getAbsolutePath(cu)));
            return cu;
        } catch (Exception e) {
            throw new CompilationUnitParseException(String.format("fail to parse [%s]", javaFile), e);
        }
    }

}