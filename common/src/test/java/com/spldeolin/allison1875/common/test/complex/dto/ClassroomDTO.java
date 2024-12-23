package com.spldeolin.allison1875.common.test.complex.dto;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ClassroomDTO {

    /**
     * 班长
     */
    private StudentDTO classPresident;

    /**
     * 学生
     */
    private List<StudentDTO> students;

}
