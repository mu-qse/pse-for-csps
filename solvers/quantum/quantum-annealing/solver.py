import os
import sys
from dwave.samplers import SimulatedAnnealingSampler, PathIntegralAnnealingSampler
from dwave.system.composites import EmbeddingComposite
from dwave.system.samplers import DWaveSampler
from typing import List

import io_utils
import quantum_annealing

num_reads: int = 5000

def solve(qubo_matrix: List[List[float]], output_path: str) -> List[List[int]]:
    model = quantum_annealing.qubo_matrix_to_bqm(qubo_matrix)
    print("n: {}".format(model.num_variables))

    solver = PathIntegralAnnealingSampler() #SimulatedAnnealingSampler()
    #solver = EmbeddingComposite(DWaveSampler())
    result = solver.sample(model, num_reads = num_reads)
    samples, energy_state_counts, lowest_energy_state = quantum_annealing.parse_result(result)
    print("Lowest energy state:", lowest_energy_state)
    #plt_utils.plot_samples(samples, os.path.join(output_path, "samples.svg"))
    #plt_utils.plot_energy_state_counts(energy_state_counts, os.path.join(output_path, "states_count.svg"))

    samples_top10 = dict(sorted(samples.items(), key = lambda key_val: key_val[1])[:10])
    print(samples_top10)
    #plt_utils.plot_samples(samples_top10, os.path.join(output_path, "samples_top10.svg"))

    bitstring = next(iter(samples))
    bits = [int(ch) for ch in bitstring] # [0 for _ in range(n)]
    return [ bits ]

if __name__ == "__main__":
    qubo_matrix_path = sys.argv[1]
    qubo_matrix = io_utils.read_from_file(qubo_matrix_path)
    output_path = sys.argv[3] if len(sys.argv) > 3 else os.getcwd()
    solutions = solve(qubo_matrix, output_path)
    print(solutions)

    solutions_path = sys.argv[2] if len(sys.argv) > 2 else None
    if solutions_path is not None:
        io_utils.write_to_file(solutions, solutions_path)
