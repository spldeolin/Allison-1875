package com.spldeolin.allison1875.docanalyzer.service;

import java.util.List;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.AnalyzeEnumConstantsRetval;
import com.spldeolin.allison1875.docanalyzer.service.impl.EnumServiceImpl;

/**
 * @author Deolin 2024-02-26
 */
@ImplementedBy(EnumServiceImpl.class)
public interface EnumService {

    List<AnalyzeEnumConstantsRetval> analyzeEnumConstants(VariableDeclarator fieldVar);

}