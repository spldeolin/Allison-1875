package com.spldeolin.allison1875.docanalyzer.exception;

import com.spldeolin.allison1875.common.exception.Allison1875Exception;

/**
 * 字符串中不存在符号<code>.</code>
 *
 * @author Deolin 2020-06-03
 */
public class DotAbsentInStringException extends Allison1875Exception {

    private static final long serialVersionUID = 1497471147051352347L;

    public DotAbsentInStringException() {
        super((String) null);
    }

}
