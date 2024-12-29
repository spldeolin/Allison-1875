package com.spldeolin.allison1875;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-06-10
 */
@Mojo(name = "persistence-generator", requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.COMPILE)
@Slf4j
public class PersistenceGeneratorMojo extends Allison1875Mojo {

    @Parameter(alias = "persistenceGenerator")
    private final PersistenceGeneratorMojoConfig persistenceGeneratorConfig = new PersistenceGeneratorMojoConfig();

    @Override
    public Allison1875Module newAllison1875Module(CommonConfig commonConfig, ClassLoader classLoader) throws Exception {
        log.info("persistenceGeneratorConfig={}", JsonUtils.toJsonPrettily(persistenceGeneratorConfig));
        log.info("new module instance for {}", persistenceGeneratorConfig.getModule());
        return (Allison1875Module) classLoader.loadClass(persistenceGeneratorConfig.getModule())
                .getConstructor(CommonConfig.class, PersistenceGeneratorConfig.class)
                .newInstance(commonConfig, persistenceGeneratorConfig);
    }

}
