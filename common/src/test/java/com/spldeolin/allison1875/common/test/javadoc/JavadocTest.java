package com.spldeolin.allison1875.common.test.javadoc;

import java.io.File;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.test.AstForestTestImpl;
import com.spldeolin.allison1875.common.util.JavadocUtils;

/**
 * @author Deolin 2024-01-16
 */
public class JavadocTest {

    public static void main(String[] args) {
        AstForest astForest = new AstForestTestImpl(new File("common/src/test/java"));

        astForest.tryFindCu("com.spldeolin.allison1875.common.test.javadoc.TestSubject").ifPresent(cu -> {
            cu.getPrimaryType().ifPresent(primaryType -> {
                Javadoc javadoc = primaryType.getJavadoc().get();
                System.out.println(javadoc);

                JavadocUtils.getComment(primaryType);
                JavadocUtils.getComment(primaryType.getMethodsByName("m1").get(0));
                JavadocUtils.getComment(primaryType.getMethodsByName("m2").get(0));
                JavadocUtils.getAuthor(cu.findAll(FieldDeclaration.class,
                        fieldDeclaration -> fieldDeclaration.toString().equals("private String f1;")).get(0));

            });
        });
    }

}