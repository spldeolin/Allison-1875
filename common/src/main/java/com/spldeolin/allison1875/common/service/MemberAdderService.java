package com.spldeolin.allison1875.common.service;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.dto.AddInjectFieldRetval;
import com.spldeolin.allison1875.common.service.impl.MemberAdderServiceImpl;

/**
 * @author Deolin 2024-02-14
 */
@ImplementedBy(MemberAdderServiceImpl.class)
public interface MemberAdderService {

    /**
     * 为参数coid添加参数typeQualifier和参数varName所组成的Field
     *
     * @return 如果所组成的Field已在coid中存在，无需添加，则返回empty；否则返回新添加的FieldDeclaration对象
     */
    AddInjectFieldRetval addInjectField(String typeQualifier, String varName, ClassOrInterfaceDeclaration coid);

    /**
     * 为参数coid添加参数typeQualifier和参数varName所转化的Field
     *
     * @return 如果所组成的Field已在coid中存在，无需添加，则返回empty；否则返回新添加的FieldDeclaration对象
     */
    AddInjectFieldRetval addInjectField(ClassOrInterfaceDeclaration toBeAdd, ClassOrInterfaceDeclaration coid);

}