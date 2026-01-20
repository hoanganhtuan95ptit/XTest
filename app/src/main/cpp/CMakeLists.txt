cmake_minimum_required(VERSION 3.22.1)

project("notitesting")

add_library(
        notitesting
        SHARED
        native-lib.cpp)

find_library(
        log-lib
        log)

target_link_libraries(
        notitesting
        ${log-lib})
