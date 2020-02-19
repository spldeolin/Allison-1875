package com.spldeolin.allison1875.base.collection.ast;


import static com.spldeolin.allison1875.base.BaseConfig.CONFIG;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.CollectionStrategy;
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.classloader.ClassLoaderCollectionStrategy;
import com.spldeolin.allison1875.base.classloader.WarOrFatJarClassLoaderFactory;
import lombok.extern.log4j.Log4j2;

/**
 * CompilationUnit对象的收集器（基于GlobalCollectionStrategy提供的策略收集）
 *
 * @author Deolin 2020-02-03
 */
@Log4j2
class CompilationUnitCollector {

    Collection<CompilationUnit> collectIntoCollection(Path path) {
        long start = System.currentTimeMillis();
        Collection<CompilationUnit> result = Lists.newLinkedList();
        collectSoruceRoots(path, newCollectionStrategy()).forEach(sourceRoot -> parseSourceRoot(sourceRoot, result));
        log.info("(Summary) {} CompilationUnit has parsed and collected from [{}] elapsing {}ms.", result.size(),
                path.toAbsolutePath(), System.currentTimeMillis() - start);
        return result;
    }

    Map<Path, CompilationUnit> collectIntoMap(Collection<CompilationUnit> cus) {
        Map<Path, CompilationUnit> result = Maps.newHashMap();
        for (CompilationUnit cu : cus) {
            cu.getStorage().ifPresent(storage -> result.put(storage.getPath(), cu));
        }
        log.info("(Summary) {} CompilationUnit has collected into Map.", result.size());
        return result;
    }

    private CollectionStrategy newCollectionStrategy() {
        CollectionStrategy collectionStrategy;
        if (CONFIG.getDoNotCollectWithLoadingClass()) {
            log.info("Start collecting CompilationUnit with ParserCollectionStrategy.");
            collectionStrategy = new ParserCollectionStrategy();
        } else {
            log.info("Start collecting CompilationUnit with ClassLoaderCollectionStrategy.");
            collectionStrategy = new ClassLoaderCollectionStrategy(WarOrFatJarClassLoaderFactory.getClassLoader());
        }
        return collectionStrategy;
    }

    private Collection<SourceRoot> collectSoruceRoots(Path path, CollectionStrategy collectionStrategy) {
        ProjectRoot projectRoot = collectionStrategy.collect(path);
        projectRoot.addSourceRoot(path);
        return projectRoot.getSourceRoots();
    }

    private void parseSourceRoot(SourceRoot sourceRoot, Collection<CompilationUnit> all) {
        long start = System.currentTimeMillis();
        int count = 0;
        for (ParseResult<CompilationUnit> parseResult : sourceRoot.tryToParseParallelized()) {
            if (parseResult.isSuccessful()) {
                parseResult.getResult().ifPresent(all::add);
                count++;
            } else {
                log.warn("Parse with problem, ignore and continue. [{}]", parseResult.getProblems());
            }
        }

        if (count > 0) {
            log.info("(Detail) {} CompilationUnit has parsed and collected from [{}] elapsing {}ms.", count,
                    CONFIG.getProjectPath().relativize(sourceRoot.getRoot()), System.currentTimeMillis() - start);
        }
    }

}
