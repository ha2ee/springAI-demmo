package com.example.langgraph.workflow;

import com.example.langgraph.graph.StateGraph;
import com.example.langgraph.graph.StateGraphBuilder;
import com.example.langgraph.nodes.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContentCreationWorkflow {

    @Bean
    public StateGraphBuilder contentGraphBuilder(
            TopicAnalysisNode topicAnalysis,
            ResearchNode research,
            DraftWritingNode draftWriting,
            ReviewNode review,
            SummaryNode summary) {

        return new StateGraphBuilder()
                .addNode("topic_analysis", topicAnalysis)
                .addNode("research", research)
                .addNode("draft_writing", draftWriting)
                .addNode("review", review)
                .addNode("summary", summary)
                .addEdge("topic_analysis", "research")
                .addEdge("research", "draft_writing")
                .addEdge("draft_writing", "review")
                .addEdge("review", "summary")
                .setEntryPoint("topic_analysis");
    }

    @Bean
    public StateGraph contentGraph(StateGraphBuilder contentGraphBuilder) {
        return contentGraphBuilder.compile();
    }
}
