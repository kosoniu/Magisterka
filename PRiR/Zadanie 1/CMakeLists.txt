cmake_minimum_required(VERSION 3.17)
project(Magnesy)

set(CMAKE_CXX_STANDARD 14)

add_executable(Magnesy main.cpp EnergyCalculator.cpp EnergyCalculator.h Helper.cpp Helper.h MonteCarlo.cpp MonteCarlo.h MyMPI.cpp MyMPI.h RandomNumberGenerator.cpp RandomNumberGenerator.h Simulation.cpp Simulation.h mpiusage.cpp mpiusage.h sg.cpp test.cpp SimulationS.h SimulationS.cpp)