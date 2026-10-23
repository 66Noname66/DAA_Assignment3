from collections import defaultdict
import csv
import os

import matplotlib.pyplot as plt

os.makedirs('results/plots', exist_ok=True)
rows = list(csv.DictReader(open('results/results.csv', newline='')))

fig, ax = plt.subplots()
for alg in sorted({r['algorithm'] for r in rows}):
    for density in (4, 20):
        rr = sorted(
            [
                r
                for r in rows
                if r['algorithm'] == alg
                and int(r['m']) == int(r['n']) * density
            ],
            key=lambda x: int(x['n']),
        )
        if rr:
            ax.plot(
                [int(x['n']) for x in rr],
                [
                    max(
                        1,
                        int(x['dsu_ops'])
                        + int(x['edge_scans'])
                        + int(x['comparisons']),
                    )
                    for x in rr
                ],
                marker='o',
                label=f'{alg}, m={density}n',
            )

ax.set(
    xscale='log',
    yscale='log',
    xlabel='Vertices n',
    ylabel='Counted operations',
    title='Operations vs n',
)
ax.legend(fontsize=7)
fig.tight_layout()
fig.savefig('results/plots/operations_vs_n.png', dpi=160)
plt.close(fig)

fig, ax = plt.subplots()
for alg in sorted({r['algorithm'] for r in rows}):
    for density in (4, 20):
        rr = sorted(
            [
                r
                for r in rows
                if r['algorithm'] == alg
                and int(r['m']) == int(r['n']) * density
            ],
            key=lambda x: int(x['n']),
        )
        if rr:
            ax.plot(
                [int(x['n']) for x in rr],
                [int(x['edge_scans']) / int(x['m']) for x in rr],
                marker='o',
                label=f'{alg}, m={density}n',
            )

ax.set(
    xscale='log',
    xlabel='Vertices n',
    ylabel='edgeScans / m',
    title='Scans per edge',
)
ax.legend(fontsize=7)
fig.tight_layout()
fig.savefig('results/plots/scans_per_edge.png', dpi=160)
plt.close(fig)

fig, ax = plt.subplots()
for density in (4, 20):
    rr = sorted(
        [
            r
            for r in rows
            if r['algorithm'] == 'Kruskal'
            and int(r['m']) == int(r['n']) * density
        ],
        key=lambda x: int(x['n']),
    )
    if rr:
        ax.plot(
            [int(x['n']) for x in rr],
            [int(x['edge_scans']) / int(x['m']) for x in rr],
            marker='o',
            label=f'm={density}n',
        )

ax.set(
    xscale='log',
    xlabel='Vertices n',
    ylabel='Kruskal edgeScans / m',
    title='Share of edges examined by Kruskal',
)
ax.legend()
fig.tight_layout()
fig.savefig('results/plots/kruskal_edge_share.png', dpi=160)
plt.close(fig)

print('Wrote plots to results/plots/')