package com.spldeolin.allison1875.querytransformer.service.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.exception.PrimaryTypeAbsentException;
import com.spldeolin.allison1875.base.service.AntiDuplicationService;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.service.FindMapperService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-08-27
 */
@Singleton
@Slf4j
public class FindMapperServiceImpl implements FindMapperService {

    @Override
    public ClassOrInterfaceDeclaration findMapper(AstForest astForest, DesignMeta designMeta) {
        CompilationUnit cu = astForest.findCu(designMeta.getMapperQualifier());
        if (cu == null) {
            return null;
        }
        return cu.getPrimaryType().orElseThrow(PrimaryTypeAbsentException::new).asClassOrInterfaceDeclaration();
    }

}