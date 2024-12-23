package com.spldeolin.allison1875.common.service;

import java.util.Map;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.dto.DataModelArg;
import com.spldeolin.allison1875.common.dto.DataModelGeneration;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceImpl;

/**
 * @author Deolin 2024-01-01
 */
@ImplementedBy(DataModelServiceImpl.class)
public interface DataModelService {

    DataModelGeneration generateDataModel(DataModelArg arg);

    Map<String/*qualifier*/, CompilationUnit> collectNestDataModels(TypeDeclaration<?> td);

}
