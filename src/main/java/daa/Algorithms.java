package daa;

import java.util.*;

public final class Algorithms {
    private Algorithms() {}

    public static final class Metrics {
        public long dsuOps;
        public long edgeScans;
        public long comparisons;
        public int maxDepth;

        public void clear() {
            dsuOps = 0;
            edgeScans = 0;
            comparisons = 0;
            maxDepth = 0;
        }
    }

    public static final class DSU {
        private final int[] parent;
        private final int[] rank;
        private int components;

        public long findCalls;
        public long successfulUnions;

        public DSU(int n) {
            parent = new int[n];
            rank = new int[n];
            components = n;

            for (int i = 0; i < n; i++) {
                parent[i] = i;
            }
        }

        public int find(int x) {
            findCalls++;

            if (parent[x] != x) {
                parent[x] = find(parent[x]);
            }

            return parent[x];
        }

        public boolean union(int a, int b) {
            int x = find(a);
            int y = find(b);

            if (x == y) {
                return false;
            }

            successfulUnions++;

            if (rank[x] < rank[y]) {
                parent[x] = y;
            } else if (rank[x] > rank[y]) {
                parent[y] = x;
            } else {
                parent[y] = x;
                rank[x]++;
            }

            components--;
            return true;
        }

        public int components() {
            return components;
        }
    }

    public record MstResult(
            long weight,
            List<Graph.Edge> edges,
            int components
    ) {}

    public static MstResult kruskal(Graph g, Metrics m) {
        if (g.isDirected()) {
            throw new IllegalArgumentException(
                    "MST requires an undirected graph"
            );
        }

        List<Graph.Edge> edges = new ArrayList<>(g.edges());

        edges.sort((a, b) -> {
            m.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });

        DSU dsu = new DSU(g.size());
        List<Graph.Edge> chosen = new ArrayList<>();
        long total = 0;

        for (Graph.Edge e : edges) {
            m.edgeScans++;

            int a = dsu.find(e.from());
            int b = dsu.find(e.to());
            m.dsuOps += 2;

            if (a != b) {
                dsu.union(a, b);
                m.dsuOps += 2;
                chosen.add(e);
                total += e.weight();
            }
        }

        return new MstResult(total, chosen, dsu.components());
    }

    public static MstResult prim(Graph g, Metrics m) {
        if (g.isDirected()) {
            throw new IllegalArgumentException(
                    "MST requires an undirected graph"
            );
        }

        int n = g.size();
        boolean[] seen = new boolean[n];
        List<Graph.Edge> chosen = new ArrayList<>();
        long total = 0;
        int components = 0;

        record Candidate(int from, int to, int weight) {}

        PriorityQueue<Candidate> pq = new PriorityQueue<>(
                Comparator.comparingInt(Candidate::weight)
        );

        for (int start = 0; start < n; start++) {
            if (seen[start]) {
                continue;
            }

            components++;
            seen[start] = true;

            for (Graph.Arc a : g.neighbors(start)) {
                m.edgeScans++;
                pq.add(new Candidate(start, a.to(), a.weight()));
            }

            while (!pq.isEmpty()) {
                Candidate c = pq.poll();

                if (seen[c.to()]) {
                    continue;
                }

                seen[c.to()] = true;
                chosen.add(new Graph.Edge(
                        c.from(), c.to(), c.weight()
                ));
                total += c.weight();

                for (Graph.Arc a : g.neighbors(c.to())) {
                    m.edgeScans++;

                    if (!seen[a.to()]) {
                        pq.add(new Candidate(
                                c.to(), a.to(), a.weight()
                        ));
                    }
                }
            }
        }

        return new MstResult(total, chosen, components);
    }

