package daa;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public final class Benchmark {
    private static final int[] SIZES = {
            1000, 3162, 10000, 31623, 100000
    };

    private record Row(
            String algorithm, String graph,
            int n, int m, long time,
            long dsu, long scans, long comparisons,
            int depth, long result
    ) {}

    public static void run() throws Exception {
        Path out = Path.of("results");
        Files.createDirectories(out.resolve("plots"));

        List<Row> rows = new ArrayList<>();

        for (int n : SIZES) {
            for (int factor : new int[]{4, 20}) {
                int m = n * factor;

                Graph ug = generateUndirected(n, m, 42L);
                Graph dg = generateDirected(n, m, 42L);
                Graph dag = generateDag(n, m, 42L);

                String label = n + "_" + m;

                Algorithms.Metrics km = new Algorithms.Metrics();
                Algorithms.Metrics pm = new Algorithms.Metrics();

                long kt = median(() -> {
                    km.clear();
                    return Algorithms.kruskal(ug, km).weight();
                });

                km.clear();
                Algorithms.MstResult kr =
                        Algorithms.kruskal(ug, km);

                pm.clear();
                Algorithms.MstResult pr =
                        Algorithms.prim(ug, pm);

                long pt = median(() -> {
                    pm.clear();
                    return Algorithms.prim(ug, pm).weight();
                });

                if (kr.weight() != pr.weight()) {
                    throw new IllegalStateException(
                            "MST mismatch on " + label
                    );
                }

                rows.add(new Row(
                        "Kruskal", label, n, m, kt,
                        km.dsuOps, km.edgeScans,
                        km.comparisons, km.maxDepth, kr.weight()
                ));

                rows.add(new Row(
                        "Prim", label, n, m, pt,
                        pm.dsuOps, pm.edgeScans,
                        pm.comparisons, pm.maxDepth, pr.weight()
                ));

                Algorithms.Metrics sm = new Algorithms.Metrics();

                long st = median(() -> {
                    sm.clear();
                    return Algorithms.kosaraju(dg, sm).size();
                });

                sm.clear();
                int scc = Algorithms.kosaraju(dg, sm).size();

                rows.add(new Row(
                        "Kosaraju", label, n, m, st,
                        sm.dsuOps, sm.edgeScans,
                        sm.comparisons, sm.maxDepth, scc
                ));

                Algorithms.Metrics tm = new Algorithms.Metrics();

                long tt = median(() -> {
                    tm.clear();
                    return Algorithms.kahn(dag, tm).order().size();
                });

                tm.clear();
                Algorithms.TopologicalResult top =
                        Algorithms.kahn(dag, tm);

                if (top.hasCycle()
                        || !Algorithms.validTopologicalOrder(
                        dag, top.order())) {
                    throw new IllegalStateException(
                            "Invalid topological order"
                    );
                }

                rows.add(new Row(
                        "Kahn", label, n, m, tt,
                        tm.dsuOps, tm.edgeScans,
                        tm.comparisons, tm.maxDepth,
                        top.order().size()
                ));

                Algorithms.Metrics dm = new Algorithms.Metrics();

                long dt = median(() -> {
                    dm.clear();
                    return Algorithms.dagShortestPaths(dag, dm).sum();
                });

                dm.clear();
                Algorithms.PathResult path =
                        Algorithms.dagShortestPaths(dag, dm);

                rows.add(new Row(
                        "DagShortestPath", label, n, m, dt,
                        dm.dsuOps, dm.edgeScans,
                        dm.comparisons, dm.maxDepth, path.sum()
                ));

                System.out.printf(
                        "n=%d m=%d MST=%d SCC=%d DAG-distance-sum=%d%n",
                        n, m, kr.weight(), scc, path.sum()
                );
            }
        }

        try (PrintWriter w = new PrintWriter(
                Files.newBufferedWriter(out.resolve("results.csv")))) {

            w.println(
                    "algorithm,graph,n,m,time_ms,dsu_ops,"
                            + "edge_scans,comparisons,max_depth,result"
            );

            for (Row r : rows) {
                w.printf(
                        Locale.ROOT,
                        "%s,%s,%d,%d,%d,%d,%d,%d,%d,%d%n",
                        r.algorithm(), r.graph(), r.n(), r.m(),
                        r.time(), r.dsu(), r.scans(),
                        r.comparisons(), r.depth(), r.result()
                );
            }
        }

        writePlotCsv(rows, out.resolve("plot_data.csv"));

        System.out.println(
                "Wrote results/results.csv (" + rows.size() + " rows)."
        );
        System.out.println(
                "Generate PNG plots with scripts/plot_results.py"
        );
    }

    private interface Task {
        long run();
    }

    private static long median(Task task) {
        long[] times = new long[5];

        for (int i = 0; i < 5; i++) {
            long start = System.nanoTime();
            task.run();
            times[i] = (System.nanoTime() - start) / 1_000_000;
        }

        Arrays.sort(times);
        return times[2];
    }

    private static void shuffle(int[] p, Random random) {
        for (int i = p.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int x = p[i];
            p[i] = p[j];
            p[j] = x;
        }
    }

    public static Graph generateUndirected(
            int n, int m, long seed
    ) {
        Random random = new Random(seed);
        Graph g = new Graph(n, false);

        int[] perm = IntStream.range(0, n).toArray();
        shuffle(perm, random);

        for (int i = 1; i < n; i++) {
            g.addEdge(
                    perm[random.nextInt(i)],
                    perm[i],
                    1 + random.nextInt(1000)
            );
        }

        while (g.edgeCount() < m) {
            int a = random.nextInt(n);
            int b = random.nextInt(n);

            if (b != a) {
                g.addEdge(a, b, 1 + random.nextInt(1000));
            }
        }

        return g;
    }

    public static Graph generateDirected(
            int n, int m, long seed
    ) {
        Random random = new Random(seed);
        Graph g = new Graph(n, true);

        while (g.edgeCount() < m) {
            int a = random.nextInt(n);
            int b = random.nextInt(n);

            if (a != b) {
                g.addEdge(a, b, 1 + random.nextInt(1000));
            }
        }

        return g;
    }

    public static Graph generateDag(
            int n, int m, long seed
    ) {
        Random random = new Random(seed);
        Graph g = new Graph(n, true);

        int[] rank = IntStream.range(0, n).toArray();
        shuffle(rank, random);

        int[] pos = new int[n];

        for (int i = 0; i < n; i++) {
            pos[rank[i]] = i;
        }

        while (g.edgeCount() < m) {
            int a = random.nextInt(n);
            int b = random.nextInt(n);

            if (a == b) {
                continue;
            }

            if (pos[a] > pos[b]) {
                int x = a;
                a = b;
                b = x;
            }

            g.addEdge(a, b, 1 + random.nextInt(1000));
        }

        return g;
    }

    private static void writePlotCsv(
            List<Row> rows, Path path
    ) throws IOException {
        try (PrintWriter w = new PrintWriter(
                Files.newBufferedWriter(path))) {

            w.println(
                    "algorithm,graph,n,m,edge_scans,dsu_ops"
            );

            for (Row r : rows) {
                w.printf(
                        "%s,%s,%d,%d,%d,%d%n",
                        r.algorithm(), r.graph(), r.n(), r.m(),
                        r.scans(), r.dsu()
                );
            }
        }
    }
}