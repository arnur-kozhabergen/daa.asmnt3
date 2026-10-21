package dsu;

public class DSU {
    private final int[] parent;
    private final int[] size;
    public long finds;
    public long unions;

    public DSU(int vertices) {
        parent = new int[vertices];
        size = new int[vertices];
        for (int i = 0; i < vertices; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    public int find(int vertex) {
        finds++;
        if (parent[vertex] != vertex) parent[vertex] = find(parent[vertex]);
        return parent[vertex];
    }

    public boolean union(int first, int second) {
        unions++;
        int a = find(first);
        int b = find(second);
        if (a == b) return false;
        if (size[a] < size[b]) {
            int temporary = a;
            a = b;
            b = temporary;
        }
        parent[b] = a;
        size[a] += size[b];
        return true;
    }
}
