import random
import threading
import time
import queue
import matplotlib.pyplot as plt
import numpy as np


def worker(elements, tsi, queueSizeList, executionTimes):
    start_time = time.time()

    for item in tsi:
        elements.get(block=True)
        time.sleep(item / 1000)
        queueSizeList.append(elements.qsize())
        executionTimes.append(time.time() - start_time)
        elements.task_done()

def customer(elements, ti, sizeList, addingTimes):
    start_time = time.time()

    for item in ti:
        sizeList.append(elements.qsize())
        time.sleep(item / 1000)
        addingTimes.append(time.time() - start_time)
        elements.put(1)

# Create a queue that we will use to store our "workload".
elements = queue.Queue()
customerQueueSizeList = []
workerQueueSizeList = []
customerResultSizeList = []
workerResultSizeList = []

addingTimes = []
executionTimes = []

lambdaA = 1/20
lambdaS = 1/15

ti = []
tsi = []

for i in range(10):
    n = random.uniform(0, 1)
    ti.append((-np.log(n) / lambdaA))
    tsi.append((-np.log(n) / lambdaS))

print(ti)
print(tsi)

customerThread = threading.Thread(target=customer, args=(elements, ti, customerQueueSizeList, addingTimes))
worker_thread = threading.Thread(target=worker, args=(elements, tsi, workerQueueSizeList, executionTimes))

customerThread.start()
worker_thread.start()

customerThread.join()
worker_thread.join()

sleep_time = sum(ti) + sum(tsi)

time.sleep((sleep_time + .2) / 1000)

# plot
fig, (ax1, ax2) = plt.subplots(2)
# ax1
customerQueueSize = ax1.scatter(addingTimes, customerQueueSizeList)

for x,y in zip(addingTimes, customerQueueSizeList):

    label = f"{y}"

    ax1.annotate(label, # this is the text
                 (x,y), # this is the point to label
                 textcoords="offset points", # how to position the text
                 xytext=(0,5), # distance from text to points (x,y)
                 ha='center')

workerQueueSize = ax1.scatter(executionTimes, workerQueueSizeList)

for x,y in zip(executionTimes, workerQueueSizeList):

    label = f"{y}"

    ax1.annotate(label, # this is the text
                 (x,y), # this is the point to label
                 textcoords="offset points", # how to position the text
                 xytext=(0,5), # distance from text to points (x,y)
                 ha='center')

# beautify the x-labels
# plt.gcf().autofmt_xdate()

ax1.legend((customerQueueSize, workerQueueSize),
           ("Customer", "Worker"),
           scatterpoints=1,
           fontsize=8)

ax1.set_xlabel("Czas")
ax1.set_ylabel("Ilość elementów w kolejce")

# ax2
elements_number = [i for i in range(10)]

result = map(lambda x: x / 1000, tsi)

executionTimesPlot = ax2.scatter(executionTimes, list(result))

ax2.set_xlabel("Czas")
ax2.set_ylabel("Czas wykonania zadania")

plt.tight_layout()
plt.show()
