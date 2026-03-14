import dimod
from dimod import SampleSet
from typing import List


def qubo_matrix_to_bqm(qubo_matrix: List[List[float]]) -> dimod.BinaryQuadraticModel:
    qubo_coeffs = {(i, j): value for i, row in enumerate(qubo_matrix) for j, value in enumerate(row)}
    return dimod.BinaryQuadraticModel.from_qubo(qubo_coeffs)

def parse_result(result: SampleSet):
    samples = ["".join(str(value) for value in item.sample.values()) for item in result.data(["sample"], sorted_by = None)]
    energy_states = [float(item.energy) for item in result.data(["energy"], sorted_by = None)]
    lowest_energy_state = min(energy_states)

    occurrences = result.data_vectors["num_occurrences"]
    energy_state_counts = dict()
    for index, state in enumerate(energy_states):
        state_key = str(state)
        if state_key in energy_state_counts.keys():
            energy_state_counts[state_key] += occurrences[index]
        else:
            energy_state_counts[state_key] = occurrences[index]

    dict_samples = dict(zip(samples, energy_states))
    sorted_samples = dict(sorted(dict_samples.items(), key = lambda key_val: key_val[1]))

    return sorted_samples, energy_state_counts, lowest_energy_state
