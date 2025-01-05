package com.spldeolin.allison1875.docanalyzer.enums;

/**
 * @author Deolin 2023-12-10
 */
public enum FlushToEnum {

    /**
     * 本地markdown文件
     */
    MARKDOWN,

    /**
     * 通过OpenAPI同步到YAPI平台
     */
    YAPI,

    /**
     * 每个endpoint输出到单独的本地markdown文件
     */
    SINGLE_MARKDOWN,

}
