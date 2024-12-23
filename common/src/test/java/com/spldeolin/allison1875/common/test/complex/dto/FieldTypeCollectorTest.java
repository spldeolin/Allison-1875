package com.spldeolin.allison1875.common.test.complex.dto;

import java.io.File;
import java.util.Map;
import com.github.javaparser.ast.CompilationUnit;
import com.google.inject.Guice;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceImpl;
import com.spldeolin.allison1875.common.test.AstForestTestImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FieldTypeCollectorTest {

    public static void main(String[] args) {
        AstForest astForest = new AstForestTestImpl(new File("common/src/test/java"));
        AstForestContext.set(astForest);
        astForest.tryFindCu(SchoolDTO.class.getName()).flatMap(CompilationUnit::getPrimaryType).ifPresent(pt -> {

            Map<String, CompilationUnit> dtos = Guice.createInjector().getInstance(DataModelServiceImpl.class)
                    .collectNestDataModels(pt);

            dtos.forEach((qualifier, cu) -> {
                log.info(qualifier);
            });
        });
    }

}
