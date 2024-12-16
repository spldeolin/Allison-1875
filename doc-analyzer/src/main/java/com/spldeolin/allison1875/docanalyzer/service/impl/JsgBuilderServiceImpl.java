package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;
import org.springframework.core.annotation.AnnotatedElementUtils;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.docanalyzer.enums.ValidatorTypeEnum;
import com.spldeolin.allison1875.docanalyzer.javabean.AnalyzeFieldVarsRetval;
import com.spldeolin.allison1875.docanalyzer.javabean.AnalyzeValidRetval;
import com.spldeolin.allison1875.docanalyzer.javabean.JsonPropertyDescriptionValueDTO;
import com.spldeolin.allison1875.docanalyzer.service.JsgBuilderService;

/**
 * 内聚了 解析得到所有枚举、属性信息 和 生成自定义JsonSchemaGenerator对象的功能
 *
 * @author Deolin 2020-06-10
 */
@Singleton
public class JsgBuilderServiceImpl implements JsgBuilderService {

    @Override
    public JsonSchemaGenerator buildJsg(Table<String, String, AnalyzeFieldVarsRetval> afvRetvals,
            boolean forReqOrResp) {
        // 缺省配置
        ObjectMapper customOm = JsonUtils.createObjectMapper();

        // 只有类属性可见，类的getter、setter、构造方法里的字段不会被当作JSON的字段
        customOm.setVisibility(customOm.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY).withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        customOm.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            private static final long serialVersionUID = -3267511125040673149L;

            @Override
            public boolean hasIgnoreMarker(AnnotatedMember m) {
                // 当开发者指定了Access.READ_ONLY时，如果是reqDTO，则算作ignore
                Access propertyAccess = super.findPropertyAccess(m);
                if (forReqOrResp && propertyAccess == Access.READ_ONLY) {
                    return true;
                }

                // 当开发者指定了Access.WRITE_ONLY时候，如果是respDTO，则算作ignore
                if (!forReqOrResp && propertyAccess == Access.WRITE_ONLY) {
                    return true;
                }

                // 拓展分析
                String className = m.getDeclaringClass().getName().replace('$', '.');
                String fieldNameMight = m.getName();
                AnalyzeFieldVarsRetval afvRetval = afvRetvals.get(className, fieldNameMight);
                if (isIgnored(m, afvRetval, forReqOrResp)) {
                    return true;
                }

                return super.hasIgnoreMarker(m);
            }

            @Override
            public String findPropertyDescription(Annotated annotated) {
                Field field = findFieldEvenIfAnnotatedMethod(annotated.getAnnotated());
                if (field == null) {
                    return null;
                }

                String className = field.getDeclaringClass().getName().replace('$', '.');
                String fieldNameMight = field.getName();

                JsonPropertyDescriptionValueDTO jpdv = new JsonPropertyDescriptionValueDTO();

                // jpdv 注释
                AnalyzeFieldVarsRetval afvRetval = afvRetvals.get(className, fieldNameMight);
                if (afvRetval != null) {
                    jpdv.getCommentLines().addAll(afvRetval.getCommentLines());
                }

                // jpdv 枚举项
                if (afvRetval != null) {
                    jpdv.getAnalyzeEnumConstantsRetvals().addAll(afvRetval.getAnalyzeEnumConstantsRetvals());
                }

                // jpdv 校验项
                if (forReqOrResp) {
                    jpdv.getValids().addAll(analyzeValid(annotated.getAnnotated()));
                }
                /*
                    解析自Field类型的唯一一个泛型上的校验注解（如果有唯一泛型的话）
                    e.g: private List<@NotBlank @Length(max = 10) String> userNames;
                 */
                boolean isLikeCollection = Collection.class.isAssignableFrom(annotated.getType().getRawClass());
                if (forReqOrResp && isLikeCollection) {
                    AnnotatedType at = field.getAnnotatedType();
                    if (at instanceof AnnotatedParameterizedType) {
                        AnnotatedType[] fieldTypeArguments =
                                ((AnnotatedParameterizedType) at).getAnnotatedActualTypeArguments();
                        if (fieldTypeArguments.length == 1) {
                            AnnotatedType collectionParamType = fieldTypeArguments[0];
                            List<AnalyzeValidRetval> collectonParamTypeValids = analyzeValid(collectionParamType);
                            collectonParamTypeValids.forEach(
                                    one -> one.setValidatorType("列表内元素" + one.getValidatorType()));
                            jpdv.getValids().addAll(collectonParamTypeValids);
                        }
                    }
                }

                // jpdv 格式
                JsonFormat jsonFormat = AnnotatedElementUtils.findMergedAnnotation(field, JsonFormat.class);
                jpdv.setFormatPattern(Optional.ofNullable(jsonFormat).map(JsonFormat::pattern).orElse(""));

                // jpdv 更多分析后生成的文档
                if (afvRetval != null) {
                    jpdv.getMoreDocLines().addAll(afvRetval.getMoreDocLines());
                }

                return jpdv.serialize();
            }

            private Field findFieldEvenIfAnnotatedMethod(AnnotatedElement annotated) {
                if (annotated instanceof Field) {
                    return (Field) annotated;
                }
                if (annotated instanceof Method) {
                    Method method = (Method) annotated;
                    String fieldName = MoreStringUtils.toLowerCamel(method.getName().substring(3));
                    try {
                        return method.getDeclaringClass().getDeclaredField(fieldName);
                    } catch (NoSuchFieldException e) {
                        return null;
                    }
                }
                return null;
            }

            @Override
            protected <A extends Annotation> A _findAnnotation(Annotated annotated, Class<A> annoClass) {
                if (annoClass == JsonSerialize.class) {
                    return null;
                }
                return super._findAnnotation(annotated, annoClass);
            }

        });
        return new JsonSchemaGenerator(customOm);
    }

    protected boolean isIgnored(AnnotatedMember m, @Nullable AnalyzeFieldVarsRetval afvRetval, boolean forReqOrResp) {
        return false;
    }

    protected List<AnalyzeValidRetval> analyzeValid(AnnotatedElement annotatedElement) {
        List<AnalyzeValidRetval> valids = Lists.newArrayList();
        NotNull notNull = find(annotatedElement, NotNull.class);
        if (notNull != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.NOT_NULL.getValue()));
        }

        if (find(annotatedElement, javax.validation.constraints.NotEmpty.class) != null
                || find(annotatedElement, org.hibernate.validator.constraints.NotEmpty.class) != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.NOT_EMPTY.getValue()));
        }

        if (find(annotatedElement, javax.validation.constraints.NotBlank.class) != null
                || find(annotatedElement, org.hibernate.validator.constraints.NotBlank.class) != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.NOT_BLANK.getValue()));
        }

        Size size = find(annotatedElement, Size.class);
        if (size != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.MIN_SIZE.getValue())
                    .setNote(String.valueOf(size.min())));
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.MAX_SIZE.getValue())
                    .setNote(String.valueOf(size.max())));
        }

        Length length = find(annotatedElement, Length.class);
        if (length != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.MIN_SIZE.getValue())
                    .setNote(String.valueOf(length.min())));
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.MAX_SIZE.getValue())
                    .setNote(String.valueOf(length.max())));
        }

        Min min = find(annotatedElement, Min.class);
        if (min != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.MIN_NUMBER.getValue())
                    .setNote(String.valueOf(min.value())));
        }

        DecimalMin decimalMin = find(annotatedElement, DecimalMin.class);
        if (decimalMin != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.MIN_NUMBER.getValue())
                    .setNote(decimalMin.value()));
        }

        Max max = find(annotatedElement, Max.class);
        if (max != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.MAX_NUMBER.getValue())
                    .setNote(String.valueOf(max.value())));
        }

        DecimalMax decimalMax = find(annotatedElement, DecimalMax.class);
        if (decimalMax != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.MAX_NUMBER.getValue())
                    .setNote(decimalMax.value()));
        }

        Future future = find(annotatedElement, Future.class);
        if (future != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.FUTURE.getValue()));
        }

        FutureOrPresent futureOrPresent = find(annotatedElement, FutureOrPresent.class);
        if (futureOrPresent != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.FUTURE_OR_PRESENT.getValue()));
        }

        Past past = find(annotatedElement, Past.class);
        if (past != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.PAST.getValue()));
        }

        PastOrPresent pastOrPresent = find(annotatedElement, PastOrPresent.class);
        if (pastOrPresent != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.PAST_OR_PRESENT.getValue()));
        }

        Digits digits = find(annotatedElement, Digits.class);
        if (digits != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.MAX_INTEGRAL_DIGITS.getValue())
                    .setNote(String.valueOf(digits.integer())));
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.MAX_FRACTIONAL_DIGITS.getValue())
                    .setNote(String.valueOf(digits.fraction())));
        }

        Positive positive = find(annotatedElement, Positive.class);
        if (positive != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.POSITIVE.getValue()));
        }

        Negative negative = find(annotatedElement, Negative.class);
        if (negative != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.NEGATIVE.getValue()));
        }

        Pattern pattern = find(annotatedElement, Pattern.class);
        if (pattern != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.REGEX.getValue())
                    .setNote(pattern.regexp()));
        }

        valids.forEach(valid -> {
            if (valid.getNote() == null) {
                valid.setNote("");
            }
        });
        return valids;
    }

    private <A extends Annotation> A find(AnnotatedElement field, Class<A> annotationType) {
        return AnnotatedElementUtils.findMergedAnnotation(field, annotationType);
    }

}
