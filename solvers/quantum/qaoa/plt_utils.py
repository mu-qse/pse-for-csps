from matplotlib import pyplot as plt
from matplotlib.ticker import MultipleLocator
from qiskit import QuantumCircuit


def plot_circuit(circuit: QuantumCircuit, path: str):
    circuit_plt = circuit.draw(output = "mpl")
    circuit_plt.tight_layout()
    circuit_plt.savefig(
        path,
        bbox_inches = "tight",
        pad_inches = 0,
        #transparent = True
    )

def plot_optimization_history(history: dict, path: str):
    fig, ax = plt.subplots(figsize = (6, 3), tight_layout = True)
    #plt.title("Cost per Iteration")
    plt.xlabel("Iteration")
    plt.ylabel("Cost")
    ax.plot([item[1] for item in history], color = "tab:grey")
    ax.xaxis.set_major_locator(MultipleLocator(20))
    ax.xaxis.set_major_formatter('{x:.0f}')
    ax.xaxis.set_minor_locator(MultipleLocator(5))
    ax.autoscale(tight = True)
    plt.savefig(
        path,
        bbox_inches = "tight",
        pad_inches = 0,
        #transparent = True,
    )

def plot_state_counts(state_counts: dict[str, int], path: str):
    fig, ax = plt.subplots(figsize = (6, 3), tight_layout = True)
    #plt.title("State Distribution")
    plt.xlabel("State")
    plt.xticks(rotation = 90)
    plt.ylabel("Count")
    ax.bar(list(state_counts.keys()), list(state_counts.values()), color = "tab:grey")
    ax.autoscale(tight = True)
    plt.savefig(
        path,
        bbox_inches = "tight",
        pad_inches = 0,
        #transparent = True
    )

def plot_probabilities(probabilities: dict[str, float], path: str):
    fig, ax = plt.subplots(figsize = (6, 3), tight_layout = True)
    plt.xlabel("State")
    plt.xticks(rotation = 90)
    plt.ylabel("Probability")
    ax.bar(list(probabilities.keys()), list(probabilities.values()), color = "tab:grey")
    ax.autoscale(tight = True)
    plt.savefig(
        path,
        bbox_inches = "tight",
        pad_inches = 0,
        #transparent = True
    )
