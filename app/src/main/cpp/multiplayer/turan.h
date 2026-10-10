//
// Created by Daler on 20.10.2025.
//
#pragma once

#include <string>

struct Turan
{
    std::string szHost = "84.54.82.234";
    int iPort = 7777;
};

inline Turan GetTuran()
{
    Turan config;
    return config;
}