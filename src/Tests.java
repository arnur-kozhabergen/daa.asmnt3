import connectivity.Connectivity;
import dsu.DSU;
import graph.Edge;
import graph.Graph;
import graph.GraphGenerator;
import java.util.Arrays;
import java.util.List;
import mst.MST;
import shortestpath.Dijkstra;

public class Tests {
    private static int checks;

    public static void main(String[] args) {
        testDSU();
        testMST();
        testConnectivity();
        testPaths();
        testGeneratedGraphs();
        System.out.println("Passed " + checks + " checks");
    }

    private static void testDSU() {
        DSU dsu = new DSU(5);
        check(dsu.union(0, 1), "First union");
        check(dsu.union(2, 3), "Second union");
        check(dsu.union(0, 2), "Join groups");
        check(!dsu.union(1, 3), "Already joined");
        check(dsu.find(3) == dsu.find(0), "Same root");
        check(dsu.find(4) != dsu.find(0), "Separate vertex");
        check(!dsu.union(4, 4), "Union with itself");
    }

    private static void testMST() {
        Graph graph = new Graph(4, false);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 2);
        graph.addEdge(2, 3, 3);
        graph.addEdge(0, 3, 10);
        graph.addEdge(0, 2, 8);
        checkMST(graph, 6, true, 3);

        Graph disconnected = new Graph(4, false);
        disconnected.addEdge(0, 1, 7);
        disconnected.addEdge(2, 3, 5);
        checkMST(disconnected, 12, false, 2);
        checkMST(new Graph(1, false), 0, true, 0);

        Graph tied = new Graph(3, false);
        tied.addEdge(0, 1, 2);
        tied.addEdge(1, 2, 2);
        tied.addEdge(0, 2, 2);
        tied.addEdge(0, 1, 9);
        tied.addEdge(0, 0, -5);
        checkMST(tied, 4, true, 2);

