import numpy as np
import random
import matplotlib.pyplot as plt


def roll_dice():
    return random.randint(1, 6)


capital = 500
capital_in_time = []
number_of_games = 100000
played_games = 0

result = []

for i in range(number_of_games):

    if capital <= 0:
        break

    green_roll = roll_dice()
    black_roll = roll_dice()

    if green_roll > black_roll:
        result.append(1)
        capital += 1
    else:
        result.append(-1)
        capital -= 1

    capital_in_time.append(capital)
    played_games += 1


theoretical_result = [500 + i * (-1/6) for i in range(len(result))]

bins = [i for i in range(len(capital_in_time))]

plt.plot(bins, capital_in_time, bins, theoretical_result)
plt.xlabel("Numer gry")
plt.ylabel("Kapital")
plt.show()

################################

prob_winning = 0.417
prob_losing = 0.583

E = 0

for i in result:
    if i == 1:
        E += prob_winning
    else:
        E += -prob_losing

print("Wartosc oczekiwana: " + str(E))

avg = sum(result) / len(result)
print("Srednia wygranej: " + str(avg))

summary = 0

for i in range(len(result)):
    summary += avg

print("Suma średniej wygranej: " + str(summary))

###############################
