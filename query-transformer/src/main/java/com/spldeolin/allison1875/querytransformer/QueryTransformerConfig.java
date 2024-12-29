package com.spldeolin.allison1875.querytransformer;

import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.common.config.Allison1875Config;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-08-09
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QueryTransformerConfig extends Allison1875Config {

    /**
     * 是否生成Intell IDEA的“Turn formatter on/off with makers in code comments”
     */
    @NotNull
    Boolean enableGenerateFormatterMarker = true;

}