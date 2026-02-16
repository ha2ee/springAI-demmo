package com.example.langgraph.nodes;

import com.example.langgraph.graph.GraphNode;
import com.example.langgraph.graph.GraphState;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DraftWritingNode implements GraphNode {

    private final ChatClient chatClient;

    public DraftWritingNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public GraphState process(GraphState state) {
        String topic = Objects.requireNonNull(state.getString("input_topic"), "input_topic is required");
        String analysis = Objects.requireNonNull(state.getString("topic_analysis"), "topic_analysis is required");
        String research = Objects.requireNonNull(state.getString("research_points"), "research_points is required");

        String prompt = """
                Write a blog post draft (600-800 words) based on the following:

                Topic: %s

                Analysis:
                %s

                Research Points:
                %s

                Requirements:
                - Engaging introduction
                - Clear structure with headings
                - Practical examples where appropriate
                - Strong conclusion with call-to-action
                """.formatted(topic, analysis, research);

        String draft = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        state.put("draft_content", draft);
        return state;
    }
}
