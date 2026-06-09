package com.example.DATN.service;

import com.example.DATN.entity.document.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    public AiReview generateAiReview(ContentQuiz content, AttemptDetail attempt) {

        String prompt = buildPrompt(content, attempt);

        String apiUrl =
                "https://generativelanguage.googleapis.com/v1beta/models/"
                        + model
                        + ":generateContent?key="
                        + apiKey;

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of(
                                                "text",
                                                systemPrompt() + "\n\n" + prompt
                                        )
                                )
                        )
                )
        );

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            apiUrl,
                            body,
                            Map.class
                    );

            String jsonContent = extractContent(response.getBody());

            return parseReview(
                    jsonContent,
                    content.getQuestions()
            );

        } catch (Exception e) {
            log.error("Gemini review failed for attemptId={}",
                    attempt.getAttemptId(), e);

            throw new RuntimeException(
                    "Không thể kết nối Gemini AI"
            );
        }
    }

    private String systemPrompt() {
        return """
                Bạn là trợ lý giáo dục. Nhiệm vụ: phân tích bài làm của học sinh,
                chỉ ra lỗi sai, giải thích đáp án đúng và đề xuất lộ trình cải thiện.
                Trả lời bằng tiếng Việt, định dạng JSON thuần (không markdown).
                Schema:
                {
                  "overallAnalysis": "string",
                  "weaknessAreas": ["string"],
                  "studyRoadmap": "string",
                  "perQuestion": [
                    {
                      "questionIndex": 0,
                      "analysis": "string",
                      "correctApproach": "string"
                    }
                  ]
                }
                """;
    }

    private String buildPrompt(ContentQuiz content, AttemptDetail detail) {
        StringBuilder sb = new StringBuilder();
        sb.append("Thông tin bài làm:\n");
        sb.append("Tổng câu: ").append(content.getQuestions().size()).append("\n\n");

        List<Question> questions = content.getQuestions();
        List<Answer> answers = detail.getAnswers() != null
                ? detail.getAnswers()
                : Collections.emptyList();

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            sb.append("Câu ").append(i + 1).append(": ").append(q.getQuestionText()).append("\n");
            sb.append("Loại: ").append(q.getType()).append("\n");

            if (q.getOptions() != null) {
                sb.append("Đáp án đúng: ");
                for (int j = 0; j < q.getOptions().size(); j++) {
                    if (q.getOptions().get(j).isCorrect()) {
                        sb.append("[").append(j).append("] ")
                                .append(q.getOptions().get(j).getText()).append(" ");
                    }
                }
                sb.append("\n");
            }

            Optional<Answer> ans = answers.stream()
                    .filter(a -> a.getQuestionId().equals(q.getId()))
                    .findFirst();

            if (ans.isPresent()) {
                Answer a = ans.get();
                sb.append("Học sinh chọn: ");
                if (a.getSelectAnswer() != null) sb.append(a.getSelectAnswer());
                if (a.getTextAnswer() != null) sb.append(a.getTextAnswer());
                sb.append(" | Đúng: ").append(a.isCorrect()).append("\n");
            } else {
                sb.append("Học sinh: bỏ qua\n");
            }

            sb.append("Giải thích gốc: ")
                    .append(q.getExplanation() != null ? q.getExplanation() : "Không có")
                    .append("\n\n");
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<?, ?> body) {

        List<Map<String, Object>> candidates =
                (List<Map<String, Object>>) body.get("candidates");

        Map<String, Object> content =
                (Map<String, Object>) candidates.get(0).get("content");

        List<Map<String, Object>> parts =
                (List<Map<String, Object>>) content.get("parts");

        return (String) parts.get(0).get("text");
    }

    @SuppressWarnings("unchecked")
    private AiReview parseReview(String json, List<Question> questions) {
        try {
            Map<String, Object> map = objectMapper.readValue(json, Map.class);

            List<QuestionAnalysis> perQuestion = new ArrayList<>();
            List<Map<String, Object>> rawList =
                    (List<Map<String, Object>>) map.get("perQuestion");

            if (rawList != null) {
                for (Map<String, Object> item : rawList) {
                    perQuestion.add(QuestionAnalysis.builder()
                            .questionIndex(((Number) item.get("questionIndex")).intValue())
                            .analysis((String) item.get("analysis"))
                            .correctApproach((String) item.get("correctApproach"))
                            .build());
                }
            }

            return AiReview.builder()
                    .overallAnalysis((String) map.get("overallAnalysis"))
                    .weaknessAreas((List<String>) map.get("weaknessAreas"))
                    .studyRoadmap((String) map.get("studyRoadmap"))
                    .perQuestion(perQuestion)
                    .generatedAt(Instant.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse AI review response: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể xử lý phản hồi từ AI");
        }
    }
}