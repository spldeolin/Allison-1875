package com.spldeolin.allison1875.handlertransformer;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.javabean.AddInjectFieldRetval;
import com.spldeolin.allison1875.common.javabean.GenerateMvcHandlerArgs;
import com.spldeolin.allison1875.common.javabean.GenerateMvcHandlerRetval;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.service.MemberAdderService;
import com.spldeolin.allison1875.common.service.MvcHandlerGeneratorService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.handlertransformer.javabean.AddMethodToServiceArgs;
import com.spldeolin.allison1875.handlertransformer.javabean.AddMethodToServiceRetval;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateDtoJavabeansRetval;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateServiceAndImplArgs;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateServiceAndImplRetval;
import com.spldeolin.allison1875.handlertransformer.javabean.InitDecAnalysisDto;
import com.spldeolin.allison1875.handlertransformer.service.DtoService;
import com.spldeolin.allison1875.handlertransformer.service.InitDecAnalyzerService;
import com.spldeolin.allison1875.handlertransformer.service.InitDecDetectorService;
import com.spldeolin.allison1875.handlertransformer.service.MvcControllerService;
import com.spldeolin.allison1875.handlertransformer.service.ReqRespService;
import com.spldeolin.allison1875.handlertransformer.service.ServiceLayerService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-12-22
 */
@Singleton
@Slf4j
public class HandlerTransformer implements Allison1875MainService {

    @Inject
    private MvcControllerService mvcControllerService;

    @Inject
    private InitDecDetectorService initDecDetectorService;

    @Inject
    private ReqRespService reqRespService;

    @Inject
    private DtoService dtoService;

    @Inject
    private InitDecAnalyzerService initDecAnalyzerService;

    @Inject
    private ImportExprService importExprService;

    @Inject
    private ServiceLayerService serviceLayerService;

    @Inject
    private MemberAdderService memberAdderService;

    @Inject
    private MvcHandlerGeneratorService mvcHandlerGeneratorService;

    @Inject
    private CommonConfig commonConfig;

    @Override
    public void process(AstForest astForest) {
        List<FileFlush> flushes = Lists.newArrayList();

        for (CompilationUnit cu : astForest) {
            boolean anyTransformed = false;

            for (ClassOrInterfaceDeclaration mvcController : mvcControllerService.detectMvcControllers(cu)) {
                for (InitializerDeclaration initDec : initDecDetectorService.detectInitDecs(mvcController)) {
                    BlockStmt initBody = initDec.getBody().clone();
                    InitDecAnalysisDto initDecAnalysis = initDecAnalyzerService.analyzeInitDec(cu, mvcController,
                            initDec);
                    if (initDecAnalysis == null) {
                        continue;
                    }
                    log.info("detect able to transform initDec '{}' from mvcController '{}'.", initDecAnalysis,
                            mvcController.getNameAsString());

                    // 校验init下的Req和Resp类
                    reqRespService.validInitBody(initBody, initDecAnalysis);

                    // 自底向上收集（广度优先遍历收集 + 反转）
                    List<ClassOrInterfaceDeclaration> dtoCoids = dtoService.detectDtosBottomTop(initBody);

                    // 生成Javabean
                    GenerateDtoJavabeansRetval generateDtoJavabeansRetval = reqRespService.generateDtoJavabeans(
                            initDecAnalysis, dtoCoids);
                    flushes.addAll(generateDtoJavabeansRetval.getFlushes());

                    // 生成Service方法
                    MethodDeclaration serviceMethod = serviceLayerService.generateServiceMethod(initDecAnalysis,
                            generateDtoJavabeansRetval.getReqBodyDtoType(), generateDtoJavabeansRetval.getReqParams(),
                            generateDtoJavabeansRetval.getRespBodyDtoType());

                    // 生成Service / ServiceImpl
                    GenerateServiceAndImplArgs gsaiArgs = new GenerateServiceAndImplArgs();
                    gsaiArgs.setControllerCu(cu);
                    gsaiArgs.setInitDecAnalysisDto(initDecAnalysis);
                    GenerateServiceAndImplRetval generateServiceAndImplRetval =
                            serviceLayerService.generateServiceAndImpl(
                            gsaiArgs);

                    // service方法加入到Service层
                    AddMethodToServiceArgs args = new AddMethodToServiceArgs();
                    args.setControllerCu(cu);
                    args.setInitDecAnalysisDto(initDecAnalysis);
                    args.setServiceMethod(serviceMethod);
                    args.setGenerateServiceAndImplRetval(generateServiceAndImplRetval);
                    AddMethodToServiceRetval addMethodToServiceRetval = serviceLayerService.addMethodToService(args);
                    if (addMethodToServiceRetval == null) {
                        continue;
                    }
                    flushes.addAll(addMethodToServiceRetval.getFlushes());

                    // 确保mvcController有autowired 新生成的service（由于Service/Impl是新生成的，生成时已AntiDupl了，所以这个方法不会发挥作用）
                    AddInjectFieldRetval addInjectFieldRetval = memberAdderService.addInjectField(
                            generateServiceAndImplRetval.getServiceQualifier(),
                            generateServiceAndImplRetval.getServiceVarName(), mvcController);

                    // 创建mvcHandler
                    GenerateMvcHandlerArgs gmhArgs = new GenerateMvcHandlerArgs();
                    gmhArgs.setMvcHandlerUrl(initDecAnalysis.getMvcHandlerUrl());
                    String description = initDecAnalysis.getMvcHandlerDescription();
                    if (commonConfig.getEnableLotNoAnnounce()) {
                        description += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION
                                + initDecAnalysis.getLotNo();
                    }
                    gmhArgs.setDescription(description);
                    gmhArgs.setReqBodyDtoType(generateDtoJavabeansRetval.getReqBodyDtoType());
                    gmhArgs.setRespBodyDtoType(generateDtoJavabeansRetval.getRespBodyDtoType());
                    gmhArgs.setInjectedServiceVarName(addInjectFieldRetval.getFieldVarName());
                    gmhArgs.setServiceMethodName(addMethodToServiceRetval.getMethodName());
                    gmhArgs.setMvcController(mvcController);
                    gmhArgs.setIsHttpGet(generateDtoJavabeansRetval.getIsHttpGet());
                    gmhArgs.setReqParams(generateDtoJavabeansRetval.getReqParams());
                    GenerateMvcHandlerRetval generateMvcHandlerRetval = mvcHandlerGeneratorService.generateMvcHandler(
                            gmhArgs);

                    // mvcHandler并替换掉initDec
                    mvcControllerService.replaceMvcHandlerToInitDec(initDecAnalysis, generateServiceAndImplRetval,
                            generateMvcHandlerRetval);
                    log.info("replace Initializer [{}] to Handler [{}] in Controller [{}].", initDecAnalysis,
                            generateMvcHandlerRetval.getMvcHandler().getName(), mvcController.getName());

                    anyTransformed = true;
                }
            }

            if (anyTransformed) {
                importExprService.extractQualifiedTypeToImport(cu);
                flushes.add(FileFlush.build(cu));
            }
        }

        // write all to file
        if (CollectionUtils.isNotEmpty(flushes)) {
            flushes.forEach(FileFlush::flush);
            log.info(BaseConstant.REMEMBER_REFORMAT_CODE_ANNOUNCE);
        } else {
            log.warn("no valiad Initializer detected");
        }
    }

}