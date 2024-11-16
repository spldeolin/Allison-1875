package com.spldeolin.allison1875.persistencegenerator.service;

import java.util.Optional;
import com.github.javaparser.ast.CompilationUnit;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.javabean.GenerateDesignArgs;
import com.spldeolin.allison1875.persistencegenerator.javabean.GenerateDesignRetval;
import com.spldeolin.allison1875.persistencegenerator.javabean.GenerateJoinDesignArgs;
import com.spldeolin.allison1875.persistencegenerator.javabean.TableStructureAnalysisDto;
import com.spldeolin.allison1875.persistencegenerator.service.impl.DesignGeneratorServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(DesignGeneratorServiceImpl.class)
public interface DesignGeneratorService {

    String concatDesignName(TableStructureAnalysisDto persistence);

    GenerateDesignRetval generateDesign(GenerateDesignArgs args);

    Optional<CompilationUnit> generateJoinDesign(GenerateJoinDesignArgs args);

}