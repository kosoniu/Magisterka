import numpy as np


def markov_chain(state, steps_matrix, iterations):
    steps = [0, 1, 2]

    counter = {
        "0": 0,
        "1": 0,
        "2": 0
    }

    for i in range(iterations):
        if state == 0:
            state = np.random.choice(steps, replace=True, p=steps_matrix[0])
            counter[str(state)] += 1
        elif state == 1:
            state = np.random.choice(steps, replace=True, p=steps_matrix[1])
            counter[str(state)] += 1
        elif state == 2:
            state = np.random.choice(steps, replace=True, p=steps_matrix[2])
            counter[str(state)] += 1

    return counter


# P = np.array([[0.64, 0.32, 0.04], [0.4, 0.5, 0.1], [0.25, 0.5, 0.25]])
P = np.array([[0.2, 0.7, 0.1], [0.9, 0.0, 0.1], [0.2, 0.8, 0.0]])
N = 100000

step = 0
counter = markov_chain(step, P, N)

print("Lancuch Markowa dla stanu poczatkowego: " + str(step))
print(counter["0"] / N)
print(counter["1"] / N)
print(counter["2"] / N)

step = 1
counter = markov_chain(step, P, N)

print("Lancuch Markowa dla stanu poczatkowego: " + str(step))
print(counter["0"] / N)
print(counter["1"] / N)
print(counter["2"] / N)

step = 2
counter = markov_chain(step, P, N)

print("Lancuch Markowa dla stanu poczatkowego: " + str(step))
print(counter["0"] / N)
print(counter["1"] / N)
print(counter["2"] / N)



