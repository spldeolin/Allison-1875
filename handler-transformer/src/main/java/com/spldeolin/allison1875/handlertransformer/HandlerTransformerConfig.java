package com.spldeolin.allison1875.handlertransformer;

import javax.validation.constraints.NotEmpty;
import com.spldeolin.allison1875.common.config.Allison1875Config;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Allison1875[handler-transformer]的配置
 *
 * @author Deolin 2020-08-25
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HandlerTransformerConfig extends Allison1875Config {

    /**
     * 分页对象的全限定名
     */
    @NotEmpty String pageTypeQualifier;

}