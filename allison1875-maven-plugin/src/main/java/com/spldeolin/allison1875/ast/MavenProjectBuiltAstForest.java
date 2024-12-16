package com.spldeolin.allison1875.ast;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-01-19
 */
@Slf4j
public class MavenProjectBuiltAstForest implements AstForest {

    private final ClassLoader classLoader;

    private final File sourceRoot;

    public MavenProjectBuiltAstForest(ClassLoader classLoader, File sourceRoot) {
        Preconditions.checkNotNull(classLoader, "required Argument 'classLoader' must not be null");
        Preconditions.checkNotNull(sourceRoot, "required Argument 'sourceRoot' must not be null");
        try {
            sourceRoot = sourceRoot.getCanonicalFile();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        this.classLoader = classLoader;
        this.sourceRoot = sourceRoot;
        StaticJavaParser.getParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(new ClassLoaderTypeSolver(classLoader)));
        Thread.currentThread().setContextClassLoader(classLoader);
        log.info("AstForest created, sourceRoot={}", sourceRoot);
    }

    @Override
    public Iterator<CompilationUnit> iterator() {
        // java files
        Iterator<File> javaFilesItr = FileUtils.iterateFiles(sourceRoot, new String[]{"java"}, true);
        // cus
        Iterator<CompilationUnit> cusItr = Iterators.transform(javaFilesItr, CompilationUnitUtils::parseJava);
        return cusItr;
    }

    @Override
    public AstForest cloneWithResetting() {
        return new MavenProjectBuiltAstForest(classLoader, sourceRoot);
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public Path getSourceRoot() {
        return sourceRoot.toPath();
    }

    @Override
    public Optional<CompilationUnit> tryFindCu(String primaryTypeQualifier) {
        try {
            Path absPath = sourceRoot.toPath().resolve(qualifierToRelativePath(primaryTypeQualifier));
            if (!absPath.toFile().exists()) {
                log.debug("cu not exists, qualifier={}", primaryTypeQualifier);
                return Optional.empty();
            }

            return Optional.of(CompilationUnitUtils.parseJava(absPath.toFile()));
        } catch (Exception e) {
            log.debug("cannot find cu, qualifier={}", primaryTypeQualifier, e);
            return Optional.empty();
        }
    }

    private String qualifierToRelativePath(String qualifier) {
        return qualifier.replace('.', File.separatorChar) + ".java";
    }

}