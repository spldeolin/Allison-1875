package com.spldeolin.allison1875.handlergenerator.meta;

import java.nio.file.Path;
import java.util.Collection;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.JsonUtils;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-06-26
 */
@Data
@Accessors(chain = true)
public class HandlerMetaInfo {

    private Path sourceRoot;

    private String controllerPackage;

    private ClassOrInterfaceDeclaration controller;

    private String handlerName = "";

    private String author = "";

    private String handlerDescription = "";

    private String serviceName = "";

    private Collection<String> imports = Lists.newArrayList();

    private DtoMetaInfo reqBodyDto;

    private DtoMetaInfo respBodyDto;

    private String autowiredServiceField;

    private String callServiceExpr;

    private Collection<DtoMetaInfo> dtos = Lists.newArrayList();

    public String toString() {
        return JsonUtils.toJsonPrettily(this);
    }

}