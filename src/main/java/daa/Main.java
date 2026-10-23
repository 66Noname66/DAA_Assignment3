package daa;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0
                || args[0].equalsIgnoreCase("benchmark")) {
            Benchmark.run();
            return;
        }

        Graph g = Graph.read(Path.of(args[0]));

        System.out.printf(
                "Graph: V=%d E=%d directed=%s%n",
                g.size(), g.edgeCount(), g.isDirected()
        );

        if (g.isDirected()) {
            var scc = Algorithms.kosaraju(
                    g, new Algorithms.Metrics()
            );
            System.out.println("SCC count: " + scc.size());

            var top = Algorithms.kahn(
                    g, new Algorithms.Metrics()
            );
            System.out.println("Acyclic: " + !top.hasCycle());

            if (!top.hasCycle()) {
                var paths = Algorithms.dagShortestPaths(
                        g, new Algorithms.Metrics()
                );
                System.out.println(
                        "Sum of distances: " + paths.sum()
                );
            }
        } else {
            var kruskal = Algorithms.kruskal(
                    g, new Algorithms.Metrics()
            );
            var prim = Algorithms.prim(
                    g, new Algorithms.Metrics()
            );

            System.out.printf(
                    "Kruskal forest weight=%d components=%d; "
                            + "Prim forest weight=%d components=%d%n",
                    kruskal.weight(), kruskal.components(),
                    prim.weight(), prim.components()
            );
        }
    }
}