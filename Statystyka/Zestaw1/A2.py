import matplotlib.pyplot as plt
from scipy.stats import cauchy
import numpy as np

def quantileFunction(x, gamma, y0):
    return 1 / (np.pi * gamma * (1 + (((x - y0) / gamma) ** 2)))


def generate_random_numbers(gamma, y0, x):
    return (gamma * np.tan(np.pi * (x - 0.5))) + y0


def avg(values):
    result = 0

    for i in values:
        result += i

    return result / len(values)


def variance(avg, values):
    result = 0

    for i in values:
        result += (i - avg) ** 2

    return result / len(values)

np.random.seed(19680801)

y0 = 0.0
gamma = 1.0

x = np.random.uniform(size=10000)
y = []

y = [generate_random_numbers(gamma, y0, i) for i in x]
y2 = y0 + gamma * cauchy.rvs(size=10000)

avg = avg(y)

print("Srednia wynosi: " + str(avg))
print("Wariancja wynosi: " + str(variance(avg, y)))

num_bins = np.linspace(start=-20, stop=20, num=100)

fig, ax = plt.subplots()

# the histogram of the data
n, bins, patches = ax.hist(y, num_bins, density=True, alpha=1, label='own implementation')
ax.hist(y2, num_bins, density=True, alpha=0.5, label='library')
ax.plot(bins, quantileFunction(bins, gamma, y0), linewidth=1, color='red')
ax.legend(loc='upper right')
fig.tight_layout()
plt.show()