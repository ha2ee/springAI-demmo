package com.example.langgraph.controller;

import com.example.langgraph.dto.StepInfo;
import com.example.langgraph.dto.WorkflowRequest;
import com.example.langgraph.dto.WorkflowResponse;
import com.example.langgraph.graph.GraphState;
import com.example.langgraph.graph.StateGraph;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflow")
@Tag(name = "Content Creation Workflow", description = "LangGraph-style content creation pipeline")
public class WorkflowController {

    private final StateGraph contentGraph;

    public WorkflowController(StateGraph contentGraph) {
        this.contentGraph = contentGraph;
    }

    @PostMapping("/content-creation")
    @Operation(summary = "Execute the full content creation pipeline",
            description = "Runs all 5 stages: topic analysis, research, draft writing, review, and summary")
    public ResponseEntity<WorkflowResponse> executeWorkflow(@RequestBody WorkflowRequest request) {
        GraphState initialState = new GraphState(Map.of("input_topic", request.topic()));
        GraphState result = contentGraph.execute(initialState);

        WorkflowResponse response = new WorkflowResponse(
                request.topic(),
                result.getString("topic_analysis"),
                result.getString("research_points"),
                result.getString("draft_content"),
                result.getString("review_notes"),
                result.getString("final_content"),
                result.getString("summary"),
                result.getExecutionTrace(),
                result.asMap()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/content-creation/steps")
    @Operation(summary = "List pipeline steps",
            description = "Returns the ordered list of steps in the content creation pipeline")
    public ResponseEntity<List<StepInfo>> getSteps() {
        List<String> nodeNames = contentGraph.getNodeNames();
        Map<String, String> descriptions = Map.of(
                "topic_analysis", "Analyzes the input topic to determine target audience, key angles, and tone",
                "research", "Generates 5-7 key research points based on the topic analysis",
                "draft_writing", "Writes a 600-800 word blog post draft using the analysis and research",
                "review", "Reviews the draft and produces a polished final version",
                "summary", "Generates a summary, SEO keywords, and meta description"
        );

        List<StepInfo> steps = new java.util.ArrayList<>();
        for (int i = 0; i < nodeNames.size(); i++) {
            String name = nodeNames.get(i);
            steps.add(new StepInfo(i + 1, name, descriptions.getOrDefault(name, "")));
        }
        return ResponseEntity.ok(steps);
    }
}
