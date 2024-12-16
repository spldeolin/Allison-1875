package com.spldeolin.allison1875.handlertransformer.service;

import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.handlertransformer.service.impl.DTOServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(DTOServiceImpl.class)
public interface DTOService {

    List<ClassOrInterfaceDeclaration> detectDTOBottomTop(BlockStmt initBody);

}