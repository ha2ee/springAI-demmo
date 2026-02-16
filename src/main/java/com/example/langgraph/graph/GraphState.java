package com.example.langgraph.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe state container that flows through graph nodes.
 * Mirrors LangGraph's State (TypedDict) concept.
 */
public class GraphState {

    private final ConcurrentHashMap<String, Object> data;
    private final List<String> executionTrace;

    public GraphState() {
        this.data = new ConcurrentHashMap<>();
        this.executionTrace = Collections.synchronizedList(new ArrayList<>());
    }

    public GraphState(Map<String, Object> initialData) {
        this();
        if (initialData != null) {
            this.data.putAll(initialData);
        }
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }

    public String getString(String key) {
        return get(key, String.class);
    }

    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    public Map<String, Object> asMap() {
        return Collections.unmodifiableMap(data);
    }

    public void addTrace(String nodeName, long durationMs) {
        executionTrace.add(nodeName + " (" + durationMs + "ms)");
    }

    public List<String> getExecutionTrace() {
        return Collections.unmodifiableList(executionTrace);
    }
}
