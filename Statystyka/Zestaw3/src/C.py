import random
import threading
import time
import queue
import matplotlib.pyplot as plt
import numpy as np


def worker(elements, tsi, queueSizeList, executionTimes, start_time):
    # start_time = time.time()

    for item in tsi:
        elements.get(block=True)
        time.sleep(item / 1000)
        queueSizeList.append(elements.qsize())
        executionTimes.append(time.time() - start_time)
        elements.task_done()

def customer(elements, ti, sizeList, addingTimes, start_time):
    # start_time = time.time()

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
lambdaS = 1/100

ti = []
tsi = []

for i in range(500):
    n = random.uniform(0, 1)
    ti.append((-np.log(n) / lambdaA))
    tsi.append((-np.log(n) / lambdaS))

print(ti)
print(tsi)

start_time = time.time()

customerThread = threading.Thread(target=customer, args=(elements, ti, customerQueueSizeList, addingTimes, start_time))
worker_thread = threading.Thread(target=worker, args=(elements, tsi, workerQueueSizeList, executionTimes, start_time))

customerThread.start()
worker_thread.start()

customerThread.join()
worker_thread.join()

sleep_time = sum(ti) + sum(tsi)

time.sleep((sleep_time + .2) / 1000)

# plot
customerQueueSize = plt.scatter(addingTimes, customerQueueSizeList)

workerQueueSize = plt.scatter(executionTimes, workerQueueSizeList, alpha=0.7)

plt.legend((customerQueueSize, workerQueueSize),
           ("Customer", "Worker"),
           scatterpoints=1,
           fontsize=8)

plt.xlabel("Czas")
plt.ylabel("Ilość elementów w kolejce")

plt.tight_layout()
plt.show()
