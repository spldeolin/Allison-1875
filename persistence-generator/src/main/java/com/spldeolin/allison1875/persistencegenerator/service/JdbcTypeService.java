package com.spldeolin.allison1875.persistencegenerator.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.dto.InformationSchemaDTO;
import com.spldeolin.allison1875.persistencegenerator.dto.TableStructureAnalysisDTO;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.JavaTypeNamingDTO;
import com.spldeolin.allison1875.persistencegenerator.service.impl.JdbcTypeServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(JdbcTypeServiceImpl.class)
public interface JdbcTypeService {

    JavaTypeNamingDTO jdbcType2javaType(InformationSchemaDTO columnMeta,
            TableStructureAnalysisDTO tableStructureAnalysis);

}