package com.spldeolin.allison1875.docanalyzer;

import java.util.List;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.dto.InvalidDTO;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-12-06
 */
@Slf4j
public class DocAnalyzerModule extends Allison1875Module {

    private final CommonConfig commonConfig;

    private final DocAnalyzerConfig docAnalyzerConfig;

    public DocAnalyzerModule(CommonConfig commonConfig, DocAnalyzerConfig docAnalyzerConfig) {
        this.commonConfig = commonConfig;
        this.docAnalyzerConfig = docAnalyzerConfig;
    }

    @Override
    public final Class<? extends Allison1875MainService> declareMainService() {
        return DocAnalyzer.class;
    }

    @Override
    public List<InvalidDTO> validConfigs() {
        List<InvalidDTO> invalids = commonConfig.invalidSelf();
        invalids.addAll(docAnalyzerConfig.invalidSelf());
        return invalids;
    }

    @Override
    protected void configure() {
        bind(CommonConfig.class).toInstance(commonConfig);
        bind(DocAnalyzerConfig.class).toInstance(docAnalyzerConfig);
    }

}