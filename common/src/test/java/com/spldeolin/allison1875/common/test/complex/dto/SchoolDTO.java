package com.spldeolin.allison1875.common.test.complex.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.tuple.Triple;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 学校
 */
@Data
@Accessors(chain = true)
public class SchoolDTO {

    /**
     * 学校ID
     */
    @NotNull
    private Long companyId;

    /**
     * 相关学校的学校ID
     */
    @NotEmpty
    private List<Long> companyIds;

    /**
     * 学校名称
     */
    @NotEmpty
    @Size(max = 1000)
    private String companyName;

    /**
     * 相关学校的学校名称
     */
    private List<@Size(min = 12) String> companyNames;

    /**
     * 成立时间
     */
    @Deprecated
    private Date foundingDate;

    /**
     * 成立时间（新版）
     */
    @Past
    private LocalDateTime foundingDateV2;

    /**
     * 今年学生数
     */
    private Integer studentCount;

    /**
     * 历年学生数
     */
    private List<Integer> studentCounts;

    /**
     * 最近一次事件的日期
     */
    private LocalDate lastEventDate;

    /**
     * 最近一次事件的时间
     */
    private LocalTime lastEventTime;

    /**
     * 每次事件的日期
     */
    private List<LocalDate> lastEventDates;

    /**
     * 每次事件的时间
     */
    private List<LocalTime> lastEventTimes;

    /**
     * 今年总收入
     */
    @DecimalMin("1.1")
    private BigDecimal totalRevenue;

    /**
     * 每年总收入
     */
    private List<BigDecimal> totalRevenues;

    /**
     * 班级
     */
    private List<ClassroomDTO> classrooms;

    private Triple<A1<B1>, A2<B2>, A3<B3>> xx;

}
