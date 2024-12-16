package com.spldeolin.allison1875.common.service.impl;

import java.util.List;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;
import com.spldeolin.allison1875.common.javabean.AddInjectFieldRetval;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.service.MemberAdderService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-02-14
 */
@Slf4j
@Singleton
public class MemberAdderServiceImpl implements MemberAdderService {

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public AddInjectFieldRetval addInjectField(String typeQualifier, String varName, ClassOrInterfaceDeclaration coid) {
        Preconditions.checkNotNull(typeQualifier);
        Preconditions.checkNotNull(varName);
        Preconditions.checkNotNull(coid);

        // 尝试在coid中找到同varName的field
        FieldDeclaration sameVarNameFd = null;
        VariableDeclarator sameVarNameVd = null;
        for (FieldDeclaration fd : coid.getFields()) {
            for (VariableDeclarator vd : fd.getVariables()) {
                if (vd.getNameAsString().equals(varName)) {
                    sameVarNameFd = fd;
                    sameVarNameVd = vd;
                }
            }
        }

        // 连名称一致的field都找不到，直接添加到coid并返回
        if (sameVarNameVd == null) {
            FieldDeclaration field = this.lastAddField(typeQualifier, varName, coid);
            field.addAnnotation(annotationExprService.springAutowired());
            log.info("add @Autowired FieldVar [{}] to {} [{}].", varName, coid.isInterface() ? "Interface" : "Class",
                    coid.getNameAsString());
            return new AddInjectFieldRetval().setField(field).setFieldVarName(varName);
        }

        // 找得到名称一致的field，尝试判断类型是否一致
        boolean isTypeSame;
        String describe;
        try {
            describe = sameVarNameVd.getType().resolve().describe();
            isTypeSame = typeQualifier.equals(describe);
        } catch (Exception e) {
            log.warn("fail to resolve and describe, considered not same, type={}", sameVarNameVd.getType(), e);
            // 悲观地视为类型不一致，确保不会出错
            isTypeSame = false;
        }

        // 找得到名称和类型都一致的field，无需添加直接返回
        if (isTypeSame) {
            log.info("find FieldVar [{}] in {} [{}] for using", varName, coid.isInterface() ? "Interface" : "Class",
                    coid.getNameAsString());
            return new AddInjectFieldRetval().setField(sameVarNameFd).setFieldVarName(varName);
        }

        // 找到名称一致但是类型不一致的field，等于varName被其他类型占用，只能改名继续添加
        String newVarName = varName + "Ex";
        log.info("FieldVar [{}] is duplicate in {} [{}], hence '{}' is used", varName,
                coid.isInterface() ? "Interface" : "Class", coid.getName(), newVarName);
        return addInjectField(typeQualifier, newVarName, coid);
    }

    @Override
    public AddInjectFieldRetval addInjectField(ClassOrInterfaceDeclaration toBeAdd, ClassOrInterfaceDeclaration coid) {
        String typeQualifier = toBeAdd.getFullyQualifiedName()
                .orElseThrow(() -> new Allison1875Exception("Node '" + toBeAdd.getName() + "' has no Qualifier"));
        String varName = MoreStringUtils.toLowerCamel(toBeAdd.getNameAsString());
        return this.addInjectField(typeQualifier, varName, coid);
    }

    private FieldDeclaration lastAddField(String typeQualifier, String varName, ClassOrInterfaceDeclaration coid) {
        FieldDeclaration field = new ClassOrInterfaceDeclaration().addField(typeQualifier, varName, Keyword.PRIVATE);
        List<FieldDeclaration> fields = coid.getFields();
        if (CollectionUtils.isNotEmpty(fields)) {
            coid.getMembers().addAfter(field, Iterables.getLast(fields));
        } else {
            coid.getMembers().add(0, field);
        }
        return field;
    }

}