package com.example.DATN.dto.post;

import com.example.DATN.entity.enums.ReportReason;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequest {

    @NotNull
    private ReportReason reason;

    private String description;
}