package mst;

import dsu.DSU;
import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class MST {
    public static class Result {
        public final List<Edge> edges = new ArrayList<>();
        public long weight;
        public boolean connected;
        public long edgeChecks;
        public long comparisons;
        public long finds;
        public long unions;
    }

    public static Result kruskal(Graph graph) {
        if (graph.directed) throw new IllegalArgumentException("MST needs undirected roads");
        Result result = new Result();
        List<Edge> sorted = new ArrayList<>(graph.edges);
        sorted.sort((a, b) -> {
            result.comparisons++;
            return Integer.compare(a.weight, b.weight);
        });
        DSU dsu = new DSU(graph.vertices);
        for (Edge edge : sorted) {
            result.edgeChecks++;
            if (dsu.union(edge.from, edge.to)) {
                result.edges.add(edge);
                result.weight += edge.weight;
                if (result.edges.size() == graph.vertices - 1) break;
            }
        }
        result.connected = result.edges.size() == graph.vertices - 1;
        result.finds = dsu.finds;
        result.unions = dsu.unions;
        return result;
    }

    public static Result prim(Graph graph) {
        if (graph.directed) throw new IllegalArgumentException("MST needs undirected roads");
        Result result = new Result();
        boolean[] visited = new boolean[graph.vertices];
        PriorityQueue<Edge> queue = new PriorityQueue<>((a, b) -> {
            result.comparisons++;
            return Integer.compare(a.weight, b.weight);
        });
        int components = 0;
        for (int start = 0; start < graph.vertices; start++) {
            if (visited[start]) continue;
            components++;
            queue.add(new Edge(-1, start, 0));
            while (!queue.isEmpty()) {
                Edge next = queue.remove();
                if (visited[next.to]) continue;
                visited[next.to] = true;
                if (next.from != -1) {
                    result.edges.add(next);
                    result.weight += next.weight;
                }
                for (Edge edge : graph.adjacent.get(next.to)) {
                    result.edgeChecks++;
                    if (!visited[edge.to]) queue.add(edge);
                }
            }
        }
        result.connected = components == 1;
        return result;
    }
}
