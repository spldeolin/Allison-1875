package com.spldeolin.allison1875.persistencegenerator.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.persistencegenerator.javabean.TableStructureAnalysisDto;
import com.spldeolin.allison1875.persistencegenerator.service.impl.EntityGeneratorServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(EntityGeneratorServiceImpl.class)
public interface EntityGeneratorService {

    JavabeanGeneration generateEntity(TableStructureAnalysisDto persistence);

}