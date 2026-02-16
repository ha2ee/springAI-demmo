package com.example.langgraph.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Compiled, executable graph that processes nodes sequentially.
 * Produced by {@link StateGraphBuilder#compile()}.
 */
public class StateGraph {

    private static final Logger log = LoggerFactory.getLogger(StateGraph.class);

    private final List<NodeEntry> nodes;

    StateGraph(List<NodeEntry> nodes) {
        this.nodes = nodes;
    }

    public GraphState execute(GraphState initialState) {
        GraphState state = initialState;
        log.info("Starting graph execution with {} nodes", nodes.size());

        for (NodeEntry entry : nodes) {
            log.info("Executing node: {}", entry.name());
            long start = System.currentTimeMillis();
            try {
                state = entry.node().process(state);
                long duration = System.currentTimeMillis() - start;
                state.addTrace(entry.name(), duration);
                log.info("Node '{}' completed in {}ms", entry.name(), duration);
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - start;
                state.addTrace(entry.name() + " [FAILED]", duration);
                log.error("Node '{}' failed after {}ms", entry.name(), duration, e);
                throw new RuntimeException("Graph execution failed at node: " + entry.name(), e);
            }
        }

        log.info("Graph execution completed. Trace: {}", state.getExecutionTrace());
        return state;
    }

    public List<String> getNodeNames() {
        return nodes.stream().map(NodeEntry::name).toList();
    }
}
