package com.example.DATN.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    private String keyword;
    private int page = 0;
    private int size = 20;
}
