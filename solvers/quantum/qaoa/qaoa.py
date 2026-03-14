import datetime
import numpy as np
from qiskit.circuit.library import QAOAAnsatz
from qiskit.quantum_info import SparsePauliOp
from qiskit.transpiler.preset_passmanagers import generate_preset_pass_manager
from qiskit_aer import AerSimulator
from qiskit_aer.primitives import EstimatorV2, SamplerV2
from qiskit_optimization import QuadraticProgram
from qiskit_optimization.algorithms import (
    MinimumEigenOptimizer,
    OptimizationResultStatus,
)
from qiskit_optimization.algorithms import SolutionSample
from qiskit_optimization.converters import QuadraticProgramToQubo
from qiskit_optimization.minimum_eigensolvers import QAOA
from qiskit_optimization.optimizers import COBYLA
from scipy.optimize import minimize
from typing import List

seed: int = 1304
np.random.seed(seed = seed)

device = AerSimulator()

def infer_cost_hamiltonian_qubo(qubo_matrix: List[List[float]]) -> tuple[QuadraticProgram, SparsePauliOp, float]:
    # Create quadratic program instance and convert to ising
    qp = QuadraticProgram()
    length = len(qubo_matrix)
    for _ in range(length):
        qp.binary_var()

    linear_coeffs = dict()
    quadratic_coeffs = dict()
    for i, row in enumerate(qubo_matrix):
        for j, value in enumerate(row):
            if i == j:
                linear_coeffs[i] = value
            elif j > i:
                quadratic_coeffs[i, j] = value
    qp.minimize(linear = linear_coeffs, quadratic = quadratic_coeffs)
    ising, offset = qp.to_ising()

    # Convert to qubo
    #qubo = QuadraticProgramToQubo().convert(qp)
    #ising, offset = qubo.to_ising()

    return qp, ising, offset

def build_circuit(cost_operator: SparsePauliOp, layers: int):
    circuit = QAOAAnsatz(cost_operator = cost_operator, reps = layers, flatten = True)
    circuit.measure_all()
    return circuit

def optimize_circuit(
        cost_operator: SparsePauliOp,
        circuit: QAOAAnsatz,
        layers: int,
        initial_params: List[float] = None,
):
    if initial_params is None:
        initial_gamma = 1 # np.pi
        initial_beta = 1 # np.pi / 2
        initial_params = [initial_beta, initial_gamma] * layers
        # initial_params = np.random.uniform(-np.pi / 8, np.pi / 8, circuit.num_parameters) # [np.pi, np.pi / 2]

    history = []
    isa_hamiltonian = cost_operator.apply_layout(circuit.layout)
    last_timestamp = datetime.datetime.now()
    elapsed_time_history = []

    max_iterations = 200

    estimator = EstimatorV2.from_backend(device)
    pass_manager = generate_preset_pass_manager(optimization_level = 2, backend = device)
    isa_circuit = pass_manager.run(circuit)

    def cost_estimator_func(params: np.ndarray[float]) -> np.ndarray[float]:
        nonlocal history
        nonlocal isa_hamiltonian
        nonlocal last_timestamp

        i = len(history)
        pub = (isa_circuit, isa_hamiltonian, params)
        job = estimator.run([ pub ])
        iteration_result = job.result()[0]
        cost: np.ndarray[float] = iteration_result.data.evs
        history.append((params.tolist(), cost.max()))

        current_timestamp = datetime.datetime.now()
        elapsed_time = current_timestamp - last_timestamp
        elapsed_time_history.append(elapsed_time)
        average_elapsed_time = datetime.timedelta(seconds = sum([time.total_seconds() for time in elapsed_time_history]) / len(elapsed_time_history))
        remaining_time = average_elapsed_time * (max_iterations - i)

        if i % 10 == 0:
            print("{}/{}: {} -> {:.5f} | {} remaining".format(i + 1, max_iterations, [float("{:.8f}".format(p)) for p in params], cost, str(remaining_time)[:-7]), flush = True)

        last_timestamp = datetime.datetime.now()
        return cost

    optimization_result = minimize(
        cost_estimator_func,
        np.array(initial_params),
        method = "COBYLA",
        tol = 1e-9,
        options = {
            "disp": True,
            "maxiter": max_iterations,
            "rhobeg": 0.2 , # np.pi / 8,
        }
    )

    return optimization_result.x, history

def run_circuit(circuit: QAOAAnsatz, shots: int):
    sampler = SamplerV2.from_backend(device, seed = 1304)
    job = sampler.run([ circuit ], shots = shots)
    result = job.result()[0]
    state_counts = result.data.meas.get_counts()
    state_counts_sorted = dict(sorted(state_counts.items(), key = lambda key_val: key_val[1], reverse = True))
    return state_counts_sorted

def sample(
        qubo: QuadraticProgram,
        shots: int,
        layers: int = 1,
        initial_params: List[float] = None,
):
    if initial_params is None:
        initial_gamma = 1
        initial_beta = 1
        initial_params = [initial_beta, initial_gamma] * layers
        # initial_params = np.random.uniform(-np.pi / 8, np.pi / 8, layers * 2)

    sampler = SamplerV2.from_backend(device, seed = 1304, default_shots = shots)
    pass_manager = generate_preset_pass_manager(optimization_level = 2, backend = device)
    qaoa = QAOA(
        sampler = sampler,
        pass_manager = pass_manager,
        reps = layers,
        initial_point = initial_params,
        optimizer = COBYLA(
            disp = True,
            maxiter = 50,
            rhobeg = 0.2,
            tol = 1e-9,
        ),
    )

    optimizer = MinimumEigenOptimizer(qaoa)
    result = optimizer.solve(qubo)
    samples = [sample for sample in result.samples if sample.status == OptimizationResultStatus.SUCCESS and sum(sample.x) > 0]
    samples.sort(key = lambda sample: sample.probability, reverse = True)
    cost_value = result.fval

    def get_bitstring(sample: SolutionSample) -> str:
        bits = [int(value) for value in sample.x]
        return "".join(str(x) for x in bits)

    probabilities = { get_bitstring(sample): sample.probability for sample in samples }
    probabilities_sorted = dict(sorted(probabilities.items(), key = lambda key_val: key_val[1], reverse = True))

    costs = { get_bitstring(sample): float(sample.fval) for sample in samples }
    best_states = [get_bitstring(sample) for sample in samples if sample.fval == cost_value]

    return probabilities_sorted, costs, cost_value, best_states
