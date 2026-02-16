package com.example.langgraph.graph;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class StateGraphBuilderTest {

    @Test
    void shouldExecuteNodesInOrder() {
        AtomicInteger counter = new AtomicInteger(0);

        GraphNode nodeA = state -> {
            state.put("a_order", counter.getAndIncrement());
            state.put("a_result", "done");
            return state;
        };
        GraphNode nodeB = state -> {
            state.put("b_order", counter.getAndIncrement());
            state.put("b_result", state.getString("a_result") + "_b");
            return state;
        };
        GraphNode nodeC = state -> {
            state.put("c_order", counter.getAndIncrement());
            state.put("c_result", state.getString("b_result") + "_c");
            return state;
        };

        StateGraph graph = new StateGraphBuilder()
                .addNode("A", nodeA)
                .addNode("B", nodeB)
                .addNode("C", nodeC)
                .addEdge("A", "B")
                .addEdge("B", "C")
                .setEntryPoint("A")
                .compile();

        GraphState result = graph.execute(new GraphState());

        assertEquals(0, result.get("a_order", Integer.class));
        assertEquals(1, result.get("b_order", Integer.class));
        assertEquals(2, result.get("c_order", Integer.class));
        assertEquals("done_b_c", result.getString("c_result"));
        assertEquals(3, result.getExecutionTrace().size());
    }

    @Test
    void shouldRejectDuplicateNodeName() {
        StateGraphBuilder builder = new StateGraphBuilder()
                .addNode("A", state -> state);

        assertThrows(IllegalArgumentException.class, () ->
                builder.addNode("A", state -> state));
    }

    @Test
    void shouldRejectMissingEntryPoint() {
        StateGraphBuilder builder = new StateGraphBuilder()
                .addNode("A", state -> state);

        assertThrows(IllegalStateException.class, builder::compile);
    }

    @Test
    void shouldRejectEntryPointNotInNodes() {
        StateGraphBuilder builder = new StateGraphBuilder()
                .addNode("A", state -> state)
                .setEntryPoint("X");

        assertThrows(IllegalStateException.class, builder::compile);
    }

    @Test
    void shouldRejectEdgeToUnknownNode() {
        StateGraphBuilder builder = new StateGraphBuilder()
                .addNode("A", state -> state)
                .addEdge("A", "B")
                .setEntryPoint("A");

        assertThrows(IllegalStateException.class, builder::compile);
    }

    @Test
    void shouldDetectCycle() {
        StateGraphBuilder builder = new StateGraphBuilder()
                .addNode("A", state -> state)
                .addNode("B", state -> state)
                .addEdge("A", "B")
                .addEdge("B", "A")
                .setEntryPoint("A");

        assertThrows(IllegalStateException.class, builder::compile);
    }

    @Test
    void shouldPreserveInitialState() {
        GraphNode node = state -> {
            state.put("processed", true);
            return state;
        };

        StateGraph graph = new StateGraphBuilder()
                .addNode("A", node)
                .setEntryPoint("A")
                .compile();

        GraphState initial = new GraphState(Map.of("input", "hello"));
        GraphState result = graph.execute(initial);

        assertEquals("hello", result.getString("input"));
        assertEquals(true, result.get("processed", Boolean.class));
    }

    @Test
    void graphStateShouldTrackExecution() {
        GraphNode node = state -> state;

        StateGraph graph = new StateGraphBuilder()
                .addNode("step1", node)
                .addNode("step2", node)
                .addEdge("step1", "step2")
                .setEntryPoint("step1")
                .compile();

        GraphState result = graph.execute(new GraphState());

        assertEquals(2, result.getExecutionTrace().size());
        assertTrue(result.getExecutionTrace().get(0).startsWith("step1"));
        assertTrue(result.getExecutionTrace().get(1).startsWith("step2"));
    }
}
