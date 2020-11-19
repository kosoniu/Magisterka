import random
import math
import matplotlib.pyplot as plt
import numpy as np


def gaussian_distribution(x, mu, sigma):
    return (1 / (np.sqrt(2 * np.pi) * sigma)) * np.exp(-((x - mu) ** 2) / 2 * sigma)


def generate_random_numbers():
    while True:
        x = random.uniform(-1, 1)
        y = random.uniform(-1, 1)
        s = x * x + y * y

        if s < 1:
            return math.sqrt(-2 * math.log(s)) * (y / math.sqrt(s))


np.random.seed(19680801)

mu = 0
sigma = 1
y = mu + sigma * np.random.randn(50000)

x = []
for i in range(50000):
    x.append(mu + sigma * generate_random_numbers())

num_bins = 100

fig, ax = plt.subplots()

# the histogram of the data
n, bins, patches = ax.hist(x, num_bins, density=True, alpha=1, label='own implementation')
ax.hist(y, num_bins, density=True, alpha=0.5, label='library')
ax.plot(bins, gaussian_distribution(bins, mu, sigma), '--')
ax.legend(loc='upper right')
fig.tight_layout()
plt.show()