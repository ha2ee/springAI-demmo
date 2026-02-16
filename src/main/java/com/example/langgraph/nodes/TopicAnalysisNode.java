package com.example.langgraph.nodes;

import com.example.langgraph.graph.GraphNode;
import com.example.langgraph.graph.GraphState;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class TopicAnalysisNode implements GraphNode {

    private final ChatClient chatClient;

    public TopicAnalysisNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public GraphState process(GraphState state) {
        String topic = Objects.requireNonNull(
                state.getString("input_topic"),
                "input_topic is required");

        String prompt = """
                Analyze the following topic for a blog post. Provide:
                1. Target audience
                2. Key angles to cover
                3. Recommended tone and style
                4. Main value proposition for readers

                Topic: %s

                Respond in a structured format.
                """.formatted(topic);

        String analysis = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        state.put("topic_analysis", analysis);
        return state;
    }
}
