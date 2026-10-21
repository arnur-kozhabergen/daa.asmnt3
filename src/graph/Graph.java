package graph;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    public final int vertices;
    public final boolean directed;
    public final List<Edge> edges = new ArrayList<>();
    public final List<List<Edge>> adjacent = new ArrayList<>();

    public Graph(int vertices, boolean directed) {
        if (vertices < 1) throw new IllegalArgumentException("A graph needs a vertex");
        this.vertices = vertices;
        this.directed = directed;
        for (int i = 0; i < vertices; i++) adjacent.add(new ArrayList<>());
    }

    public void addEdge(int from, int to, int weight) {
        if (from < 0 || from >= vertices || to < 0 || to >= vertices) {
            throw new IllegalArgumentException("Unknown vertex");
        }
        Edge edge = new Edge(from, to, weight);
        edges.add(edge);
        adjacent.get(from).add(edge);
        if (!directed) adjacent.get(to).add(new Edge(to, from, weight));
    }
}
