package com.spldeolin.allison1875.querytransformer.util;

import com.github.javaparser.JavaToken;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.Node;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;

/**
 * @author Deolin 2021-06-14
 */
public class TokenRangeUtils {

    private TokenRangeUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static String getRawCode(Node node) {
        if (node.getComment().isPresent()) {
            JavaToken begin = node.getComment().get().getTokenRange()
                    .orElseThrow(() -> new Allison1875Exception("Node [" + node + "] has no Token Range")).getBegin();
            JavaToken end = node.getTokenRange()
                    .orElseThrow(() -> new Allison1875Exception("Node [" + node + "] has no Token Range")).getEnd();
            return new TokenRange(begin, end).toString();
        } else {
            return node.getTokenRange()
                    .orElseThrow(() -> new Allison1875Exception("Node [" + node + "] has no Token Range")).toString();
        }
    }

}