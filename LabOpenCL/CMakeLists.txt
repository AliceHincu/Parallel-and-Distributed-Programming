cmake_minimum_required(VERSION 3.22)
project(LabOpenCL)

set(CMAKE_CXX_STANDARD 14)

include_directories(.)

add_executable(LabOpenCL
        Source.cpp
        Source1.cpp)
