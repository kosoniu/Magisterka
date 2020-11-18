#include "Simulation.h"
#include "Helper.h"
#include <stdlib.h>
#include <iostream>
#include <math.h>
#include <iomanip>

using namespace std;

Simulation::Simulation(MyMPI *_mmpi)
{
    mmpi = _mmpi;
}

void Simulation::setRandomNumberGenerator(RandomNumberGenerator *randomNumberGenerator)
{
    this->rng = randomNumberGenerator;
}

void Simulation::setEnergyCalculator(EnergyCalculator *energyCalculator)
{
    this->energyCalculator = energyCalculator;
}

void Simulation::setMonterCarlo(MonteCarlo *mc)
{
    this->mc = mc;
}

void Simulation::setMaxChange(double maxChange)
{
    this->maxChange = maxChange;
}

void Simulation::setInitialData(double *data, int size)
{
    this->data = data;
    this->size = size;
    reducedSize = size - 4;
}

void Simulation::init() {
    int myRank, size;
    int sum = 0;

    mmpi->MPI_Comm_rank(MPI_COMM_WORLD, &myRank);
    mmpi->MPI_Comm_size(MPI_COMM_WORLD, &size);
    this->bufor = new double[100];
    this->sendcounts = new int[size];
    this->displs = new int[size];

    if(myRank == 0) {
        for(int i = 1; i < size; i++)
            mmpi->MPI_Send(&this->size, 1, MPI_INT, i, 0, MPI_COMM_WORLD);

        int rem = this->size%size;

        for (int i = 0; i < size; i++) {
            sendcounts[i] = this->size/size;
            if (rem > 0) {
                sendcounts[i]++;
                rem--;
            }

            displs[i] = sum;
            sum += sendcounts[i];
        }

        for (int i = 0; i < size; i++)
            cout << sendcounts[i] << endl;

        mmpi->MPI_Scatterv(this->data, sendcounts, displs, MPI_DOUBLE, bufor, 100, MPI_DOUBLE, 0 , MPI_COMM_WORLD);
    } else  {
        MPI_Status status;
        mmpi->MPI_Recv(&this->size, 1, MPI_INT, 0, 0, MPI_COMM_WORLD, &status);
    }
}

void Simulation::calcInitialTotalEnergy()
{
    Etot = this->calcTotalEnergy();
}

double Simulation::calcTotalEnergy()
{
    cout << "moja dlugosc: " << size << endl;
    double Etot = 0.0;
    for (int row = 2; row < size - 2; row++)
        for (int col = 2; col < size - 2; col++)
            Etot += energyCalculator->calc(bufor, size, row, col);
    return Etot * 0.5;
}

double Simulation::getTotalEnergy()
{
    return Etot;
}

void Simulation::setDataToChangeInSingleStep(int dataToChange)
{
    this->dataToChange = dataToChange;
    rows = new int[dataToChange];
    cols = new int[dataToChange];
    delta = new double[dataToChange];
}

void Simulation::generateDataChange()
{
    for (int i = 0; i < dataToChange; i++)
    {
        rows[i] = 2 + rng->getInt(reducedSize);
        cols[i] = 2 + rng->getInt(reducedSize);
        delta[i] = maxChange * (1.0 - 2.0 * rng->getDouble());
    }
}

void Simulation::changeData()
{
    for (int i = 0; i < dataToChange; i++)
    {
        Helper::updateValue(data, size, rows[i], cols[i], delta[i]);
    }
}

void Simulation::changeDataUndo()
{
    for (int i = 0; i < dataToChange; i++)
    {
        Helper::updateValue(data, size, rows[i], cols[i], -delta[i]);
    }
}

void Simulation::singleStep()
{
    int myRank;
    mmpi->MPI_Comm_rank(MPI_COMM_WORLD, &myRank);

    if(myRank == 0) {
        generateDataChange(); // wygenerowanie danych potrzebnych do zmiany stanu
        changeData(); // zmiana danych (stanu)
    }

    // calcTotalEnergy
    mmpi->MPI_Scatter(this->data, this->size, MPI_DOUBLE, bufor, this->size, MPI_DOUBLE, 0 , MPI_COMM_WORLD);
    double newEtot = calcTotalEnergy(); // wyliczenie nowej energii caĹkowitej
    mmpi->MPI_Gather(bufor, this->size, MPI_DOUBLE, this->data, this->size, MPI_DOUBLE, 0 , MPI_COMM_WORLD);

    // decyzja modulu MonteCarlo o akceptacji zmiany
    if(myRank == 0) {
        if (mc->accept(Etot, newEtot))
        {
            cout << "Accepted Eold " << Etot << " newE " << newEtot << endl;
            Etot = newEtot;
            // zaakceptowano zmiane -> nowa wartosc energii calkowitej
        }
        else
        {
            changeDataUndo();
            cout << "Not accepted Eold " << Etot << " newE " << newEtot << endl;
            // zmiany nie zaakceptowano -> przywracany stary stan, energia bez zmiany
        }
    }

}