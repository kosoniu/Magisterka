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
    int myRank, numberOfProcesses;

    mmpi->MPI_Comm_rank(MPI_COMM_WORLD, &myRank);
    mmpi->MPI_Comm_size(MPI_COMM_WORLD, &numberOfProcesses);

    mmpi->MPI_Bcast(&size, sizeof(int), MPI_INT, 0, MPI_COMM_WORLD);

    if(myRank != 0) {
        data = new double[size * size];
    }

    mmpi->MPI_Bcast(data, size * size, MPI_DOUBLE, 0, MPI_COMM_WORLD);
    mmpi->MPI_Bcast(&this->dataToChange, sizeof(int), MPI_INT, 0, MPI_COMM_WORLD);


    rows = new int[dataToChange];
    cols = new int[dataToChange];
    delta = new double[dataToChange];
}

void Simulation::calcInitialTotalEnergy()
{
    int myRank;
    mmpi->MPI_Comm_rank(MPI_COMM_WORLD, &myRank);

    double localInitialEnergy = calcTotalEnergy();
    mmpi->MPI_Reduce(&localInitialEnergy, &Etot, 1, MPI_DOUBLE, MPI_SUM, 0, MPI_COMM_WORLD);
}

double Simulation::calcTotalEnergy()
{
    int numberOfProcesses, rank, numberOfRows, startRow, endRow;

    mmpi->MPI_Comm_size(MPI_COMM_WORLD, &numberOfProcesses);
    mmpi->MPI_Comm_rank(MPI_COMM_WORLD, &rank);

    double Etot = 0.0;

    numberOfRows = getNumberOfRows(numberOfProcesses, rank);

    startRow = 0;

    for (int i = 0; i < rank; i++)
        startRow += getNumberOfRows(numberOfProcesses, i);

    endRow = startRow + numberOfRows;

    if (rank == 0)
        startRow = 2;

    if (rank == numberOfProcesses - 1)
        endRow -= 2;

    for (int i = startRow; i < endRow; i++)
        for (int col = 2; col < size - 2; col++)
            Etot += energyCalculator->calc(data, size, i, col);

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

int Simulation::getNumberOfRows(int numberOfProcesses, int rank)
{
    int differNumberOfRows = this->size % numberOfProcesses;
    int numberOfRows = this->size / numberOfProcesses;
    int difference = numberOfProcesses - differNumberOfRows;

    if (differNumberOfRows == 0){
        return numberOfRows;
    }

    return rank >= difference ? numberOfRows + 1 : numberOfRows;
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
    int myRank, numberOfProcesses;
    MPI_Status status;

    mmpi->MPI_Comm_rank(MPI_COMM_WORLD, &myRank);
    mmpi->MPI_Comm_size(MPI_COMM_WORLD, &numberOfProcesses);

    if(myRank == 0) {
        generateDataChange();
        for(int i = 1; i < numberOfProcesses; ++i) {
            mmpi->MPI_Send(rows, dataToChange, MPI_INT, i, i, MPI_COMM_WORLD);
            mmpi->MPI_Send(cols, dataToChange, MPI_INT, i, i, MPI_COMM_WORLD);
            mmpi->MPI_Send(delta, dataToChange, MPI_DOUBLE, i, i, MPI_COMM_WORLD);
        }
    } else {
        mmpi->MPI_Recv(rows, dataToChange, MPI_INT, 0, myRank, MPI_COMM_WORLD, &status);
        mmpi->MPI_Recv(cols, dataToChange, MPI_INT, 0, myRank, MPI_COMM_WORLD, &status);
        mmpi->MPI_Recv(delta, dataToChange, MPI_DOUBLE, 0, myRank, MPI_COMM_WORLD, &status);
    }

    changeData();

    double localInitialEnergy = calcTotalEnergy();
    double newEtot = 0.0;
    mmpi->MPI_Reduce(&localInitialEnergy, &newEtot, 1, MPI_DOUBLE, MPI_SUM, 0, MPI_COMM_WORLD);

    bool accepted = false;

    if(myRank == 0) {
        accepted = mc->accept(Etot, newEtot);
        for (int i = 1; i < numberOfProcesses; i++)
            mmpi->MPI_Send(&accepted, 1, MPI_INT, i, i, MPI_COMM_WORLD);

        if (accepted) {
            cout << "Accepted Eold " << Etot << " newE " << newEtot << endl;
            Etot = newEtot;
        } else {
            changeDataUndo();
            cout << "Not accepted Eold " << Etot << " newE " << newEtot << endl;
        }
    } else {
        mmpi->MPI_Recv(&accepted, 1, MPI_INT, 0, myRank, MPI_COMM_WORLD, &status);

        if(!accepted) {
            changeDataUndo();
        }
    }
}