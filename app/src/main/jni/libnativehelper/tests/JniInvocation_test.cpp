/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "NativeBridge_test"

#include <nativehelper/JniInvocation.h>
#include <gtest/gtest.h>


#include "string.h"

#if defined(__ANDROID__) && defined(__BIONIC__)
#define HAVE_TEST_STUFF 1
#else
#undef HAVE_TEST_STUFF
#endif

#ifdef HAVE_TEST_STUFF

// PROPERTY_VALUE_MAX.
#include "cutils/properties.h"

#endif

#ifdef HAVE_TEST_STUFF
static const char* kTestNonNull = "libartd.so";
static const char* kTestNonNull2 = "libartd2.so";
static const char* kExpected = "libart.so";
#endif

TEST(JNIInvocation, Debuggable) {
#ifdef HAVE_TEST_STUFF
    auto is_debuggable = []() { return true; };
    auto get_library_system_property = [](char* buffer) -> int {
        strcpy(buffer, kTestNonNull2);
        return sizeof(kTestNonNull2);
    };

    char buffer[PROPERTY_VALUE_MAX];
    const char* result =
        JniInvocation::GetLibrary(NULL, buffer, is_debuggable, get_library_system_property);
    EXPECT_FALSE(result == NULL);
    if (result != NULL) {
        EXPECT_TRUE(strcmp(result, kTestNonNull2) == 0);
        EXPECT_FALSE(strcmp(result, kExpected) == 0);
    }

    result =
        JniInvocation::GetLibrary(kTestNonNull, buffer, is_debuggable, get_library_system_property);
    EXPECT_FALSE(result == NULL);
    if (result != NULL) {
        EXPECT_TRUE(strcmp(result, kTestNonNull) == 0);
        EXPECT_FALSE(strcmp(result, kTestNonNull2) == 0);
    }
#else
    GTEST_LOG_(WARNING) << "Host testing unsupported. Please run target tests.";
#endif
}

TEST(JNIInvocation, NonDebuggable) {
#ifdef HAVE_TEST_STUFF
    auto is_debuggable = []() { return false; };

    char buffer[PROPERTY_VALUE_MAX];
    const char* result = JniInvocation::GetLibrary(NULL, buffer, is_debuggable, nullptr);
    EXPECT_FALSE(result == NULL);
    if (result != NULL) {
        EXPECT_TRUE(strcmp(result, kExpected) == 0);
        EXPECT_FALSE(strcmp(result, kTestNonNull) == 0);
        EXPECT_FALSE(strcmp(result, kTestNonNull2) == 0);
    }

    result = JniInvocation::GetLibrary(kTestNonNull, buffer, is_debuggable, nullptr);
    EXPECT_FALSE(result == NULL);
    if (result != NULL) {
        EXPECT_TRUE(strcmp(result, kExpected) == 0);
        EXPECT_FALSE(strcmp(result, kTestNonNull) == 0);
    }
#else
    GTEST_LOG_(WARNING) << "Host testing unsupported. Please run target tests.";
#endif
}
