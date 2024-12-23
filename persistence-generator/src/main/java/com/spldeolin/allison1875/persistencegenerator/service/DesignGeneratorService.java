package com.spldeolin.allison1875.persistencegenerator.service;

import java.util.Optional;
import com.github.javaparser.ast.CompilationUnit;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.dto.GenerateDesignArgs;
import com.spldeolin.allison1875.persistencegenerator.dto.GenerateDesignRetval;
import com.spldeolin.allison1875.persistencegenerator.dto.GenerateJoinChainArgs;
import com.spldeolin.allison1875.persistencegenerator.dto.TableStructureAnalysisDTO;
import com.spldeolin.allison1875.persistencegenerator.service.impl.DesignGeneratorServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(DesignGeneratorServiceImpl.class)
public interface DesignGeneratorService {

    String concatDesignName(TableStructureAnalysisDTO persistence);

    GenerateDesignRetval generateDesign(GenerateDesignArgs args);

    Optional<CompilationUnit> generateJoinChain(GenerateJoinChainArgs args);

}