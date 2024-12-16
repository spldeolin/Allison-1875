package com.spldeolin.allison1875.persistencegenerator.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.JavaTypeNamingDTO;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDTO;
import com.spldeolin.allison1875.persistencegenerator.javabean.TableStructureAnalysisDTO;
import com.spldeolin.allison1875.persistencegenerator.service.impl.JdbcTypeServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(JdbcTypeServiceImpl.class)
public interface JdbcTypeService {

    JavaTypeNamingDTO jdbcType2javaType(InformationSchemaDTO columnMeta,
            TableStructureAnalysisDTO tableStructureAnalysis);

}