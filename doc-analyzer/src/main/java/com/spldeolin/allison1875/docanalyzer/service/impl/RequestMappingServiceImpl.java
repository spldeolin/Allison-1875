package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeRequestMappingRetval;
import com.spldeolin.allison1875.docanalyzer.service.RequestMappingService;

/**
 * 内聚了 对请求URL和请求动词解析的功能
 *
 * @author Deolin 2020-06-10
 */
@Singleton
public class RequestMappingServiceImpl implements RequestMappingService {

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Inject
    private DocAnalyzerConfig config;

    @Override
    public AnalyzeRequestMappingRetval analyzeRequestMapping(Class<?> controllerClass, Method reflectionMethod) {
        RequestMapping controllerRequestMapping = findRequestMappingAnnoOrElseNull(controllerClass);
        String[] controllerPaths = findValueFromAnno(controllerRequestMapping);
        RequestMethod[] controllerVerbs = findVerbFromAnno(controllerRequestMapping);

        RequestMapping methodRequestMapping = findRequestMappingAnnoOrElseNull(reflectionMethod);
        String[] methodPaths = methodRequestMapping.value();
        RequestMethod[] methodVerbs = methodRequestMapping.method();

        // 将controller层的path与method层的path进行组合
        List<String> combinedUrls = combineUrl(controllerPaths, methodPaths);

        // 为组合后的url添加全局前缀
        String globalUrlPrefix = config.getGlobalUrlPrefix();
        if (StringUtils.isNotBlank(globalUrlPrefix)) {
            if (globalUrlPrefix.startsWith("/")) {
                combinedUrls.replaceAll(combineUrl -> globalUrlPrefix + combineUrl);
            } else {
                combinedUrls.replaceAll(combineUrl -> "/" + globalUrlPrefix + combineUrl);
            }
        }

        final MutableBoolean questionMark = new MutableBoolean(false);

        // 为组合后的url添加@RequestMapping.params条件
        combinedUrls.replaceAll(combinedUrl -> {
            StringBuilder sb = new StringBuilder(combinedUrl);
            if (controllerRequestMapping != null) {
                for (String param : controllerRequestMapping.params()) {
                    if (questionMark.isFalse()) {
                        questionMark.setTrue();
                        sb.append("?");
                    } else {
                        sb.append("&");
                    }
                    sb.append(param);
                }
            }
            for (String param : methodRequestMapping.params()) {
                if (questionMark.isFalse()) {
                    questionMark.setTrue();
                    sb.append("?");
                } else {
                    sb.append("&");
                }
                sb.append(param);
            }
            return sb.toString();
        });

        // 为组合后的url条件@RequestParam参数
        combinedUrls.replaceAll(combinedUrl -> {
            StringBuilder sb = new StringBuilder(combinedUrl);
            for (Parameter parameter : reflectionMethod.getParameters()) {
                RequestParam requestParam = AnnotatedElementUtils.findMergedAnnotation(parameter, RequestParam.class);
                if (requestParam != null && !MultipartFile.class.isAssignableFrom(parameter.getType())) {
                    if (questionMark.isFalse()) {
                        questionMark.setTrue();
                        sb.append("?");
                    } else {
                        sb.append("&");
                    }
                    sb.append(requestParam.value()).append("={").append(requestParam.value()).append("}");
                }
            }
            return sb.toString();
        });

        List<RequestMethod> combinedVerbs = combineVerb(controllerVerbs, methodVerbs);
        return new AnalyzeRequestMappingRetval(combinedUrls, combinedVerbs);
    }

    protected RequestMapping findRequestMappingAnnoOrElseNull(AnnotatedElement annotated) {
        return AnnotatedElementUtils.findMergedAnnotation(annotated, RequestMapping.class);
    }

    private String[] findValueFromAnno(RequestMapping controllerRequestMapping) {
        return controllerRequestMapping == null ? new String[0] : controllerRequestMapping.value();
    }

    private RequestMethod[] findVerbFromAnno(RequestMapping controllerRequestMapping) {
        return controllerRequestMapping == null ? new RequestMethod[0] : controllerRequestMapping.method();
    }

    private List<RequestMethod> combineVerb(RequestMethod[] cVerbs, RequestMethod[] mVerbs) {
        List<RequestMethod> combinedVerbs = Lists.newArrayList();
        if (ArrayUtils.isNotEmpty(cVerbs)) {
            combinedVerbs.addAll(Arrays.asList(cVerbs));
        }
        if (ArrayUtils.isNotEmpty(mVerbs)) {
            combinedVerbs.addAll(Arrays.asList(mVerbs));
        }
        if (CollectionUtils.isEmpty(combinedVerbs)) {
            combinedVerbs.addAll(Arrays.asList(RequestMethod.values()));
        }
        return combinedVerbs;
    }

    private List<String> combineUrl(String[] cPaths, String[] mPaths) {
        List<String> combinedUrls = Lists.newArrayList();
        if (ArrayUtils.isNotEmpty(cPaths) && ArrayUtils.isNotEmpty(mPaths)) {
            for (String cPath : cPaths) {
                for (String mPath : mPaths) {
                    combinedUrls.add(pathMatcher.combine(cPath, mPath));
                }
            }
        } else if (ArrayUtils.isEmpty(cPaths)) {
            combinedUrls.addAll(Arrays.asList(mPaths));
        } else if (ArrayUtils.isEmpty(mPaths)) {
            combinedUrls.addAll(Arrays.asList(cPaths));
        } else {
            combinedUrls.add("/");
        }
        combinedUrls = ensureAllStartWithSlash(combinedUrls);
        return combinedUrls;
    }

    private List<String> ensureAllStartWithSlash(List<String> urls) {
        List<String> result = Lists.newArrayList();
        for (String url : urls) {
            if (!url.startsWith("/")) {
                url = "/" + url;
            }
            result.add(url);
        }
        return result;
    }

}
