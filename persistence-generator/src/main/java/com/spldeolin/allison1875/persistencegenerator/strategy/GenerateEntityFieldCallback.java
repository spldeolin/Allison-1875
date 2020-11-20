package com.spldeolin.allison1875.persistencegenerator.strategy;

import java.nio.file.Path;
import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;

/**
 * @author Deolin 2020-11-14
 */
public interface GenerateEntityFieldCallback {

    Collection<CompilationUnit> handleEntityField(PropertyDto propertyDto, FieldDeclaration field, Path sourceRoot);

}
