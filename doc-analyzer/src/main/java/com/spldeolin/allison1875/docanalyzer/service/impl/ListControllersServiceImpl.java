package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.constant.ImportConstant;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.docanalyzer.constant.ControllerMarkerConstant;
import com.spldeolin.allison1875.docanalyzer.javabean.ControllerFullDto;
import com.spldeolin.allison1875.docanalyzer.service.ListControllersService;
import com.spldeolin.allison1875.docanalyzer.util.LoadClassUtils;
import lombok.extern.log4j.Log4j2;

/**
 * 内聚了 遍历AstForest中每一个controller的功能
 *
 * @author Deolin 2020-06-10
 */
@Singleton
@Log4j2
public class ListControllersServiceImpl implements ListControllersService {

    @Override
    public Collection<ControllerFullDto> process(AstForest astForest) {
        Collection<ControllerFullDto> result = Lists.newArrayList();
        for (CompilationUnit cu : astForest) {
            if (!Locations.getAbsolutePath(cu).startsWith(astForest.getPrimaryJavaRoot())) {
                // 非宿主controller
                continue;
            }
            for (ClassOrInterfaceDeclaration controller : cu.findAll(ClassOrInterfaceDeclaration.class,
                    this::isController)) {
                if (findIgnoreFlag(controller)) {
                    continue;
                }

                // 反射controller，如果失败那么这个controller就没有处理该controller的必要了
                Class<?> reflectionController;
                try {
                    reflectionController = tryReflectController(controller);
                } catch (ClassNotFoundException e) {
                    continue;
                }

                String controllerCat = findControllerCat(controller);

                result.add(new ControllerFullDto(controllerCat, controller, reflectionController));
            }
        }
        return result;
    }

    private String findControllerCat(ClassOrInterfaceDeclaration controller) {
        String controllerCat = findCat(controller);
        if (controllerCat == null) {
            controllerCat = JavadocDescriptions.getFirstLine(controller);
        }
        if (StringUtils.isEmpty(controllerCat)) {
            controllerCat = controller.getNameAsString();
        }
        return controllerCat;
    }

    private String findCat(NodeWithJavadoc<?> node) {
        for (String line : JavadocDescriptions.getAsLines(node)) {
            if (StringUtils.startsWithIgnoreCase(line, ControllerMarkerConstant.DOC_CAT)) {
                String catContent = StringUtils.removeStartIgnoreCase(line, ControllerMarkerConstant.DOC_CAT).trim();
                if (catContent.length() > 0) {
                    return catContent;
                }
            }
        }
        return null;
    }

    private boolean findIgnoreFlag(NodeWithJavadoc<?> node) {
        for (String line : JavadocDescriptions.getAsLines(node)) {
            if (StringUtils.startsWithIgnoreCase(line, ControllerMarkerConstant.DOC_IGNORE)) {
                return true;
            }
        }
        return false;
    }

    private boolean isController(ClassOrInterfaceDeclaration coid) {
        for (AnnotationExpr annotation : coid.getAnnotations()) {
            try {
                ResolvedAnnotationDeclaration resolve = annotation.resolve();
                if (resolve.hasAnnotation(ImportConstant.SPRING_CONTROLLER.getNameAsString())
                        || ImportConstant.SPRING_CONTROLLER.getNameAsString().equals(resolve.getQualifiedName())) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("annotation [{}] of class [{}] cannot resolve", annotation.getNameAsString(),
                        coid.getNameAsString(), e);
            }
        }
        return false;
    }

    private Class<?> tryReflectController(ClassOrInterfaceDeclaration controller) throws ClassNotFoundException {
        String qualifier = controller.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new);
        try {
            return LoadClassUtils.loadClass(qualifier, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            log.error("cannot load class [{}]", qualifier);
            throw e;
        }
    }

}
