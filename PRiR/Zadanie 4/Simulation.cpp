#include "Simulation.h"
#include "Helper.h"
#include <stdlib.h>
#include <iostream>
#include <math.h>
#include <iomanip>
#include <omp.h>

using namespace std;

Simulation::Simulation() {
}

void Simulation::setRandomNumberGenerator(RandomNumberGenerator *randomNumberGenerator) {
    this->rng = randomNumberGenerator;
}

void Simulation::setEnergyCalculator(EnergyCalculator *energyCalculator) {
    this->energyCalculator = energyCalculator;
}

void Simulation::setMonterCarlo(MonteCarlo *mc) {
    this->mc = mc;
}

void Simulation::setMaxChange(double maxChange) {
    this->maxChange = maxChange;
}

void Simulation::setInitialData(double *data, int size) {
    this->data = data;
    this->size = size;
    reducedSize = size - 4;
}

void Simulation::calcInitialTotalEnergy() {
    Etot = this->calcTotalEnergy();
}

// do zrĂłwnoleglenia
double Simulation::calcTotalEnergy() {
    double Etot = 0.0;
#pragma omp parallel for collapse(2)
    for (int row = 2; row < size - 2; row++) {
        for (int col = 2; col < size - 2; col++) {
            Etot += energyCalculator->calc(data, size, row, col);
        }
    }

    return Etot * 0.5;
}

int Simulation::similarNeighbours(int col, int row, int delta, double limit) {
    double middle = Helper::getValue(data, size, row, col);
    int neighbours = 0;
    for (int rowd = -delta; rowd <= delta; rowd++)
        for (int cold = -delta; cold <= delta; cold++) {
            if (cos(Helper::getValue(data, size, row + rowd, col + cold) - middle) > limit)
                neighbours++;
        }

    return neighbours - 1;
}

// do zrĂłwnoleglenia
double Simulation::calcAvgNumberOfSimilarNeighbours(int neighboursDistance, double limit) {
    int sum = 0;
    int neighbours = 0;
    maxNeighbours = 0;
    int neighboursTmp;

#pragma omp parallel private( neighboursTmp )
    {
#pragma omp for collapse(2)
        for (int row = neighboursDistance; row < size - neighboursDistance; row++) {
            for (int col = neighboursDistance; col < size - neighboursDistance; col++) {
                neighboursTmp = similarNeighbours(col, row, neighboursDistance, limit);
                sum += neighboursTmp;
#pragma omp critical
                {
                    if (neighboursTmp > maxNeighbours) {
                        maxNeighbours = neighboursTmp;
                    }
                }

                neighbours++;
            }
        }
    }

    return (double) sum / (double) (neighbours * (neighboursDistance + 1) * 4 * neighboursDistance);
}

double Simulation::getTotalEnergy() {
    return Etot;
}

int Simulation::getMaxNeighbours() {
    return maxNeighbours;
}

void Simulation::setDataToChangeInSingleStep(int dataToChange) {
    this->dataToChange = dataToChange;
    rows = new int[dataToChange];
    cols = new int[dataToChange];
    delta = new double[dataToChange];
}

// do zrĂłwnoleglenia - konieczna wymiara generatora liczb losowych na drand48_r
void Simulation::generateDataChange() {
    struct drand48_data drand_buf;
    int seed;
    double delta, row, col;

#pragma omp parallel private( seed, row, col, delta, drand_buf )
    {
        seed = 1202107158 + omp_get_thread_num() * 1999;
        srand48_r (seed, &drand_buf);

#pragma omp for
        for (int i = 0; i < dataToChange; i++) {
            drand48_r (&drand_buf, &row);
            drand48_r (&drand_buf, &col);
            drand48_r (&drand_buf, &delta);
            rows[i] = 2 + (int)(row * reducedSize);
            cols[i] = 2 + (int)(col * reducedSize);
            this->delta[i] = maxChange * (1.0 - 2.0 * delta);
        }
    }


}

void Simulation::changeData() {
    for (int i = 0; i < dataToChange; i++) {
        Helper::updateValue(data, size, rows[i], cols[i], delta[i]);
    }
}

void Simulation::changeDataUndo() {
    for (int i = 0; i < dataToChange; i++) {
        Helper::updateValue(data, size, rows[i], cols[i], -delta[i]);
    }
}

void Simulation::singleStep() {
    generateDataChange(); // wygenerowanie danych potrzebnych do zmiany stanu
    changeData();         // zmiana danych (stanu)

    // calcTotalEnergy
    double newEtot = calcTotalEnergy(); // wyliczenie nowej energii caĹkowitej

    // decyzja modulu MonteCarlo o akceptacji zmiany
    if (mc->accept(Etot, newEtot)) {
        cout << "Accepted Eold " << Etot << " newE " << newEtot << endl;
        Etot = newEtot;
        // zaakceptowano zmiane -> nowa wartosc energii calkowitej
    } else {
        changeDataUndo();
        cout << "Not accepted Eold " << Etot << " newE " << newEtot << endl;
        // zmiany nie zaakceptowano -> przywracany stary stan, energia bez zmiany
    }
}