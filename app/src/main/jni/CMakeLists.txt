# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.4.1)

# Enable reproducible builds
set(CMAKE_REPRODUCIBLE 1)

# Define compile flags for reproducible builds
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -ffile-prefix-map=${CMAKE_SOURCE_DIR}=. -ffile-prefix-map=${ANDROID_HOME}=. -ffile-prefix-map=${CMAKE_TOOLCHAIN_FILE}=. -ffile-prefix-map=${PROJECT_ROOT}=.")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -ffile-prefix-map=${CMAKE_SOURCE_DIR}=. -ffile-prefix-map=${ANDROID_HOME}=. -ffile-prefix-map=${CMAKE_TOOLCHAIN_FILE}=. -ffile-prefix-map=${PROJECT_ROOT}=.")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O2 -g -fdebug-prefix-map=${CMAKE_SOURCE_DIR}=. -fdebug-prefix-map=${ANDROID_HOME}=. -fdebug-prefix-map=${CMAKE_TOOLCHAIN_FILE}=. -fdebug-prefix-map=${PROJECT_ROOT}=.")

# Define linker flags to strip debug symbols
add_link_options(-Wl,--hash-style=gnu,--build-id=none -Wl,--strip-debug)

# Set the archiver and ranlib to use llvm tools
set(CMAKE_AR "llvm-ar")
set(CMAKE_RANLIB "llvm-ranlib")

project(inure)

# Creates the project's shared libraries
add_library(inure_terminal_emulator
        SHARED
        termExec.cpp
        common.cpp
        fileCompat.cpp
        process.cpp)

add_library(inure_su
        SHARED
        root_proc.cpp)

# Find the log library
find_library(log-lib log)

# Link the log library to the target libraries
target_link_libraries(inure_terminal_emulator ${log-lib})
target_link_libraries(inure_su ${log-lib})