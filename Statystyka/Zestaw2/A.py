import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

P = np.array([[0.64, 0.32, 0.04], [0.4, 0.5, 0.1], [0.25, 0.5, 0.25]])
# P = np.array([[0.2, 0.7, 0.1], [0.9, 0.0, 0.1], [0.2, 0.8, 0.0]])
state = np.array([[1.0, 0.0, 0.0]])
state_hist = state
hist = pd.DataFrame(state)

result = P
counter = 0

while True:
    prev = result
    result = result.dot(P)
    state = np.dot(state, P)
    state_hist = np.append(state_hist,state,axis=0)
    hist = pd.DataFrame(state_hist)

    if np.abs(result[0, 0] - prev[0, 0]) < (10 ** -9):
        break


print(result)
print(state)
hist.plot()
plt.show()

