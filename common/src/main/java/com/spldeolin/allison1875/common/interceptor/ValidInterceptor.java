package com.spldeolin.allison1875.common.interceptor;

import java.lang.reflect.Method;
import java.util.List;
import org.aopalliance.intercept.MethodInvocation;
import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;
import com.spldeolin.allison1875.common.ancestor.Allison1875Interceptor;
import com.spldeolin.allison1875.common.dto.InvalidDTO;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.ValidUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-02-14
 */
@Slf4j
public class ValidInterceptor extends Allison1875Interceptor {

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Method method = methodInvocation.getMethod();

        for (int i = 0; i < methodInvocation.getArguments().length; i++) {
            Object args = methodInvocation.getArguments()[i];
            if (args == null) {
                continue;
            }
            List<InvalidDTO> valid = ValidUtils.valid(args);
            if (CollectionUtils.isNotEmpty(valid)) {
                throw new Allison1875Exception(
                        String.format("invalid arguments, method=%s, index=%s, invalids=%s, args=%s", method.getName(),
                                i, valid, args));
            }
        }

        return methodInvocation.proceed();
    }

}