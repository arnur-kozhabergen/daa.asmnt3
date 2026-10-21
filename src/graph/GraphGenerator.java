package graph;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GraphGenerator {
    public static Graph undirected(int vertices, int edges, long seed) {
        long maximum = (long) vertices * (vertices - 1) / 2;
        if (vertices < 1 || edges < vertices - 1 || edges > maximum) {
            throw new IllegalArgumentException("Invalid connected graph size");
        }
        Graph graph = new Graph(vertices, false);
        Random random = new Random(seed);
        Set<Long> used = new HashSet<>();
        for (int to = 1; to < vertices; to++) add(graph, random.nextInt(to), to, random, used);
        while (graph.edges.size() < edges) {
            add(graph, random.nextInt(vertices), random.nextInt(vertices), random, used);
        }
        return graph;
    }

    public static Graph directed(int vertices, int edges, boolean dag, long seed) {
        long maximum = (long) vertices * (vertices - 1) / (dag ? 2 : 1);
        if (vertices < 1 || edges < 0 || edges > maximum || (!dag && (vertices < 3 || edges < 3))) {
            throw new IllegalArgumentException("Invalid directed graph size");
        }
        Graph graph = new Graph(vertices, true);
        Random random = new Random(seed);
        Set<Long> used = new HashSet<>();
        if (!dag) {
            add(graph, 0, 1, random, used);
            add(graph, 1, 2, random, used);
            add(graph, 2, 0, random, used);
        }
        while (graph.edges.size() < edges) {
            int from = random.nextInt(vertices);
            int to = random.nextInt(vertices);
            if (dag && from > to) {
                int temporary = from;
                from = to;
                to = temporary;
            }
            add(graph, from, to, random, used);
        }
        return graph;
    }

    private static void add(Graph graph, int from, int to, Random random, Set<Long> used) {
        if (from == to) return;
        int first = graph.directed ? from : Math.min(from, to);
        int second = graph.directed ? to : Math.max(from, to);
        long key = (long) first * graph.vertices + second;
        if (used.add(key)) graph.addEdge(from, to, random.nextInt(50) + 1);
    }
}
