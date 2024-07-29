package com.spldeolin.allison1875;

import org.apache.maven.plugins.annotations.Parameter;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-07-29
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HandlerTransformerMojoConfig extends HandlerTransformerConfig {

    @Parameter(defaultValue = "com.spldeolin.allison1875.handlertransformer.HandlerTransformerModule")
    private String module;

}
