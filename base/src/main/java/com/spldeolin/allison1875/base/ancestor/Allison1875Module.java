package com.spldeolin.allison1875.base.ancestor;

import java.util.Set;
import javax.validation.ConstraintViolation;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.spldeolin.allison1875.base.util.ValidateUtils;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-12-10
 */
@Log4j2
public abstract class Allison1875Module extends AbstractModule {

    public <T> T ensureValid(T config) {
        Set<ConstraintViolation<Object>> violations = ValidateUtils.validate(config);
        if (violations.size() > 0) {
            log.error("配置项校验未通过，请检查后重新运行");
            for (ConstraintViolation<Object> violation : violations) {
                log.error(violation.getRootBeanClass().getSimpleName() + "." + violation.getPropertyPath() + " "
                        + violation.getMessage());
            }
            System.exit(-9);
        }
        return config;
    }

    public abstract Allison1875MainProcessor getMainProcessor(Injector injector);

}