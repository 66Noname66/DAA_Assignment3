package daa;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmsTest {

    @Test
    void dsuComponents() {
        Algorithms.DSU dsu = new Algorithms.DSU(6);

        dsu.union(0, 1);
        dsu.union(1, 2);
        dsu.union(4, 5);

        assertEquals(dsu.find(0), dsu.find(2));
        assertNotEquals(dsu.find(0), dsu.find(3));
        assertEquals(3, dsu.components());
    }

    @Test
    void mstAgreeAndDisconnectedForest() {
        Graph g = new Graph(5, false);

        g.addEdge(0, 1, 4);
        g.addEdge(1, 2, 2);
        g.addEdge(0, 2, 7);
        g.addEdge(3, 4, 1);

        var kruskal = Algorithms.kruskal(
                g, new Algorithms.Metrics()
        );
        var prim = Algorithms.prim(
                g, new Algorithms.Metrics()
        );

        assertEquals(7, kruskal.weight());
        assertEquals(kruskal.weight(), prim.weight());
        assertEquals(2, kruskal.components());
        assertEquals(2, prim.components());
    }

    @Test
    void kosarajuScc() {
        Graph g = new Graph(5, true);

        g.addEdge(0, 1, 1);
        g.addEdge(1, 0, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 3, 1);
        g.addEdge(3, 2, 1);

        assertEquals(
                3,
                Algorithms.kosaraju(
                        g, new Algorithms.Metrics()
                ).size()
        );
    }

    @Test
    void kahnDetectsCycleAndValidOrder() {
        Graph g = new Graph(4, true);

        g.addEdge(0, 1, 1);
        g.addEdge(0, 2, 1);
        g.addEdge(2, 3, 1);

        var result = Algorithms.kahn(
                g, new Algorithms.Metrics()
        );

        assertFalse(result.hasCycle());
        assertTrue(
                Algorithms.validTopologicalOrder(g, result.order())
        );

        g.addEdge(3, 0, 1);

        assertTrue(
                Algorithms.kahn(
                        g, new Algorithms.Metrics()
                ).hasCycle()
        );
    }

    @Test
    void dagNegativeEdgesAndParents() {
        Graph g = new Graph(4, true);

        g.addEdge(0, 1, 2);
        g.addEdge(1, 2, -5);
        g.addEdge(0, 2, 4);

        var result = Algorithms.dagShortestPaths(
                g, new Algorithms.Metrics()
        );

        assertEquals(-3, result.distance()[2]);
        assertEquals(List.of(0, 1, 2), result.pathTo(2));
    }

    @Test
    void stackSafeLongPath() {
        int n = 100_000;
        Graph g = new Graph(n, true);

        for (int i = 0; i < n - 1; i++) {
            g.addEdge(i, i + 1, 1);
        }

        assertEquals(
                n,
                Algorithms.kosaraju(
                        g, new Algorithms.Metrics()
                ).size()
        );
    }

    @Test
    void generatedMstsAgree() {
        Random random = new Random(7);

        for (int z = 0; z < 100; z++) {
            int n = 2 + random.nextInt(25);
            Graph g = new Graph(n, false);

            for (int i = 1; i < n; i++) {
                g.addEdge(
                        i,
                        random.nextInt(i),
                        1 + random.nextInt(100)
                );
            }

            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (random.nextDouble() < 0.12) {
                        g.addEdge(
                                i, j, 1 + random.nextInt(100)
                        );
                    }
                }
            }

            assertEquals(
                    Algorithms.kruskal(
                            g, new Algorithms.Metrics()
                    ).weight(),
                    Algorithms.prim(
                            g, new Algorithms.Metrics()
                    ).weight()
            );
        }
    }
}