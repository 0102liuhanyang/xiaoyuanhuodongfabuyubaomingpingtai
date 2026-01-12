package com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventRequest {
    private Long id;
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    private String category;
    private String tags;
    @NotBlank
    private String location;
    @NotNull
    private LocalDateTime startTime;
    @NotNull
    @Future
    private LocalDateTime endTime;
    private LocalDateTime signupStartTime;
    private LocalDateTime signupEndTime;
    @NotNull
    @Min(1)
    private Integer capacity;
    private Long orgId;

    @AssertTrue(message = "报名时间必须早于活动开始时间，且开始早于结束")
    public boolean isSignupWindowValid() {
        if (signupStartTime == null && signupEndTime == null) {
            return true;
        }
        if (signupStartTime != null && signupEndTime != null && signupStartTime.isAfter(signupEndTime)) {
            return false;
        }
        if (signupEndTime != null && startTime != null && signupEndTime.isAfter(startTime)) {
            return false;
        }
        return true;
    }

    @AssertTrue(message = "活动开始时间必须早于结束时间")
    public boolean isEventTimeValid() {
        if (startTime == null || endTime == null) return true;
        return startTime.isBefore(endTime);
    }
}