    public static List<List<Integer>> kosaraju(
            Graph g, Metrics m
    ) {
        int n = g.size();
        boolean[] seen = new boolean[n];
        List<Integer> finish = new ArrayList<>(n);

        final class Frame {
            int v;
            int i;

            Frame(int v) {
                this.v = v;
            }
        }

        ArrayDeque<Frame> stack = new ArrayDeque<>();

        for (int s = 0; s < n; s++) {
            if (seen[s]) {
                continue;
            }

            seen[s] = true;
            stack.push(new Frame(s));
            m.maxDepth = Math.max(m.maxDepth, stack.size());

            while (!stack.isEmpty()) {
                Frame f = stack.peek();
                List<Graph.Arc> neighbors = g.neighbors(f.v);

                if (f.i < neighbors.size()) {
                    Graph.Arc a = neighbors.get(f.i++);
                    m.edgeScans++;

                    if (!seen[a.to()]) {
                        seen[a.to()] = true;
                        stack.push(new Frame(a.to()));
                        m.maxDepth = Math.max(
                                m.maxDepth, stack.size()
                        );
                    }
                } else {
                    finish.add(f.v);
                    stack.pop();
                }
            }
        }

        Graph reversed = g.reversed();
        Arrays.fill(seen, false);

        List<List<Integer>> result = new ArrayList<>();
        ArrayDeque<Integer> todo = new ArrayDeque<>();

        for (int i = finish.size() - 1; i >= 0; i--) {
            int s = finish.get(i);

            if (seen[s]) {
                continue;
            }

            List<Integer> component = new ArrayList<>();
            seen[s] = true;
            todo.push(s);

            m.maxDepth = Math.max(m.maxDepth, todo.size());

            while (!todo.isEmpty()) {
                int v = todo.pop();
                component.add(v);

                for (Graph.Arc a : reversed.neighbors(v)) {
                    m.edgeScans++;

                    if (!seen[a.to()]) {
                        seen[a.to()] = true;
                        todo.push(a.to());
                        m.maxDepth = Math.max(
                                m.maxDepth, todo.size()
                        );
                    }
                }
            }

            result.add(component);
        }

        return result;
    }

    public record TopologicalResult(
            List<Integer> order,
            boolean hasCycle
    ) {}

    public static TopologicalResult kahn(Graph g, Metrics m) {
        int n = g.size();
        int[] indegree = g.indegrees();
        ArrayDeque<Integer> queue = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            if (indegree[i] == 0) {
                queue.add(i);
            }
        }

        List<Integer> order = new ArrayList<>(n);

        while (!queue.isEmpty()) {
            int v = queue.remove();
            order.add(v);

            for (Graph.Arc a : g.neighbors(v)) {
                m.edgeScans++;

                if (--indegree[a.to()] == 0) {
                    queue.add(a.to());
                }
            }
        }

        return new TopologicalResult(
                order, order.size() != n
        );
    }

    public static boolean validTopologicalOrder(
            Graph g, List<Integer> order
    ) {
        if (order.size() != g.size()) {
            return false;
        }

        int[] position = new int[g.size()];
        Arrays.fill(position, -1);

        for (int i = 0; i < order.size(); i++) {
            int v = order.get(i);

            if (v < 0 || v >= position.length
                    || position[v] != -1) {
                return false;
            }

            position[v] = i;
        }

        for (Graph.Edge e : g.edges()) {
            if (position[e.from()] >= position[e.to()]) {
                return false;
            }
        }

        return true;
    }

    public record PathResult(
            long[] distance,
            int[] parent,
            long sum
    ) {
        public List<Integer> pathTo(int target) {
            if (distance[target] == Long.MAX_VALUE) {
                return List.of();
            }

            ArrayList<Integer> path = new ArrayList<>();

            for (int v = target; v != -1; v = parent[v]) {
                path.add(v);
            }

            Collections.reverse(path);
            return path;
        }
    }

    public static PathResult dagShortestPaths(
            Graph g, Metrics m
    ) {
        TopologicalResult tr = kahn(g, new Metrics());

        if (tr.hasCycle()) {
            throw new IllegalArgumentException(
                    "DAG shortest paths require an acyclic graph"
            );
        }

        int n = g.size();
        int[] indegree = g.indegrees();
        long[] distance = new long[n];
        int[] parent = new int[n];

        Arrays.fill(distance, Long.MAX_VALUE);
        Arrays.fill(parent, -1);

        for (int i = 0; i < n; i++) {
            if (indegree[i] == 0) {
                distance[i] = 0;
            }
        }

        for (int v : tr.order()) {
            if (distance[v] == Long.MAX_VALUE) {
                continue;
            }

            for (Graph.Arc a : g.neighbors(v)) {
                m.edgeScans++;

                long candidate = distance[v] + a.weight();

                if (candidate < distance[a.to()]) {
                    distance[a.to()] = candidate;
                    parent[a.to()] = v;
                }
            }
        }

        long sum = 0;

        for (long d : distance) {
            if (d != Long.MAX_VALUE) {
                sum += d;
            }
        }

        return new PathResult(distance, parent, sum);
    }
}