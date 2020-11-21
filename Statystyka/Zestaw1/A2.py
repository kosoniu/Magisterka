import matplotlib.pyplot as plt
from scipy.stats import cauchy
import numpy as np


def generate_random_numbers(gamma, y0, x):
    return (gamma * np.tan(np.pi * (x - 0.5))) + y0


np.random.seed(19680801)

y0 = 0.0
gamma = 1.0

x = np.random.uniform(size=10000)
y = [generate_random_numbers(gamma, y0, i) for i in x]
y2 = y0 + gamma * cauchy.rvs(size=10000)

num_bins = np.linspace(start=-20, stop=20, num=100)

fig, ax = plt.subplots()

# the histogram of the data
n, bins, patches = ax.hist(y, num_bins, density=True, alpha=1, label='own implementation')
ax.hist(y2, num_bins, density=True, alpha=0.5, label='library')
ax.plot(bins, 1 / (np.pi * gamma * (1 + (((bins - y0) / gamma) ** 2))), linewidth=1, color='red')
ax.legend(loc='upper right')
fig.tight_layout()
plt.show()