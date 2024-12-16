package com.spldeolin.allison1875.common.test.complex.javabean;

import java.io.File;
import com.github.javaparser.ast.CompilationUnit;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.NestJavabeanCollector;
import com.spldeolin.allison1875.common.test.AstForestTestImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FieldTypeCollectorTest {

    public static void main(String[] args) {
        AstForest astForest = new AstForestTestImpl(new File("common/src/test/java"));
        astForest.tryFindCu(SchoolDTO.class.getName()).flatMap(CompilationUnit::getPrimaryType).ifPresent(pt -> {

            NestJavabeanCollector fieldTypeCollector = new NestJavabeanCollector(astForest, pt);
            fieldTypeCollector.collect();
            fieldTypeCollector.getTypeQualfiers().forEach(log::info);

        });
    }

}
