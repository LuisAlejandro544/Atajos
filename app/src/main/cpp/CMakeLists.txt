cmake_minimum_required(VERSION 3.22.1)

project("nativelua" C CXX)

# Include directories
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/lua)

# Gather all Lua 5.4.7 C sources
file(GLOB LUA_CORE_SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/lua/*.c")

# Compile Lua 5.4.7 core and JNI bridge into a shared library
add_library(
    native-lua
    SHARED
    ${LUA_CORE_SOURCES}
    native-lua.cpp
)

# Find Android log library
find_library(
    log-lib
    log
)

target_link_libraries(
    native-lua
    ${log-lib}
    m
)
