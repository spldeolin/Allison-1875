package com.spldeolin.allison1875.querytransformer.dto;

import com.github.javaparser.ast.type.Type;
import com.spldeolin.allison1875.common.ast.FileFlush;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-06-01
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenerateReturnTypeRetval {

    Type resultType;

    /**
     * 对于查询方法
     * 返回单条数据时，这个值是返回类型本身的全限定名；
     * 返回多条数据时，这个值是返回类型List的元素类型的全限定名；
     * 返回多条数据并且指定Each或Multimap时，这个值是返回类型Map或Multimap的V元素类型的全限定名；
     * 终结方法是count时，这个值是null
     *
     * 对于更新或删除方法
     * 这个值是null
     */
    String elementTypeQualifier;

    FileFlush flush;

}