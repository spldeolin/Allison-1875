package com.spldeolin.allison1875.docanalyzer.processor;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.docanalyzer.javabean.RequestMappingFullDto;

/**
 * 内聚了 对请求URL和请求动词解析的功能
 *
 * @author Deolin 2020-06-10
 */
@Singleton
public class RequestMappingProc {

    @Inject
    AntPathMatcher pathMatcher;

    public RequestMappingFullDto analyze(Class<?> controllerClass, Method reflectionMethod, String globalUrlPrefix, Integer globalUrlPrefixIndex) {
        RequestMapping controllerRequestMapping = findRequestMappingAnnoOrElseNull(controllerClass);
        String[] controllerPaths = findValueFromAnno(controllerRequestMapping);
        RequestMethod[] controllerVerbs = findVerbFromAnno(controllerRequestMapping);

        RequestMapping methodRequestMapping = findRequestMappingAnnoOrElseNull(reflectionMethod);
        String[] methodPaths = methodRequestMapping.value();
        RequestMethod[] methodVerbs = methodRequestMapping.method();

        List<String> combinedUrls = combineUrl(controllerPaths, methodPaths);
        // 添加全局前缀
        if (StringUtils.isNotBlank(globalUrlPrefix)) {
            ListIterator<String> itr = combinedUrls.listIterator();
            while (itr.hasNext()) {
                if(globalUrlPrefixIndex == null || globalUrlPrefixIndex == 0){
                    itr.set(globalUrlPrefix + itr.next());
                }else {
                    String[] urls = itr.next().split("/");
                    StringBuilder result = new StringBuilder();
                    for (int i = 0; i < urls.length; i ++){
                        if(globalUrlPrefixIndex == i){
                            result.append(globalUrlPrefix).append("/").append(urls[i]);
                        }else{
                            result.append("/").append(urls[i]);
                        }
                    }
                    itr.set(result.toString());
                }
            }
        }
        Collection<RequestMethod> combinedVerbs = combineVerb(controllerVerbs, methodVerbs);
        return new RequestMappingFullDto(combinedUrls, combinedVerbs);
    }

    private RequestMapping findRequestMappingAnnoOrElseNull(AnnotatedElement annotated) {
        return AnnotatedElementUtils.findMergedAnnotation(annotated, RequestMapping.class);
    }

    private String[] findValueFromAnno(RequestMapping controllerRequestMapping) {
        return controllerRequestMapping == null ? new String[0] : controllerRequestMapping.value();
    }

    private RequestMethod[] findVerbFromAnno(RequestMapping controllerRequestMapping) {
        return controllerRequestMapping == null ? new RequestMethod[0] : controllerRequestMapping.method();
    }

    private Collection<RequestMethod> combineVerb(RequestMethod[] cVerbs, RequestMethod[] mVerbs) {
        Collection<RequestMethod> combinedVerbs = Lists.newArrayList();
        if (ArrayUtils.isNotEmpty(cVerbs)) {
            combinedVerbs.addAll(Arrays.asList(cVerbs));
        }
        if (ArrayUtils.isNotEmpty(mVerbs)) {
            combinedVerbs.addAll(Arrays.asList(mVerbs));
        }
        if (combinedVerbs.size() == 0) {
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

    private List<String> ensureAllStartWithSlash(Collection<String> urls) {
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
