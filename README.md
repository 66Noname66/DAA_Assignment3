Design and Analysis of Algorithms — Assignment 3
Graph Algorithms: DSU, MST, SCC and Shortest Paths
Student: Seipenov Damir 
Group: SE-2523
Course: Design and Analysis of Algorithms
Assignment: 3
1. Project Overview
This project studies graph algorithms and compares their behavior on generated graph instances. It covers Disjoint Set Union (DSU), Minimum Spanning Tree (MST), Strongly Connected Components (SCC), and shortest paths in directed acyclic graphs (DAGs).

The project includes Java source code, tests, benchmark output in CSV format, and a Python script that generates plots from the CSV data.
2. Algorithms
DSU: Maintains disjoint sets using find and union.
Kruskal's algorithm: Builds a minimum spanning forest by sorting edges by weight and joining components with DSU.
Prim's algorithm: Builds a minimum spanning forest by repeatedly selecting the lightest available edge.
Kosaraju's algorithm: Finds strongly connected components in a directed graph using two DFS passes.
Kahn's algorithm: Produces a topological ordering of a directed acyclic graph.
DAG shortest paths: Uses topological order to calculate shortest distances in a DAG, including graphs with negative edge weights.
3. Project Structure
DAA_Assignment3/
├── src/
│   ├── main/java/       # Java implementation
│   └── test/java/       # Tests
├── data/                # Example input data, if used
├── results/
│   ├── results.csv      # Benchmark output required by plotting script
│   └── plots/           # Generated PNG charts
├── scripts/
│   └── plot_results.py  # Python plotting script
├── pom.xml              # Maven configuration
└── README.md
The exact structure may differ depending on how the project files were arranged.
4. Requirements
Java JDK 17 or newer
Python 3
Python package matplotlib
Maven, if the project is configured and run with Maven
Install the plotting dependency:
python -m pip install matplotlib
5. Running the Project
Run Java tests
From the project root, if Maven is configured:
mvn test
Run the benchmark
Run the project's benchmark entry point using the method provided in Main.java / Benchmark.java, or use the configured Maven command if one is provided. The benchmark should write its measurements to results/results.csv.

Make sure the CSV includes the columns used by the plotting script:
algorithm,n,m,dsu_ops,edge_scans,comparisons
The exact columns must match the actual benchmark implementation.
Generate plots
From the project root:
python scripts/plot_results.py
On Windows, this may also work:
py scripts/plot_results.py
Successful execution prints:
Wrote plots to results/plots/
6. Generated Charts
1. results/plots/operations_vs_n.png — counted operations versus the number of vertices, using logarithmic axes.
2. results/plots/scans_per_edge.png — edge scans divided by the number of edges.
3. results/plots/kruskal_edge_share.png — the share of edges examined by Kruskal's algorithm.
These charts are based on the data already present in results/results.csv. The Python script does not run the Java algorithms or generate benchmark measurements itself.
7. Testing and Validation
Tests should cover DSU connectivity and union behavior; MST behavior on connected and disconnected undirected graphs; SCC results on directed graphs; topological ordering for DAGs; shortest paths in DAGs, including negative edge weights; and benchmark output format.

Before submission, run the tests and inspect the CSV and all three charts. Confirm that charts contain real data rather than empty axes.
8. Limitations and Notes
Operation counters are only meaningful when they are defined consistently across algorithms. The sum shown in operations_vs_n.png is an aggregate of dsu_ops, edge_scans, and comparisons, not necessarily an exact count of every machine operation.
9. Conclusion
The project implements and evaluates several fundamental graph algorithms. The benchmark CSV and generated charts can be used to compare how measured work changes with graph size and density. Final conclusions should be written after checking the actual benchmark data and confirming that the implementation follows the assignment specification.
