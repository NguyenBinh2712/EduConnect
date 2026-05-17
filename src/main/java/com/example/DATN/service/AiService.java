package com.example.DATN.service;

import com.example.DATN.dto.quiz.AiReviewResponse;
import com.example.DATN.entity.document.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import org.springframework.http.HttpHeaders;import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    RestTemplate restTemplate;
    ObjectMapper objectMapper;

    public AiReview generateAiReview(ContentQuiz content, AttemptDetail attempt){
        String prompt = buildPrompt(content, attempt);

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt()),
                        Map.of("role", "user", "content", prompt)
                ),
                "response_format", Map.of("type", "json_object"),
                "temperature", 0.4,
                "max_tokens", 2000
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            String content2 = extractContent(response.getBody());
            return parseReview(content2, attempt.getAnswers(), content.getQuestions());

        } catch (Exception e) {
            log.error("OpenAI review failed for attemptId={}", attempt.getAttemptId(), e);
            throw new RuntimeException("Không thể kết nối AI, vui lòng thử lại sau");
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
        List<Answer> answers = detail.getAnswers();

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            sb.append("Câu ").append(i + 1).append(": ").append(q.getQuestionText()).append("\n");
            sb.append("Loại: ").append(q.getType()).append("\n");

            // Đáp án đúng
            if (q.getOptions() != null) {
                sb.append("Đáp án đúng: ");
                for (int j = 0; j < q.getOptions().size(); j++) {
                    if (q.getOptions().get(j).isCorrect()) {
                        sb.append("[").append(j).append("] ").append(q.getOptions().get(j).getText()).append(" ");
                    }
                }
                sb.append("\n");
            }

            // Học sinh chọn gì
            int idx = i;
            Optional<Answer> ans = answers.stream()
                    .filter(a -> a.getSelectAnswer().equals(idx))
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

            sb.append("Giải thích gốc: ").append(q.getExplanation() != null ? q.getExplanation() : "Không có").append("\n\n");
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map body) {
        List<Map> choices = (List<Map>) body.get("choices");
        Map message = (Map) choices.get(0).get("message");
        return (String) message.get("content");
    }

    @SuppressWarnings("unchecked")
    private AiReview parseReview(
            String json,
            List<Answer> answers,
            List<Question> questions
    ) throws Exception {
        Map<String, Object> map = objectMapper.readValue(json, Map.class);

        List<QuestionAnalysis> perQuestion = new ArrayList<>();
        List<Map<String, Object>> rawList = (List<Map<String, Object>>) map.get("perQuestion");
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
    }

}
