package com.spldeolin.allison1875.persistencegenerator;

import java.util.List;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.dto.InvalidDTO;
import lombok.ToString;

/**
 * @author Deolin 2020-12-08
 */
@ToString
public class PersistenceGeneratorModule extends Allison1875Module {

    private final CommonConfig commonConfig;

    private final PersistenceGeneratorConfig persistenceGeneratorConfig;

    public PersistenceGeneratorModule(CommonConfig commonConfig,
            PersistenceGeneratorConfig persistenceGeneratorConfig) {
        this.commonConfig = commonConfig;
        this.persistenceGeneratorConfig = persistenceGeneratorConfig;
    }

    @Override
    public final Class<? extends Allison1875MainService> declareMainService() {
        return PersistenceGenerator.class;
    }

    @Override
    public List<InvalidDTO> validConfigs() {
        List<InvalidDTO> invalids = commonConfig.invalidSelf();
        invalids.addAll(persistenceGeneratorConfig.invalidSelf());
        return invalids;
    }

    @Override
    protected void configure() {
        bind(CommonConfig.class).toInstance(commonConfig);
        bind(PersistenceGeneratorConfig.class).toInstance(persistenceGeneratorConfig);
    }

}