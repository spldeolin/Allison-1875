package com.spldeolin.allison1875.persistencegenerator.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.javabean.TableStructureAnalysisDto;
import com.spldeolin.allison1875.persistencegenerator.service.impl.TableStructureAnalyzerServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(TableStructureAnalyzerServiceImpl.class)
public interface TableStructureAnalyzerService {

    List<TableStructureAnalysisDto> analyzeTableStructure();

}