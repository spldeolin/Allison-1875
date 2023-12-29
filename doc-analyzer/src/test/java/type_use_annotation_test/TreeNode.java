package type_use_annotation_test;

import java.util.Collection;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaGenerateUtils;
import com.spldeolin.allison1875.docanalyzer.util.exception.JsonSchemaException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-25
 */
@Log4j2
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TreeNode {

    @NotNull Long id;

    @NotBlank String title;

    Collection<@NotBlank TreeNode>[][] children;

    public static void main(String[] args) throws JsonSchemaException, JsonProcessingException {
        ObjectMapper om = JsonUtils.createObjectMapper();
        om.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            private static final long serialVersionUID = 4963154137655948984L;

            @Override
            public Boolean hasRequiredMarker(AnnotatedMember m) {
                boolean isNotNull = super._findAnnotation(m, NotNull.class) != null;
                boolean isNotEmpty = super._findAnnotation(m, NotEmpty.class) != null;
                boolean isNotBlank = super._findAnnotation(m, NotBlank.class) != null;
                return isNotNull || isNotEmpty || isNotBlank;
            }
        });

        JsonSchema jsonSchema = JsonSchemaGenerateUtils.generateSchema("type_use_annotation_test.TreeNode",
                new JsonSchemaGenerator(om));

        JsonNode jsonNode = om.readTree(JsonUtils.toJson(jsonSchema));
        log.info(JsonUtils.toJson(jsonSchema));
    }

}