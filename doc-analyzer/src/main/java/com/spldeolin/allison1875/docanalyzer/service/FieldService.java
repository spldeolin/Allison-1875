package com.spldeolin.allison1875.docanalyzer.service;

import com.google.common.collect.Table;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeFieldVarsRetval;
import com.spldeolin.allison1875.docanalyzer.service.impl.FieldServiceImpl;

/**
 * @author Deolin 2024-02-23
 */
@ImplementedBy(FieldServiceImpl.class)
public interface FieldService {

    Table<String/*classQualifier*/, String/*fieldVarName*/, AnalyzeFieldVarsRetval> analyzeFieldVars();

}