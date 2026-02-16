package com.example.langgraph.nodes;

import com.example.langgraph.graph.GraphNode;
import com.example.langgraph.graph.GraphState;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ReviewNode implements GraphNode {

    private final ChatClient chatClient;

    public ReviewNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public GraphState process(GraphState state) {
        String draft = Objects.requireNonNull(state.getString("draft_content"), "draft_content is required");

        // First LLM call: review
        String reviewPrompt = """
                Review the following blog post draft. Provide specific feedback on:
                1. Content accuracy and completeness
                2. Writing quality and clarity
                3. Structure and flow
                4. Engagement and readability
                5. Specific suggestions for improvement

                Draft:
                %s
                """.formatted(draft);

        String reviewNotes = chatClient.prompt()
                .user(reviewPrompt)
                .call()
                .content();

        state.put("review_notes", reviewNotes);

        // Second LLM call: polish based on review
        String polishPrompt = """
                Improve the following blog post based on the review feedback. \
                Apply all suggestions while maintaining the original voice and message.

                Original Draft:
                %s

                Review Feedback:
                %s

                Produce the final polished version of the blog post.
                """.formatted(draft, reviewNotes);

        String finalContent = chatClient.prompt()
                .user(polishPrompt)
                .call()
                .content();

        state.put("final_content", finalContent);
        return state;
    }
}