        Graph largeWeights = new Graph(3, false);
        largeWeights.addEdge(0, 1, Integer.MAX_VALUE);
        largeWeights.addEdge(1, 2, Integer.MAX_VALUE);
        checkMST(largeWeights, 2L * Integer.MAX_VALUE, true, 2);
        invalid(() -> MST.kruskal(new Graph(2, true)));
        invalid(() -> MST.prim(new Graph(2, true)));
    }

    private static void checkMST(Graph graph, long weight, boolean connected, int edges) {
        for (MST.Result result : new MST.Result[]{MST.kruskal(graph), MST.prim(graph)}) {
            check(result.weight == weight, "MST weight");
            check(result.connected == connected, "MST connectivity");
            check(result.edges.size() == edges, "MST edge count");
        }
    }

    private static void testConnectivity() {
        Graph dag = new Graph(6, true);
        dag.addEdge(0, 1, 1);
        dag.addEdge(0, 2, 1);
        dag.addEdge(1, 3, 1);
        dag.addEdge(2, 3, 1);
        dag.addEdge(3, 4, 1);
        checkOrder(dag);
        check(Connectivity.kosaraju(dag).groups.size() == 6, "DAG components");

        Graph cycles = new Graph(6, true);
        cycles.addEdge(0, 1, 1);
        cycles.addEdge(1, 2, 1);
        cycles.addEdge(2, 0, 1);
        cycles.addEdge(2, 3, 1);
        cycles.addEdge(3, 4, 1);
        cycles.addEdge(4, 3, 1);
        Connectivity.Order order = Connectivity.topological(cycles);
        check(!order.dag && order.vertices.isEmpty(), "Cycle has no order");
        List<List<Integer>> groups = Connectivity.kosaraju(cycles).groups;
        int[] component = new int[6];
        Arrays.fill(component, -1);
        for (int i = 0; i < groups.size(); i++) {
            for (int vertex : groups.get(i)) {
                check(component[vertex] == -1, "Vertex appears once");
                component[vertex] = i;
            }
        }
        for (int group : component) check(group != -1, "Every vertex is in an SCC");
        check(groups.size() == 3, "Three components");
        check(component[0] == component[1] && component[1] == component[2], "First SCC");
        check(component[3] == component[4] && component[3] != component[0], "Second SCC");
        check(component[5] != component[0] && component[5] != component[3], "Isolated SCC");

        Graph one = new Graph(1, true);
        checkOrder(one);
        check(Connectivity.kosaraju(one).groups.get(0).equals(List.of(0)), "Single SCC");
        one.addEdge(0, 0, 0);
        check(!Connectivity.topological(one).dag, "Self loop is a cycle");
        check(Connectivity.kosaraju(one).groups.size() == 1, "Self loop SCC");

        Graph chain = new Graph(1000, true);
        for (int i = 0; i < 999; i++) chain.addEdge(i, i + 1, 1);
        checkOrder(chain);
        check(Connectivity.kosaraju(chain).groups.size() == 1000, "Long chain SCCs");
        chain.addEdge(999, 0, 1);
        check(Connectivity.kosaraju(chain).groups.size() == 1, "Long cycle SCC");
        check(!Connectivity.topological(chain).dag, "Long cycle detection");
        invalid(() -> Connectivity.topological(new Graph(2, false)));
        invalid(() -> Connectivity.kosaraju(new Graph(2, false)));
    }

    private static void checkOrder(Graph graph) {
        Connectivity.Order order = Connectivity.topological(graph);
        check(order.dag && order.vertices.size() == graph.vertices, "Complete DAG order");
        int[] position = new int[graph.vertices];
        Arrays.fill(position, -1);
        for (int i = 0; i < order.vertices.size(); i++) {
            int vertex = order.vertices.get(i);
            check(position[vertex] == -1, "No duplicate in order");
            position[vertex] = i;
        }
        for (Edge edge : graph.edges) check(position[edge.from] < position[edge.to], "Edge follows order");
    }

    private static void testPaths() {
        Graph graph = new Graph(6, true);
        graph.addEdge(0, 1, 10);
        graph.addEdge(0, 2, 3);
        graph.addEdge(2, 1, 1);
        graph.addEdge(1, 3, 2);
        graph.addEdge(2, 3, 8);
        graph.addEdge(3, 4, 0);
        graph.addEdge(4, 1, 1);
        Dijkstra.Result paths = Dijkstra.shortestPaths(graph, 0);
        check(Arrays.equals(paths.distance, new long[]{0, 4, 3, 6, 6, Long.MAX_VALUE}), "Distances");
        check(paths.pathTo(4).equals(List.of(0, 2, 1, 3, 4)), "Restored path");
        check(paths.pathTo(0).equals(List.of(0)), "Source path");
        check(paths.pathTo(5).isEmpty(), "Unreachable path");
        check(Dijkstra.shortestPaths(graph, 5).distance[0] == Long.MAX_VALUE, "Isolated source");
        check(Dijkstra.shortestPaths(new Graph(1, false), 0).distance[0] == 0, "Single vertex distance");
        Graph large = new Graph(3, false);
        large.addEdge(0, 1, Integer.MAX_VALUE);
        large.addEdge(1, 2, Integer.MAX_VALUE);
        check(Dijkstra.shortestPaths(large, 0).distance[2] == 2L * Integer.MAX_VALUE, "Long distance");
        Graph negative = new Graph(3, true);
        negative.addEdge(1, 2, -1);
        invalid(() -> Dijkstra.shortestPaths(negative, 0));
        invalid(() -> Dijkstra.shortestPaths(graph, -1));
        invalid(() -> Dijkstra.shortestPaths(graph, 6));
        invalid(() -> new Graph(0, true));
        invalid(() -> graph.addEdge(0, 6, 1));
    }

    private static void testGeneratedGraphs() {
        int[][] sizes = {{10, 30}, {100, 500}, {1000, 5000}};
        for (int[] size : sizes) {
            Graph roads = GraphGenerator.undirected(size[0], size[1], 42);
            Graph copy = GraphGenerator.undirected(size[0], size[1], 42);
            check(roads.edges.size() == size[1], "Exact undirected edge count");
            for (int i = 0; i < roads.edges.size(); i++) {
                Edge a = roads.edges.get(i);
                Edge b = copy.edges.get(i);
                check(a.from == b.from && a.to == b.to && a.weight == b.weight, "Reproducible graph");
            }
            MST.Result kruskal = MST.kruskal(roads);
            MST.Result prim = MST.prim(roads);
            check(kruskal.connected && prim.connected, "Generated roads are connected");
            check(kruskal.weight == prim.weight, "Generated MST agreement");
            Graph dag = GraphGenerator.directed(size[0], size[1], true, 42);
            Graph cycles = GraphGenerator.directed(size[0], size[1], false, 42);
            check(dag.edges.size() == size[1] && cycles.edges.size() == size[1], "Exact directed edge count");
            checkOrder(dag);
            check(!Connectivity.topological(cycles).dag, "Generated cycle");
            int[] count = new int[size[0]];
            for (List<Integer> group : Connectivity.kosaraju(cycles).groups) {
                for (int vertex : group) count[vertex]++;
            }
            for (int total : count) check(total == 1, "Generated SCC partition");
            Dijkstra.Result paths = Dijkstra.shortestPaths(cycles, 0);
            check(paths.distance[0] == 0, "Generated source distance");
            for (Edge edge : cycles.edges) {
                check(edge.weight >= 0, "Generated weight");
                if (paths.distance[edge.from] != Long.MAX_VALUE) {
                    check(paths.distance[edge.to] <= paths.distance[edge.from] + edge.weight, "Shortest path inequality");
                }
            }
        }
    }

    private static void invalid(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected invalid input");
        } catch (IllegalArgumentException expected) {
            checks++;
        }
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
