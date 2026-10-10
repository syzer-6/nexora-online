//
// Created by Daler on 20.10.2025.
//
#pragma once

#include <string>

struct Turan
{
    std::string szHost = "94.136.185.24";
    int iPort = 7776;
};

inline Turan GetTuran()
{
    Turan config;
    return config;
}