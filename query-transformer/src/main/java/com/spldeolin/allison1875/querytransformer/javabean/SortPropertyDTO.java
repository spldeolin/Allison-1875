package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.Objects;
import com.spldeolin.allison1875.querytransformer.enums.OrderSequenceEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-11-23
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SortPropertyDTO {

    String propertyName;

    OrderSequenceEnum orderSequence;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SortPropertyDTO that = (SortPropertyDTO) o;
        return Objects.equals(propertyName, that.propertyName) && orderSequence == that.orderSequence;
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyName, orderSequence);
    }

}