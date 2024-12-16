package com.spldeolin.allison1875.docanalyzer.service;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.AnalyzeMvcHandlerRetval;
import com.spldeolin.allison1875.docanalyzer.javabean.MvcHandlerDTO;
import com.spldeolin.allison1875.docanalyzer.service.impl.MvcHandlerAnalyzerServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(MvcHandlerAnalyzerServiceImpl.class)
public interface MvcHandlerAnalyzerService {

    AnalyzeMvcHandlerRetval analyzeMvcHandler(ClassOrInterfaceDeclaration mvcControllerCoid, MvcHandlerDTO mvcHandler);

}