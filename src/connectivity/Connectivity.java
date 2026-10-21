package connectivity;

import graph.Edge;
import graph.Graph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Connectivity {
    public static class Order {
        public final List<Integer> vertices = new ArrayList<>();
        public boolean dag;
        public long edgeChecks;
    }

    public static class Components {
        public final List<List<Integer>> groups = new ArrayList<>();
        public long edgeChecks;
    }

    public static Order topological(Graph graph) {
        if (!graph.directed) throw new IllegalArgumentException("Topological order needs directed roads");
        Order result = new Order();
        int[] indegree = new int[graph.vertices];
        for (Edge edge : graph.edges) {
            result.edgeChecks++;
            indegree[edge.to]++;
        }
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < graph.vertices; i++) {
            if (indegree[i] == 0) queue.add(i);
        }
        while (!queue.isEmpty()) {
            int vertex = queue.remove();
            result.vertices.add(vertex);
            for (Edge edge : graph.adjacent.get(vertex)) {
                result.edgeChecks++;
                if (--indegree[edge.to] == 0) queue.add(edge.to);
            }
        }
        result.dag = result.vertices.size() == graph.vertices;
        if (!result.dag) result.vertices.clear();
        return result;
    }

    public static Components kosaraju(Graph graph) {
        if (!graph.directed) throw new IllegalArgumentException("SCC needs directed roads");
        Components result = new Components();
        boolean[] visited = new boolean[graph.vertices];
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < graph.vertices; i++) {
            if (!visited[i]) finish(graph, i, visited, order, result);
        }
        Graph reversed = new Graph(graph.vertices, true);
        for (Edge edge : graph.edges) {
            result.edgeChecks++;
            reversed.addEdge(edge.to, edge.from, edge.weight);
        }
        visited = new boolean[graph.vertices];
        Collections.reverse(order);
        for (int vertex : order) {
            if (visited[vertex]) continue;
            List<Integer> group = new ArrayList<>();
            collect(reversed, vertex, visited, group, result);
            result.groups.add(group);
        }
        return result;
    }

    private static void finish(Graph graph, int vertex, boolean[] visited,
                               List<Integer> order, Components result) {
        visited[vertex] = true;
        for (Edge edge : graph.adjacent.get(vertex)) {
            result.edgeChecks++;
            if (!visited[edge.to]) finish(graph, edge.to, visited, order, result);
        }
        order.add(vertex);
    }

    private static void collect(Graph graph, int vertex, boolean[] visited,
                                List<Integer> group, Components result) {
        visited[vertex] = true;
        group.add(vertex);
        for (Edge edge : graph.adjacent.get(vertex)) {
            result.edgeChecks++;
            if (!visited[edge.to]) collect(graph, edge.to, visited, group, result);
        }
    }
}
