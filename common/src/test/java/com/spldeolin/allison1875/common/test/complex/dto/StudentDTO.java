package com.spldeolin.allison1875.common.test.complex.dto;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StudentDTO {

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 课程
     */
    private List<CourseDTO> course;

}
