import numpy as np
import random


def roll_dice():
    return random.randint(1, 6)


def play(throws_number):
    result = []

    for i in range(throws_number):
        green_roll = roll_dice()
        black_roll = roll_dice()

        if green_roll > black_roll:
            result.append(1)
        else:
            result.append(-1)

    return result


########################
P = []
tries = 10000
possibilities = 36

for i in range(0, tries):
    one_game = play(possibilities)
    wins = 0

    for index, value in enumerate(one_game):
        if value == 1:
            wins += 1

    P.append(wins/possibilities)

print("Eksperymentalna wartosc prawdopodobienstwa dla jednej gry wynosi: " + str(sum(P) / tries))

