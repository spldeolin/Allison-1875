package com.spldeolin.allison1875.common.test.complex.dto;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CourseDTO {

    /**
     * 教师
     */
    private TeacherDTO teacher;

    /**
     * 前置课程
     */
    private CourseDTO course;

    /**
     * 相关课程
     */
    private List<CourseDTO> courses;

    /**
     * 开设日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime time;

}
