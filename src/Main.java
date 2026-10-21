import connectivity.Connectivity;
import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.List;
import mst.MST;
import shortestpath.Dijkstra;

public class Main {
    private static final String[] NAMES = {
        "Central square", "Banana depot", "Hospital", "University", "Park", "Old station"
    };

    public static void main(String[] args) {
        int source = 0;
        try {
            if (args.length > 1) throw new IllegalArgumentException();
            if (args.length == 1) source = Integer.parseInt(args[0]);
            if (source < 0 || source >= NAMES.length) throw new IllegalArgumentException();
        } catch (IllegalArgumentException error) {
            System.out.println("Choose a source from 0 to 5");
            return;
        }

        Graph roads = new Graph(6, false);
        roads.addEdge(0, 1, 4);
        roads.addEdge(0, 2, 2);
        roads.addEdge(1, 2, 1);
        roads.addEdge(1, 3, 5);
        roads.addEdge(2, 3, 3);
        roads.addEdge(3, 4, 2);
        roads.addEdge(4, 5, 6);
        roads.addEdge(2, 5, 12);
        MST.Result kruskal = MST.kruskal(roads);
        MST.Result prim = MST.prim(roads);
        System.out.println("Kruskal cost " + kruskal.weight);
        System.out.println("Prim cost " + prim.weight);
        System.out.println("All districts connected " + kruskal.connected);
        for (Edge edge : kruskal.edges) {
            System.out.println(NAMES[edge.from] + " to " + NAMES[edge.to] + " cost " + edge.weight);
        }

        Graph services = new Graph(6, true);
        services.addEdge(0, 1, 1);
        services.addEdge(1, 2, 1);
        services.addEdge(2, 3, 1);
        services.addEdge(1, 4, 1);
        System.out.println("Service order " + names(Connectivity.topological(services).vertices, " then "));

        Graph oneWay = new Graph(6, true);
        oneWay.addEdge(0, 1, 4);
        oneWay.addEdge(1, 2, 1);
        oneWay.addEdge(2, 0, 2);
        oneWay.addEdge(2, 3, 3);
        oneWay.addEdge(3, 4, 2);
        oneWay.addEdge(4, 3, 2);
        System.out.println("One way roads contain a cycle " + !Connectivity.topological(oneWay).dag);
        for (List<Integer> group : Connectivity.kosaraju(oneWay).groups) {
            System.out.println("SCC members " + names(group, " and "));
        }

        Dijkstra.Result paths = Dijkstra.shortestPaths(oneWay, source);
        System.out.println("Routes from " + NAMES[source]);
        for (int target = 0; target < NAMES.length; target++) {
            if (paths.distance[target] == Long.MAX_VALUE) {
                System.out.println(NAMES[target] + " unreachable");
            } else {
                System.out.println(NAMES[target] + " distance " + paths.distance[target]
                        + " path " + names(paths.pathTo(target), " to "));
            }
        }
    }

    private static String names(List<Integer> vertices, String separator) {
        List<String> names = new ArrayList<>();
        for (int vertex : vertices) names.add(NAMES[vertex]);
        return String.join(separator, names);
    }
}
