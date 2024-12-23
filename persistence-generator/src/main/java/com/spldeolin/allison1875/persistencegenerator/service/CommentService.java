package com.spldeolin.allison1875.persistencegenerator.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.dto.InformationSchemaDTO;
import com.spldeolin.allison1875.persistencegenerator.service.impl.CommentServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(CommentServiceImpl.class)
public interface CommentService {

    String analyzeColumnComment(InformationSchemaDTO infoSchema);

    String analyzeTableComment(InformationSchemaDTO infoSchema);

}