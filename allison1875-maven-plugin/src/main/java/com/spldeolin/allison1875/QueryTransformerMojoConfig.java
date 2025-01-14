package com.spldeolin.allison1875;

import com.spldeolin.allison1875.querytransformer.QueryTransformerConfig;
import com.spldeolin.allison1875.querytransformer.QueryTransformerModule;
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
public class QueryTransformerMojoConfig extends QueryTransformerConfig {

    String module = QueryTransformerModule.class.getName();

}
