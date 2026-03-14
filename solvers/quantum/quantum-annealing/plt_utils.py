from matplotlib import pyplot as plt
from matplotlib.ticker import MultipleLocator
from typing import List, Any, Dict


def plot_samples(samples: Dict[str, float], path: str):
    fig, ax = plt.subplots(figsize = (6, 3), tight_layout = True)
    plt.xlabel("Solutions")
    plt.xticks(rotation = 90)
    plt.ylabel("Energy state")
    ax.bar(list(samples.keys()), list(samples.values()), color = "tab:grey")
    ax.yaxis.set_major_locator(MultipleLocator(1))
    plt.savefig(
        path,
        bbox_inches = "tight",
        pad_inches = 0,
        transparent = True
    )

def plot_energy_state_counts(energy_state_counts: Dict[str, int], path: str):
    fig, ax = plt.subplots(figsize = (4, 2), tight_layout = True)
    plt.xlabel("Energy state")
    plt.ylabel("Count")
    ax.bar(list(energy_state_counts.keys()), list(energy_state_counts.values()), color = "tab:grey")
    ax.yaxis.set_major_locator(MultipleLocator(200))
    ax.yaxis.set_minor_locator(MultipleLocator(100))
    plt.savefig(
        path,
        bbox_inches = "tight",
        pad_inches = 0,
        #transparent = True
    )
