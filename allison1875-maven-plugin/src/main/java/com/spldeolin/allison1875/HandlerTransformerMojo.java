package com.spldeolin.allison1875;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-06-14
 */
@Mojo(name = "handler-transformer", requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.COMPILE)
@Slf4j
public class HandlerTransformerMojo extends Allison1875Mojo {

    @Parameter(alias = "handlerTransformer")
    private final HandlerTransformerMojoConfig handlerTransformerConfig = new HandlerTransformerMojoConfig();

    @Override
    public Allison1875Module newAllison1875Module(CommonConfig commonConfig, ClassLoader classLoader) throws Exception {
        log.info("handlerTransformerConfig={}", JsonUtils.toJsonPrettily(handlerTransformerConfig));
        log.info("new module instance for {}", handlerTransformerConfig.getModule());
        return (Allison1875Module) classLoader.loadClass(handlerTransformerConfig.getModule())
                .getConstructor(CommonConfig.class, HandlerTransformerConfig.class)
                .newInstance(commonConfig, handlerTransformerConfig);
    }

}
