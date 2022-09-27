# Sets the minimum version of CMake required to build the native
# library.
cmake_minimum_required(VERSION 3.4.1)

# Creates the project's shared lib: libnative-lib.so.
# The lib is loaded by this project's Java code in MainActivity.java:
#     System.loadLibrary("native-lib");
# The lib name in both places must match.
add_library(inure_terminal_emulator
        SHARED
        termExec.cpp
        common.cpp
        fileCompat.cpp
        process.cpp)

add_library(inure_su
        SHARED
        root_proc.cpp)

find_library(log-lib log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        inure_terminal_emulator

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
