package com.spldeolin.allison1875.startransformer;

import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.common.ancestor.Allison1875Config;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2023-05-05
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StarTransformerConfig extends Allison1875Config {

    /**
     * Whole DTO的后缀
     */
    @NotNull
    String wholeDTONamePostfix = "WholeDTO";

}