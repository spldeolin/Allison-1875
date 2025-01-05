package com.spldeolin.allison1875.docanalyzer;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeBodyRetval;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeFieldVarsRetval;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeMvcHandlerRetval;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeRequestMappingRetval;
import com.spldeolin.allison1875.docanalyzer.dto.EndpointDTO;
import com.spldeolin.allison1875.docanalyzer.dto.MvcControllerDTO;
import com.spldeolin.allison1875.docanalyzer.dto.MvcHandlerDTO;
import com.spldeolin.allison1875.docanalyzer.enums.FlushToEnum;
import com.spldeolin.allison1875.docanalyzer.service.FieldService;
import com.spldeolin.allison1875.docanalyzer.service.JsgBuilderService;
import com.spldeolin.allison1875.docanalyzer.service.MarkdownService;
import com.spldeolin.allison1875.docanalyzer.service.MvcHandlerAnalyzerService;
import com.spldeolin.allison1875.docanalyzer.service.MvcHandlerDetectorService;
import com.spldeolin.allison1875.docanalyzer.service.RequestBodyService;
import com.spldeolin.allison1875.docanalyzer.service.RequestMappingService;
import com.spldeolin.allison1875.docanalyzer.service.ResponseBodyService;
import com.spldeolin.allison1875.docanalyzer.service.YApiService;
import lombok.extern.slf4j.Slf4j;

/**
 * doc-analyzer的主流程
 *
 * @author Deolin 2020-06-10
 */
@Singleton
@Slf4j
public class DocAnalyzer implements Allison1875MainService {

    @Inject
    private MvcHandlerDetectorService mvcHandlerDetectorService;

    @Inject
    private RequestMappingService requestMappingService;

    @Inject
    private MvcHandlerAnalyzerService mvcHandlerAnalyzerService;

    @Inject
    private YApiService yapiService;

    @Inject
    private MarkdownService markdownService;

    @Inject
    private FieldService fieldService;

    @Inject
    private JsgBuilderService jsgBuilderService;

    @Inject
    private RequestBodyService requestBodyService;

    @Inject
    private ResponseBodyService responseBodyService;

    @Inject
    private DocAnalyzerConfig config;

    @Override
    public void process(AstForest astForest) {
        // 分析所有fieldVars
        Table<String, String, AnalyzeFieldVarsRetval> analyzeFieldVarsRetvals = fieldService.analyzeFieldVars();

        // 基于注释和枚举项分析结果，构建jsg
        JsonSchemaGenerator jsg4req = jsgBuilderService.buildJsg(analyzeFieldVarsRetvals, true);
        JsonSchemaGenerator jsg4resp = jsgBuilderService.buildJsg(analyzeFieldVarsRetvals, false);

        // 收集endpoint
        List<EndpointDTO> endpoints = Lists.newArrayList();

        // 发现mvcHandlers
        List<MvcHandlerDTO> mvcHandlers = mvcHandlerDetectorService.detectMvcHandler();
        if (CollectionUtils.isEmpty(mvcHandlers)) {
            log.warn("no MVC Handlers detected");
            return;
        }
        log.info("{} MVC Handler(s) detected", mvcHandlers.size());

        // 遍历mvcHandlers
        for (MvcHandlerDTO mvcHandler : mvcHandlers) {
            MvcControllerDTO mvcController = mvcHandler.getMvcController();
            EndpointDTO endpoint = new EndpointDTO();

            // 分析并保存handler的分类、代码简称、描述、是否过时、作者、源码位置 等基本信息
            AnalyzeMvcHandlerRetval analyzeMvcHandlerRetval = mvcHandlerAnalyzerService.analyzeMvcHandler(
                    mvcController.getCoid(), mvcHandler);
            setTo(analyzeMvcHandlerRetval, endpoint);

            // 分析
            try {
                // 分析Request Body
                AnalyzeBodyRetval analyzeBodyRetval = requestBodyService.analyzeBody(jsg4req, mvcHandler.getMd());
                endpoint.setRequestBodyDescribe(analyzeBodyRetval.getDescribe());
                endpoint.setRequestBodyJsonSchema(analyzeBodyRetval.getJsonSchema());

                // 分析Response Body
                analyzeBodyRetval = responseBodyService.analyzeBody(jsg4resp, mvcController.getCoid(),
                        mvcHandler.getMd());
                endpoint.setResponseBodyDescribe(analyzeBodyRetval.getDescribe());
                endpoint.setResponseBodyJsonSchema(analyzeBodyRetval.getJsonSchema());

                // 分析controller级与handler级的@RequestMapping
                AnalyzeRequestMappingRetval analyzeRequestMappingRetval = requestMappingService.analyzeRequestMapping(
                        mvcController.getReflection(), mvcHandler.getReflection());

                // 如果handler能通过多种url+Http动词进行请求，HTTP动词只使用其中之一
                endpoint.setHttpMethod(getMoreAcceptableOnes(analyzeRequestMappingRetval.getCombinedVerbs()));
                endpoint.setUrls(analyzeRequestMappingRetval.getCombinedUrls());

                endpoints.add(endpoint);
            } catch (Exception e) {
                log.error("fail to analyze api doc, ignore, endpoint={}", endpoint, e);
            }
        }

        // 输出
        if (config.getFlushTo() == FlushToEnum.YAPI) {
            yapiService.flushToYApi(endpoints);
        }
        if (config.getFlushTo() == FlushToEnum.MARKDOWN) {
            markdownService.flushToMarkdown(endpoints);
        }
        if (config.getFlushTo() == FlushToEnum.SINGLE_MARKDOWN) {
            markdownService.flushToSingleMarkdown(endpoints);
        }

        log.info("endpoints.size={}", endpoints.size());
    }

    private void setTo(AnalyzeMvcHandlerRetval analyzeMvcHandlerRetval, EndpointDTO endpoint) {
        endpoint.setCat(analyzeMvcHandlerRetval.getCat());
        endpoint.setDescriptionLines(analyzeMvcHandlerRetval.getDescriptionLines());
        endpoint.setIsDeprecated(analyzeMvcHandlerRetval.getIsDeprecated());
        endpoint.setAuthor(analyzeMvcHandlerRetval.getAuthor());
        endpoint.setSourceCode(analyzeMvcHandlerRetval.getSourceCode());
    }

    private String getMoreAcceptableOnes(List<RequestMethod> combinedVerbs) {
        if (combinedVerbs.stream().anyMatch(v -> v.equals(RequestMethod.POST))) {
            return "post";
        }
        if (combinedVerbs.stream().anyMatch(v -> v.equals(RequestMethod.GET))) {
            return "get";
        }
        return StringUtils.lowerCase(combinedVerbs.get(0).toString());
    }

}
