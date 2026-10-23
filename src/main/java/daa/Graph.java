package daa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class Graph {
    public record Edge(int from, int to, int weight) {}
    public record Arc(int to, int weight) {}

    private final int n;
    private final boolean directed;
    private final List<List<Arc>> adj;
    private final List<Edge> edges = new ArrayList<>();

    public Graph(int n, boolean directed) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be non-negative");
        }

        this.n = n;
        this.directed = directed;
        adj = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
    }

    public int size() {
        return n;
    }

    public boolean isDirected() {
        return directed;
    }

    public List<Arc> neighbors(int v) {
        return Collections.unmodifiableList(adj.get(v));
    }

    public List<Edge> edges() {
        return Collections.unmodifiableList(edges);
    }

    public int edgeCount() {
        return edges.size();
    }

    public void addEdge(int u, int v, int w) {
        check(u);
        check(v);

        edges.add(new Edge(u, v, w));
        adj.get(u).add(new Arc(v, w));

        if (!directed && u != v) {
            adj.get(v).add(new Arc(u, w));
        }
    }

    private void check(int v) {
        if (v < 0 || v >= n) {
            throw new IndexOutOfBoundsException("vertex: " + v);
        }
    }

    public Graph reversed() {
        Graph r = new Graph(n, true);

        for (Edge e : edges) {
            r.addEdge(e.to(), e.from(), e.weight());
        }

        return r;
    }

    public int[] indegrees() {
        int[] degree = new int[n];

        for (Edge e : edges) {
            degree[e.to()]++;
        }

        return degree;
    }

    public static Graph read(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path)
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty() && !s.startsWith("#"))
                .toList();

        if (lines.isEmpty()) {
            throw new IOException("Empty graph file: " + path);
        }

        String[] header = lines.get(0).split("\\s+");

        if (header.length != 3) {
            throw new IOException(
                    "Header must be: n m directed|undirected"
            );
        }

        int n = Integer.parseInt(header[0]);
        int m = Integer.parseInt(header[1]);

        boolean directed = switch (header[2].toLowerCase(Locale.ROOT)) {
            case "directed" -> true;
            case "undirected" -> false;
            default -> throw new IOException(
                    "Expected directed or undirected"
            );
        };

        if (lines.size() != m + 1) {
            throw new IOException(
                    "Expected " + m + " edges, found " + (lines.size() - 1)
            );
        }

        Graph graph = new Graph(n, directed);

        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\\s+");

            if (parts.length != 3) {
                throw new IOException("Bad edge line " + (i + 1));
            }

            graph.addEdge(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])
            );
        }

        return graph;
    }
}