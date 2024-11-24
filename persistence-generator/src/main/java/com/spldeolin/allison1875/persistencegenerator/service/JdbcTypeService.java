package com.spldeolin.allison1875.persistencegenerator.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.JavaTypeNamingDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.TableStructureAnalysisDto;
import com.spldeolin.allison1875.persistencegenerator.service.impl.JdbcTypeServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(JdbcTypeServiceImpl.class)
public interface JdbcTypeService {

    JavaTypeNamingDto jdbcType2javaType(InformationSchemaDto columnMeta,
            TableStructureAnalysisDto tableStructureAnalysis);

}