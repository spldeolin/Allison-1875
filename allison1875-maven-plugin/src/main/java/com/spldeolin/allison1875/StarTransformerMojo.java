package com.spldeolin.allison1875;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.startransformer.StarTransformerConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-06-14
 */
@Mojo(name = "star-transformer", requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.COMPILE)
@Slf4j
public class StarTransformerMojo extends Allison1875Mojo {

    @Parameter(alias = "starTransformer")
    private final StarTransformerMojoConfig starTransformerConfig = new StarTransformerMojoConfig();

    @Override
    public Allison1875Module newAllison1875Module(CommonConfig commonConfig, ClassLoader classLoader) throws Exception {
        log.info("starTransformerConfig={}", JsonUtils.toJsonPrettily(starTransformerConfig));
        log.info("new module instance for {}", starTransformerConfig.getModule());
        return (Allison1875Module) classLoader.loadClass(starTransformerConfig.getModule())
                .getConstructor(CommonConfig.class, StarTransformerConfig.class)
                .newInstance(commonConfig, starTransformerConfig);
    }

}
