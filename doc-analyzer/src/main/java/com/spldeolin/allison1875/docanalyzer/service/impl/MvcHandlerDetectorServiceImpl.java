package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.exception.QualifierAbsentException;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.javabean.MvcControllerDTO;
import com.spldeolin.allison1875.docanalyzer.javabean.MvcHandlerDTO;
import com.spldeolin.allison1875.docanalyzer.service.MvcHandlerDetectorService;
import com.spldeolin.allison1875.docanalyzer.util.LoadClassUtils;
import com.spldeolin.allison1875.docanalyzer.util.MethodQualifierUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 内聚了 遍历Class controllerClass下handler的功能
 *
 * @author Deolin 2020-06-10
 */
@Slf4j
@Singleton
public class MvcHandlerDetectorServiceImpl implements MvcHandlerDetectorService {

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private DocAnalyzerConfig config;

    @Override
    public List<MvcHandlerDTO> detectMvcHandler() {
        List<MvcHandlerDTO> result = Lists.newArrayList();

        for (CompilationUnit cu : AstForestContext.get()) {
            if (!CompilationUnitUtils.getCuAbsolutePath(cu).startsWith(AstForestContext.get().getSourceRoot())) {
                // 非宿主controller
                continue;
            }
            for (ClassOrInterfaceDeclaration coid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                if (isNotController(coid)) {
                    continue;
                }

                // 反射controller，如果失败那么这个controller就没有继续处理的必要了
                Class<?> mvcControllerReflection;
                try {
                    mvcControllerReflection = tryReflectController(coid);
                } catch (ClassNotFoundException e) {
                    continue;
                }

                // 分类
                String mvcControllerCat = getControllerCat(coid);

                LinkedHashMap<String/*shortestQualifiedSignature*/, MethodDeclaration> mds = this.listMethods(coid);

                List<MvcHandlerDTO> mvcHandlers = Lists.newArrayList();
                for (Method methodReflection : mvcControllerReflection.getDeclaredMethods()) {
                    if (isNotMvcHandler(methodReflection)) {
                        continue;
                    }

                    MethodDeclaration mvcHandlerMd = mds.get(
                            MethodQualifierUtils.getShortestQualifiedSignature(methodReflection));

                    if (mvcHandlerMd == null) {
                        // 可能是源码删除了某个mvcHandler但未编译，所以反射对象存在，但ast对象已经不存在了
                        // 这种情况没有继续处理该mvcHandler的必要了
                        continue;
                    }

                    if (config.getMvcHandlerQualifierWildcards().stream().noneMatch(
                            wildcard -> MethodQualifierUtils.getTypeQualifierWithMethodName(mvcHandlerMd)
                                    .matches(convertGlobToRegex(wildcard)))) {
                        continue;
                    }

                    MvcControllerDTO mvcController = new MvcControllerDTO();
                    mvcController.setCat(mvcControllerCat);
                    mvcController.setCoid(coid);
                    mvcController.setReflection(mvcControllerReflection);
                    MvcHandlerDTO mvcHandler = new MvcHandlerDTO();
                    mvcHandler.setMvcController(mvcController);
                    mvcHandler.setCat(mvcControllerCat);
                    mvcHandler.setMd(mvcHandlerMd);
                    mvcHandler.setReflection(methodReflection);
                    mvcHandlers.add(mvcHandler);
                }

                mvcHandlers.sort(Comparator.comparingInt(student -> Lists.newArrayList(mds.keySet())
                        .indexOf(MethodQualifierUtils.getShortestQualifiedSignature(student.getMd()))));
                result.addAll(mvcHandlers);
            }
        }
        return result;
    }

    private boolean isNotMvcHandler(Method method) {
        return AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class) == null;
    }

    private LinkedHashMap<String, MethodDeclaration> listMethods(ClassOrInterfaceDeclaration mvcControllerCoid) {
        LinkedHashMap<String, MethodDeclaration> methods = Maps.newLinkedHashMap();
        for (MethodDeclaration method : mvcControllerCoid.findAll(MethodDeclaration.class)) {
            try {
                methods.put(MethodQualifierUtils.getShortestQualifiedSignature(method), method);
            } catch (Exception e) {
                log.warn("fail to get shortest qualified signature [{}]", method.getNameAsString(), e);
            }
        }
        return methods;
    }

    private boolean isNotController(ClassOrInterfaceDeclaration coid) {
        return !annotationExprService.isAnnotated(annotationExprService.springController().getNameAsString(), coid)
                && !annotationExprService.isAnnotated(annotationExprService.springRestController().getNameAsString(),
                coid);
    }

    private Class<?> tryReflectController(ClassOrInterfaceDeclaration controller)
            throws ClassNotFoundException {
        String qualifier = controller.getFullyQualifiedName()
                .orElseThrow(() -> new QualifierAbsentException(controller));
        try {
            return LoadClassUtils.loadClass(qualifier, AstForestContext.get().getClassLoader());
        } catch (ClassNotFoundException e) {
            log.error("cannot load class [{}]", qualifier, e);
            throw e;
        }
    }

    protected String getControllerCat(ClassOrInterfaceDeclaration controller) {
        String controllerCat = Iterables.getFirst(JavadocUtils.getCommentAsLines(controller), "");
        if (StringUtils.isEmpty(controllerCat)) {
            controllerCat = controller.getNameAsString();
        }
        return controllerCat;
    }

    private String convertGlobToRegex(String glob) {
        int length = glob.length();
        StringBuilder sb = new StringBuilder(length + 12);
        // Remove beginning and ending * globs because they're useless
//        if (glob.startsWith("*")) {
//            glob = glob.substring(1);
//            length--;
//        }
//        if (glob.endsWith("*")) {
//            glob = glob.substring(0, length - 1);
//        }
        boolean escaping = false;
        int inCurlies = 0;
        for (char currentChar : glob.toCharArray()) {
            switch (currentChar) {
                case '*':
                    if (escaping) {
                        sb.append("\\*");
                    } else {
                        sb.append(".*");
                    }
                    escaping = false;
                    break;
                case '?':
                    if (escaping) {
                        sb.append("\\?");
                    } else {
                        sb.append('.');
                    }
                    escaping = false;
                    break;
                case '.':
                case '(':
                case ')':
                case '+':
                case '|':
                case '^':
                case '$':
                case '@':
                case '%':
                    sb.append('\\');
                    sb.append(currentChar);
                    escaping = false;
                    break;
                case '\\':
                    if (escaping) {
                        sb.append("\\\\");
                        escaping = false;
                    } else {
                        escaping = true;
                    }
                    break;
                case '{':
                    if (escaping) {
                        sb.append("\\{");
                    } else {
                        sb.append('(');
                        inCurlies++;
                    }
                    escaping = false;
                    break;
                case '}':
                    if (inCurlies > 0 && !escaping) {
                        sb.append(')');
                        inCurlies--;
                    } else if (escaping) {
                        sb.append("\\}");
                    } else {
                        sb.append("}");
                    }
                    escaping = false;
                    break;
                case ',':
                    if (inCurlies > 0 && !escaping) {
                        sb.append('|');
                    } else if (escaping) {
                        sb.append("\\,");
                    } else {
                        sb.append(",");
                    }
                    break;
                default:
                    escaping = false;
                    sb.append(currentChar);
            }
        }
        return "^" + sb + "$";
    }

}
