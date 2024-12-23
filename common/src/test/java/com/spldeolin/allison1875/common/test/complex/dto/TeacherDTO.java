package com.spldeolin.allison1875.common.test.complex.dto;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TeacherDTO {

    /**
     * 第一个任职的学校
     */
    SchoolDTO school;

    /**
     * 曾任职的学校
     */
    List<SchoolDTO> schools;

}
