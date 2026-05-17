package com.example.DATN.entity.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiReview {
    String overallAnalysis; // phan tic tong the
    List<String> weaknessAreas;    //diem yeu can cai thienj
    String studyRoadmap;
    List<QuestionAnalysis> perQuestion;
    Instant generatedAt;
}
