package com.example.langgraph.dto;

import java.util.List;
import java.util.Map;

public record WorkflowResponse(
        String topic,
        String topicAnalysis,
        String researchPoints,
        String draftContent,
        String reviewNotes,
        String finalContent,
        String summary,
        List<String> executionTrace,
        Map<String, Object> fullState
) {
}
