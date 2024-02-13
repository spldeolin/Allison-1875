package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.docanalyzer.constant.ControllerMarkerConstant;
import com.spldeolin.allison1875.docanalyzer.javabean.MvcControllerDto;
import com.spldeolin.allison1875.docanalyzer.javabean.MvcHandlerDto;
import com.spldeolin.allison1875.docanalyzer.service.MvcControllerDetectorService;
import com.spldeolin.allison1875.docanalyzer.service.MvcHandlerDetectorService;
import com.spldeolin.allison1875.docanalyzer.util.MethodQualifierUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 内聚了 遍历Class controllerClass下handler的功能
 *
 * @author Deolin 2020-06-10
 */
@Slf4j
public class MvcHandlerDetectorServiceImpl implements MvcHandlerDetectorService {

    @Inject
    private MvcControllerDetectorService mvcControllerDetectorService;

    @Override
    public List<MvcHandlerDto> detectMvcHandler(AstForest astForest) {
        List<MvcControllerDto> controllers = mvcControllerDetectorService.detectMvcControllers(astForest);

        List<MvcHandlerDto> result = Lists.newArrayList();
        for (MvcControllerDto controller : controllers) {
            Map<String/*shortestQualifiedSignature*/, MethodDeclaration> mds = this.listMethods(controller.getCoid());

            for (Method reflectionMethod : controller.getReflection().getDeclaredMethods()) {
                if (!this.isMvcHandler(reflectionMethod)) {
                    continue;
                }

                MethodDeclaration mvcHandlerMd = mds.get(
                        MethodQualifierUtils.getShortestQualifiedSignature(reflectionMethod));
                if (mvcHandlerMd == null) {
                    // 可能是源码删除了某个handler但未编译，所以reflectionMethod存在，但MethodDeclaration已经不存在了
                    // 这种情况没有继续处理该handler的必要了
                    continue;
                }

                if (findIgnoreFlag(mvcHandlerMd)) {
                    continue;
                }

                // doc-cat标志
                String handlerCat = findCat(mvcHandlerMd);
                if (handlerCat == null) {
                    handlerCat = controller.getCat();
                }

                result.add(new MvcHandlerDto(handlerCat, mvcHandlerMd, reflectionMethod, controller));
            }
        }
        return result;
    }

    private boolean isMvcHandler(Method method) {
        return AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class) != null;
    }

    private boolean findIgnoreFlag(NodeWithJavadoc<?> node) {
        for (String line : JavadocUtils.getCommentAsLines(node)) {
            if (org.apache.commons.lang3.StringUtils.startsWithIgnoreCase(line, ControllerMarkerConstant.DOC_IGNORE)) {
                return true;
            }
        }
        return false;
    }

    private String findCat(NodeWithJavadoc<?> node) {
        for (String line : JavadocUtils.getCommentAsLines(node)) {
            if (org.apache.commons.lang3.StringUtils.startsWithIgnoreCase(line, ControllerMarkerConstant.DOC_CAT)) {
                String catContent = org.apache.commons.lang3.StringUtils.removeStartIgnoreCase(line,
                        ControllerMarkerConstant.DOC_CAT).trim();
                if (catContent.length() > 0) {
                    return catContent;
                }
            }
        }
        return null;
    }

    Map<String, MethodDeclaration> listMethods(ClassOrInterfaceDeclaration mvcControllerCoid) {
        Map<String, MethodDeclaration> methods = Maps.newHashMap();
        for (MethodDeclaration method : mvcControllerCoid.findAll(MethodDeclaration.class)) {
            try {
                methods.put(MethodQualifierUtils.getShortestQualifiedSignature(method), method);
            } catch (Exception e) {
                log.warn("fail to get shortest qualified signature [{}]", method.getNameAsString(), e);
            }
        }
        return methods;
    }

}