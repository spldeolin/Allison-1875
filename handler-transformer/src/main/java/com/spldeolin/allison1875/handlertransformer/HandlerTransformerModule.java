package com.spldeolin.allison1875.handlertransformer;

import java.util.List;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.dto.InvalidDTO;
import lombok.ToString;

/**
 * @author Deolin 2020-12-07
 */
@ToString
public class HandlerTransformerModule extends Allison1875Module {

    private final CommonConfig commonConfig;

    private final HandlerTransformerConfig handlerTransformerConfig;

    public HandlerTransformerModule(CommonConfig commonConfig, HandlerTransformerConfig handlerTransformerConfig) {
        this.commonConfig = commonConfig;
        this.handlerTransformerConfig = handlerTransformerConfig;
    }

    @Override
    public final Class<? extends Allison1875MainService> declareMainService() {
        return HandlerTransformer.class;
    }

    @Override
    public List<InvalidDTO> validConfigs() {
        List<InvalidDTO> invalids = commonConfig.invalidSelf();
        invalids.addAll(handlerTransformerConfig.invalidSelf());
        return invalids;
    }

    @Override
    protected void configure() {
        bind(CommonConfig.class).toInstance(commonConfig);
        bind(HandlerTransformerConfig.class).toInstance(handlerTransformerConfig);
    }

}