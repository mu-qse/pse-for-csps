import math
import os
import sys
from typing import List

shots: int = 10000

import io_utils
import plt_utils
import qaoa

def solve(qubo_matrix: List[List[float]], output_path: str) -> List[List[int]]:
    n = len(qubo_matrix)
    layers = 8 #math.ceil(math.log2(n))
    print("n: {} -> layers: {}".format(n, layers))

    qubo, ising, offset = qaoa.infer_cost_hamiltonian_qubo(qubo_matrix)
    print(qubo.prettyprint())
    print("Ising Hamiltonian: {}".format(ising))
    print("Offset: {}".format(offset))
    circuit = qaoa.build_circuit(ising, layers)
    plt_utils.plot_circuit(circuit, os.path.join(output_path, "circuit.svg"))

    optimized_params, history = qaoa.optimize_circuit(ising, circuit, layers)
    io_utils.write_to_file(history, os.path.join(output_path, "optimization_history.json"))
    plt_utils.plot_optimization_history(history, os.path.join(output_path, "optimization_history.svg"))

    circuit_optimized = circuit.assign_parameters(optimized_params)
    plt_utils.plot_circuit(circuit_optimized, os.path.join(output_path, "circuit_optimized.svg"))

    state_counts = qaoa.run_circuit(circuit_optimized, shots)
    most_frequent_state = next(iter(state_counts))
    print("Most frequent state: {} ({})".format(most_frequent_state, state_counts[most_frequent_state] / shots))
    io_utils.write_to_file(state_counts, os.path.join(output_path, "state_counts.json"))

    state_counts_top10 = dict(list(state_counts.items())[:10])
    plt_utils.plot_state_counts(state_counts_top10, os.path.join(output_path, "state_counts_top10.svg"))

    bits = [ int(ch) for ch in most_frequent_state ]
    return [ bits ]

def sample(qubo_matrix: List[List[float]], output_path: str) -> List[List[int]]:
    layers = 8
    print("Layers: {}".format(layers))

    qubo, ising, offset = qaoa.infer_cost_hamiltonian_qubo(qubo_matrix)
    print(qubo.prettyprint())
    print("Ising Hamiltonian: {}".format(ising))
    print("Offset: {}".format(offset))

    probabilities, costs, cost_value, best_states = qaoa.sample(qubo, shots, layers)
    probabilities_top10 = dict(list(probabilities.items())[:10])
    plt_utils.plot_probabilities(probabilities_top10, os.path.join(output_path, "probabilities_top10.svg"))

    most_frequent_state = next(iter(probabilities))
    most_frequent_state_cost = costs[most_frequent_state]
    print("Most frequent state: {} ({}) -> {}".format(most_frequent_state, probabilities[most_frequent_state], most_frequent_state_cost))
    print("Best cost: {}".format(cost_value))
    print("Best states: {}".format(best_states))
    print("Cost difference: {}".format(most_frequent_state_cost - cost_value))

    #bits = [[ int(ch) for ch in bitstring ] for bitstring in best_states]
    bits = [ int(ch) for ch in most_frequent_state ]
    return [ bits ]

if __name__ == "__main__":
    qubo_matrix_path = sys.argv[1]
    qubo_matrix = io_utils.read_from_file(qubo_matrix_path)
    output_path = sys.argv[3] if len(sys.argv) > 3 else os.getcwd()
    solutions = sample(qubo_matrix, output_path)
    print(solutions)

    solutions_path = sys.argv[2] if len(sys.argv) > 2 else None
    if solutions_path is not None:
        io_utils.write_to_file(solutions, solutions_path)
