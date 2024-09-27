package com.spldeolin.allison1875.common.ast;

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
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.spldeolin.allison1875.common.exception.CompilationUnitParseException;
import com.spldeolin.allison1875.common.service.AstFilterService;
import com.spldeolin.allison1875.common.service.impl.AcceptAllAstFilterService;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-01-19
 */
@Slf4j
public class MavenProjectBuiltAstForest implements AstForest {

    private final ClassLoader classLoader;

    private final File sourceRoot;

    private final AstFilterService astFilterService;

    public MavenProjectBuiltAstForest(ClassLoader classLoader, File sourceRoot) {
        this(classLoader, sourceRoot, null);
    }

    public MavenProjectBuiltAstForest(ClassLoader classLoader, File sourceRoot,
            AstFilterService astFilterService) {
        Preconditions.checkNotNull(classLoader, "required Argument 'classLoader' must not be null");
        Preconditions.checkNotNull(sourceRoot, "required Argument 'sourceRoot' must not be null");
        try {
            sourceRoot = sourceRoot.getCanonicalFile();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        this.classLoader = classLoader;
        this.sourceRoot = sourceRoot;
        this.astFilterService = MoreObjects.firstNonNull(astFilterService, new AcceptAllAstFilterService());
        StaticJavaParser.getParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(new ClassLoaderTypeSolver(classLoader)));
        Thread.currentThread().setContextClassLoader(classLoader);
        log.info("AstForest created, sourceRoot={}", sourceRoot);
    }

    @Override
    public Iterator<CompilationUnit> iterator() {
        // java files
        Iterator<File> javaFilesItr = FileUtils.iterateFiles(sourceRoot, new String[]{"java"}, true);
        // filtered java files
        javaFilesItr = Iterators.filter(javaFilesItr, astFilterService::accept);
        // cus
        Iterator<CompilationUnit> cusItr = Iterators.transform(javaFilesItr, CompilationUnitUtils::parseJava);
        // filtered cus
        cusItr = Iterators.filter(cusItr, astFilterService::accept);
        return cusItr;
    }

    @Override
    public AstForest cloneWithResetting() {
        return new MavenProjectBuiltAstForest(classLoader, sourceRoot, astFilterService);
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
    public Optional<CompilationUnit> findCu(String primaryTypeQualifier) {
        Path absPath;
        try {
            absPath = sourceRoot.toPath().resolve(qualifierToRelativePath(primaryTypeQualifier));
        } catch (Exception e) {
            log.warn("impossible path, qualifier={}", primaryTypeQualifier, e);
            return Optional.empty();
        }
        if (!absPath.toFile().exists()) {
            return Optional.empty();
        }
        CompilationUnit cu;
        try {
            cu = CompilationUnitUtils.parseJava(absPath.toFile());
        } catch (CompilationUnitParseException e) {
            log.warn("fail to parse cu, qualifier={}", primaryTypeQualifier, e);
            return Optional.empty();
        }
        if (!astFilterService.accept(cu)) {
            return Optional.empty();
        }
        return Optional.of(cu);
    }

    private String qualifierToRelativePath(String qualifier) {
        return qualifier.replace('.', File.separatorChar) + ".java";
    }

}