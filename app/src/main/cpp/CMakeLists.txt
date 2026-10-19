cmake_minimum_required(VERSION 3.22)
project("alb_mem")

add_library(alb_mem SHARED mem.cpp)

find_library(log-lib log)
target_link_libraries(alb_mem ${log-lib})
target_compile_options(alb_mem PRIVATE -O3 -fvisibility=hidden)
