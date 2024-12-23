package com.spldeolin.allison1875.handlertransformer.dto;

import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.ast.FileFlush;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-03-05
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddMethodToServiceRetval {

    String methodName;

    final List<FileFlush> flushes = Lists.newArrayList();

}