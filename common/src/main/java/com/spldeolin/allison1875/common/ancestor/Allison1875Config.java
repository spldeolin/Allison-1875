package com.spldeolin.allison1875.common.ancestor;

import java.util.List;
import com.spldeolin.allison1875.common.dto.InvalidDTO;
import com.spldeolin.allison1875.common.util.ValidUtils;

/**
 * @author Deolin 2024-01-14
 */
public abstract class Allison1875Config {

    public List<InvalidDTO> invalidSelf() {
        return ValidUtils.valid(this);
    }

}