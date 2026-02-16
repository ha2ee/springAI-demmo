package com.example.langgraph.graph;

import java.util.*;

/**
 * Fluent builder for constructing a StateGraph.
 * Mirrors LangGraph's StateGraph builder API.
 *
 * Usage:
 * <pre>
 * StateGraph graph = new StateGraphBuilder()
 *     .addNode("analyze", analyzeNode)
 *     .addNode("research", researchNode)
 *     .addEdge("analyze", "research")
 *     .setEntryPoint("analyze")
 *     .compile();
 * </pre>
 */
public class StateGraphBuilder {

    private final Map<String, GraphNode> nodes = new LinkedHashMap<>();
    private final List<Edge> edges = new ArrayList<>();
    private String entryPoint;

    public StateGraphBuilder addNode(String name, GraphNode node) {
        Objects.requireNonNull(name, "Node name must not be null");
        Objects.requireNonNull(node, "Node must not be null");
        if (nodes.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate node name: " + name);
        }
        nodes.put(name, node);
        return this;
    }

    public StateGraphBuilder addEdge(String from, String to) {
        Objects.requireNonNull(from, "Edge 'from' must not be null");
        Objects.requireNonNull(to, "Edge 'to' must not be null");
        edges.add(new Edge(from, to));
        return this;
    }

    public StateGraphBuilder setEntryPoint(String nodeName) {
        Objects.requireNonNull(nodeName, "Entry point must not be null");
        this.entryPoint = nodeName;
        return this;
    }

    public StateGraph compile() {
        validate();
        List<String> executionOrder = resolveExecutionOrder();
        List<NodeEntry> orderedNodes = executionOrder.stream()
                .map(name -> new NodeEntry(name, nodes.get(name)))
                .toList();
        return new StateGraph(orderedNodes);
    }

    private void validate() {
        if (entryPoint == null) {
            throw new IllegalStateException("Entry point must be set before compiling");
        }
        if (!nodes.containsKey(entryPoint)) {
            throw new IllegalStateException("Entry point '" + entryPoint + "' is not a registered node");
        }
        for (Edge edge : edges) {
            if (!nodes.containsKey(edge.from())) {
                throw new IllegalStateException("Edge references unknown node: " + edge.from());
            }
            if (!nodes.containsKey(edge.to())) {
                throw new IllegalStateException("Edge references unknown node: " + edge.to());
            }
        }
        detectCycle();
    }

    private void detectCycle() {
        Map<String, List<String>> adjacency = new HashMap<>();
        for (String name : nodes.keySet()) {
            adjacency.put(name, new ArrayList<>());
        }
        for (Edge edge : edges) {
            adjacency.get(edge.from()).add(edge.to());
        }

        Set<String> visited = new HashSet<>();
        Set<String> inStack = new HashSet<>();

        for (String node : nodes.keySet()) {
            if (hasCycleDfs(node, adjacency, visited, inStack)) {
                throw new IllegalStateException("Cycle detected in graph");
            }
        }
    }

    private boolean hasCycleDfs(String node, Map<String, List<String>> adjacency,
                                Set<String> visited, Set<String> inStack) {
        if (inStack.contains(node)) {
            return true;
        }
        if (visited.contains(node)) {
            return false;
        }
        visited.add(node);
        inStack.add(node);
        for (String neighbor : adjacency.get(node)) {
            if (hasCycleDfs(neighbor, adjacency, visited, inStack)) {
                return true;
            }
        }
        inStack.remove(node);
        return false;
    }

    private List<String> resolveExecutionOrder() {
        // Topological sort starting from entryPoint, following edges
        Map<String, List<String>> adjacency = new HashMap<>();
        for (String name : nodes.keySet()) {
            adjacency.put(name, new ArrayList<>());
        }
        for (Edge edge : edges) {
            adjacency.get(edge.from()).add(edge.to());
        }

        List<String> order = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(entryPoint);
        visited.add(entryPoint);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            order.add(current);
            for (String next : adjacency.get(current)) {
                if (!visited.contains(next)) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }
        return order;
    }

    public List<String> getNodeNames() {
        return new ArrayList<>(nodes.keySet());
    }
}
