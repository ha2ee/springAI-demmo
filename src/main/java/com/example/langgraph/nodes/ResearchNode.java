package com.example.langgraph.nodes;

import com.example.langgraph.graph.GraphNode;
import com.example.langgraph.graph.GraphState;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ResearchNode implements GraphNode {

    private final ChatClient chatClient;

    public ResearchNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public GraphState process(GraphState state) {
        String topic = Objects.requireNonNull(state.getString("input_topic"), "input_topic is required");
        String analysis = Objects.requireNonNull(state.getString("topic_analysis"), "topic_analysis is required");

        String prompt = """
                Based on the topic and analysis below, generate 5-7 key research points \
                that should be covered in a blog post. Each point should include a brief \
                explanation of why it's important.

                Topic: %s

                Analysis:
                %s

                Format as a numbered list with explanations.
                """.formatted(topic, analysis);

        String researchPoints = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        state.put("research_points", researchPoints);
        return state;
    }
}
