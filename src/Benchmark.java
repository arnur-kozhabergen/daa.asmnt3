import connectivity.Connectivity;
import graph.Graph;
import graph.GraphGenerator;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import mst.MST;
import shortestpath.Dijkstra;

public class Benchmark {
    private static class Measurement {
        long nanoseconds;
        long edgeChecks;
        long comparisons;
        long finds;
        long unions;
        String answer;
    }

    public static void main(String[] args) throws IOException {
        int[][] sizes = {{10, 30}, {100, 500}, {1000, 5000}};
        Path folder = Path.of("results", "tables");
        Files.createDirectories(folder);
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(folder.resolve("timings.csv")));
             PrintWriter chart = new PrintWriter(Files.newBufferedWriter(folder.resolve("chart-data.tsv")))) {
            writer.println("algorithm,graph,V,E,seed,repeats,mean_ns,edge_checks,comparisons,find_calls,union_calls,result");
            chart.println("Series\tV\tE\tMean ms");
            for (int[] size : sizes) {
                Graph roads = GraphGenerator.undirected(size[0], size[1], 42);
                Graph dag = GraphGenerator.directed(size[0], size[1], true, 42);
                Graph cycles = GraphGenerator.directed(size[0], size[1], false, 42);
                benchmark(writer, chart, "Kruskal", "undirected", roads);
                benchmark(writer, chart, "Prim", "undirected", roads);
                benchmark(writer, chart, "Kosaraju", "cyclic", cycles);
                benchmark(writer, chart, "Topological", "DAG", dag);
                benchmark(writer, chart, "Topological", "cyclic", cycles);
                benchmark(writer, chart, "Dijkstra", "cyclic", cycles);
            }
        }
        System.out.println("Saved 18 benchmark rows in results/tables/timings.csv");
        System.out.println("Java " + System.getProperty("java.version"));
        System.out.println(System.getProperty("os.name") + " " + System.getProperty("os.arch"));
    }

    private static void benchmark(PrintWriter writer, PrintWriter chart, String algorithm, String kind, Graph graph) {
        for (int i = 0; i < 2; i++) measure(algorithm, graph);
        long total = 0;
        Measurement first = null;
        for (int run = 0; run < 5; run++) {
            Measurement current = measure(algorithm, graph);
            total += current.nanoseconds;
            if (first == null) first = current;
            if (!first.answer.equals(current.answer) || first.edgeChecks != current.edgeChecks
                    || first.comparisons != current.comparisons || first.finds != current.finds
                    || first.unions != current.unions) {
                throw new IllegalStateException("Repeated runs must agree");
            }
        }
        writer.printf(Locale.ROOT, "%s,%s,%d,%d,42,5,%.1f,%d,%d,%d,%d,%s%n",
                algorithm, kind, graph.vertices, graph.edges.size(), total / 5.0,
                first.edgeChecks, first.comparisons, first.finds, first.unions, first.answer);
        String series = algorithm.equals("Topological") ? algorithm + " " + kind : algorithm;
        chart.printf(Locale.ROOT, "%s\t%d\t%d\t%.6f%n", series, graph.vertices,
                graph.edges.size(), total / 5.0 / 1000000);
    }

    private static Measurement measure(String algorithm, Graph graph) {
        Measurement data = new Measurement();
        switch (algorithm) {
            case "Kruskal", "Prim" -> {
                long start = System.nanoTime();
                MST.Result result = algorithm.equals("Kruskal") ? MST.kruskal(graph) : MST.prim(graph);
                data.nanoseconds = System.nanoTime() - start;
                data.edgeChecks = result.edgeChecks;
                data.comparisons = result.comparisons;
                data.finds = result.finds;
                data.unions = result.unions;
                data.answer = "weight " + result.weight;
            }
            case "Kosaraju" -> {
                long start = System.nanoTime();
                Connectivity.Components result = Connectivity.kosaraju(graph);
                data.nanoseconds = System.nanoTime() - start;
                data.edgeChecks = result.edgeChecks;
                data.answer = "components " + result.groups.size();
            }
            case "Topological" -> {
                long start = System.nanoTime();
                Connectivity.Order result = Connectivity.topological(graph);
                data.nanoseconds = System.nanoTime() - start;
                data.edgeChecks = result.edgeChecks;
                data.answer = result.dag ? "ordered " + result.vertices.size() : "cycle";
            }
            case "Dijkstra" -> {
                long start = System.nanoTime();
                Dijkstra.Result result = Dijkstra.shortestPaths(graph, 0);
                data.nanoseconds = System.nanoTime() - start;
                data.edgeChecks = result.edgeChecks;
                data.comparisons = result.comparisons;
                int reachable = 0;
                for (long distance : result.distance) {
                    if (distance != Long.MAX_VALUE) reachable++;
                }
                data.answer = "reachable " + reachable;
            }
            default -> throw new IllegalArgumentException("Unknown algorithm");
        }
        return data;
    }
}
