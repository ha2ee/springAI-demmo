package com.example.langgraph.graph;

/**
 * Functional interface for a graph node processor.
 * Each node receives the current state, performs work, and returns the updated state.
 */
@FunctionalInterface
public interface GraphNode {

    GraphState process(GraphState state);
}
