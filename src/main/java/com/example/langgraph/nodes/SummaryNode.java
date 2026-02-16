package com.example.langgraph.nodes;

import com.example.langgraph.graph.GraphNode;
import com.example.langgraph.graph.GraphState;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SummaryNode implements GraphNode {

    private final ChatClient chatClient;

    public SummaryNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public GraphState process(GraphState state) {
        String finalContent = Objects.requireNonNull(
                state.getString("final_content"),
                "final_content is required");

        String prompt = """
                Based on the following blog post, generate:
                1. A concise summary (2-3 sentences)
                2. 5-7 SEO keywords
                3. A meta description (under 160 characters)

                Blog Post:
                %s

                Format your response as:
                SUMMARY:
                [summary here]

                SEO KEYWORDS:
                [comma-separated keywords]

                META DESCRIPTION:
                [meta description here]
                """.formatted(finalContent);

        String summary = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        state.put("summary", summary);
        state.put("completed", true);
        return state;
    }
}
