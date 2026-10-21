package shortestpath;

import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class Dijkstra {
    public static class Result {
        public final long[] distance;
        public final int[] previous;
        public long edgeChecks;
        public long comparisons;

        public Result(int vertices) {
            distance = new long[vertices];
            previous = new int[vertices];
            Arrays.fill(distance, Long.MAX_VALUE);
            Arrays.fill(previous, -1);
        }

        public List<Integer> pathTo(int target) {
            List<Integer> path = new ArrayList<>();
            if (distance[target] == Long.MAX_VALUE) return path;
            for (int vertex = target; vertex != -1; vertex = previous[vertex]) path.add(vertex);
            Collections.reverse(path);
            return path;
        }
    }

    private static class Visit {
        final int vertex;
        final long distance;

        Visit(int vertex, long distance) {
            this.vertex = vertex;
            this.distance = distance;
        }
    }

    public static Result shortestPaths(Graph graph, int source) {
        if (source < 0 || source >= graph.vertices) throw new IllegalArgumentException("Unknown source");
        Result result = new Result(graph.vertices);
        for (Edge edge : graph.edges) {
            result.edgeChecks++;
            if (edge.weight < 0) throw new IllegalArgumentException("Dijkstra needs non negative weights");
        }
        PriorityQueue<Visit> queue = new PriorityQueue<>((a, b) -> {
            result.comparisons++;
            return Long.compare(a.distance, b.distance);
        });
        result.distance[source] = 0;
        queue.add(new Visit(source, 0));
        while (!queue.isEmpty()) {
            Visit next = queue.remove();
            if (next.distance != result.distance[next.vertex]) continue;
            for (Edge edge : graph.adjacent.get(next.vertex)) {
                result.edgeChecks++;
                long candidate = next.distance + edge.weight;
                if (candidate < result.distance[edge.to]) {
                    result.distance[edge.to] = candidate;
                    result.previous[edge.to] = next.vertex;
                    queue.add(new Visit(edge.to, candidate));
                }
            }
        }
        return result;
    }
}
