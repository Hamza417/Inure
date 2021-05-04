/*
 * Copyright (C) 2017 The Android Open Source Project
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

/**
 * Compile-time, zero-cost checking of JNI signatures against their C++ function type.
 * This can trigger compile-time assertions if any of the input is invalid:
 *     (a) The signature specified does not conform to the JNI function descriptor syntax.
 *     (b) The C++ function is itself an invalid JNI function (e.g. missing JNIEnv*, etc).
 *     (c) The descriptor does not match the C++ function (e.g. "()V" will not match jint(jint)).
 *
 * The fundamental macros are as following:
 *   MAKE_JNI_[FAST_|CRITICAL_]NATIVE_METHOD - Create a checked JNINativeMethod{name, sig, func}.
 *   MAKE_JNI_[FAST_|CRITICAL_]NATIVE_METHOD_AUTOSIG - Same as above, but infer the JNI signature.
 *
 * Usage examples:
 *     // path/to/package/KlassName.java
 *     class KlassName {
 *         native jobject normal(int x);
 *         @FastNative native jobject fast(int x);
 *         @CriticalNative native int critical(long ptr);
 *     }
 *     // path_to_package_KlassName.cpp
 *     jobject KlassName_normal(JNIEnv*,jobject,jint) {...}
 *     jobject KlassName_fast(JNIEnv*,jobject,jint) {...}
 *     jint KlassName_critical(jlong) {...}
 *
 *     // Manually specify each signature:
 *     JNINativeMethod[] gMethods = {
 *         MAKE_JNI_NATIVE_METHOD("normal", "(I)Ljava/lang/Object;", KlassName_normal),
 *         MAKE_JNI_FAST_NATIVE_METHOD("fast", "(I)Ljava/lang/Object;", KlassName_fast),
 *         MAKE_JNI_CRITICAL_NATIVE_METHOD("critical", "(Z)I", KlassName_critical),
 *     };
 *
 *     // Automatically infer the signature:
 *     JNINativeMethod[] gMethodsAutomaticSignature = {
 *         MAKE_JNI_NATIVE_METHOD_AUTOSIG("normal", KlassName_normal),
 *         MAKE_JNI_FAST_NATIVE_METHOD_AUTOSIG("fast", KlassName_fast),
 *         MAKE_JNI_CRITICAL_NATIVE_METHOD_AUTOSIG("critical", KlassName_critical),
 *     };
 *
 *     // and then call JNIEnv::RegisterNatives with gMethods as usual.
 *
 * For convenience the following macros are defined:
 *   [FAST_|CRITICAL_]NATIVE_METHOD - Return JNINativeMethod for class, func name, and signature.
 *   OVERLOADED_[FAST_|CRITICAL_]NATIVE_METHOD - Same as above but allows a separate func identifier.
 *   [FAST_|CRITICAL_]NATIVE_METHOD_AUTOSIG - Return JNINativeMethod, sig inferred from function.
 *
 * The FAST_ prefix corresponds to functions annotated with @FastNative,
 * and the CRITICAL_ prefix corresponds to functions annotated with @CriticalNative.
 * See dalvik.annotation.optimization.CriticalNative for more details.
 *
 * =======================================
 * Checking rules
 * =======================================
 *
 * ---------------------------------------
 * JNI descriptor syntax for functions
 *
 * Refer to "Chapter 3: JNI Types and Data Structures" of the JNI specification
 * under the subsection "Type Signatures" table entry "method type".
 *
 * JNI signatures not conforming to the above syntax are rejected.
 * ---------------------------------------
 * C++ function types
 *
 * A normal or @FastNative JNI function type must be of the form
 *
 *     ReturnType (JNIEnv*, jclass|jobject, [ArgTypes...]) {}
 *
 * A @CriticalNative JNI function type:
 *
 *   must be of the form...  ReturnType ([ArgTypes...]){}
 *   and must not contain any Reference Types.
 *
 * Refer to "Chapter 3: JNI Types and Data Structures" of the JNI specification
 * under the subsection "Primitive Types" and "Reference Types" for the list
 * of valid argument/return types.
 *
 * C++ function types not conforming to the above requirements are rejected.
 * ---------------------------------------
 * Matching of C++ function type against JNI function descriptor.
 *
 * Assuming all of the above conditions are met for signature and C++ type validity,
 * then matching between the signature and the type validity can occur:
 *
 * Given a signature (Args...)Ret and the
 *     C++ function type of the form "CRet fn(JNIEnv*, jclass|jobject, CArgs...)",
 *     or for @CriticalNative of the form "CRet fn(CArgs...)"
 *
 * The number of Args... and the number of CArgs... must be equal.
 *
 * If so, attempt to match every component from the signature and function type
 * against each other:
 *
 * ReturnType:
 *     V <-> void
 *     ArgumentType
 *
 * ArgumentType:
 *     PrimitiveType
 *     ReferenceType  [except for @CriticalNative]
 *
 * PrimitiveType:
 *     Z <-> jboolean
 *     B <-> jbyte
 *     C <-> jchar
 *     S <-> jshort
 *     I <-> jint
 *     J <-> jlong
 *     F <-> jfloat
 *     D <-> jdouble
 *
 * ReferenceType:
 *     Ljava/lang/String;    <-> jstring
 *     Ljava/lang/Class;     <-> jclass
 *     L*;                   <-  jobject
 *     Ljava/lang/Throwable;  -> jthrowable
 *     L*;                   <-  jthrowable
 *     [ PrimitiveType       <-> ${CPrimitiveType}Array
 *     [ ReferenceType       <-> jobjectArray
 *     [*                    <-  jarray
 *
 * Wherein <-> represents a strong match (if the left or right pattern occurs,
 * then left must match right, otherwise matching fails). <- and -> represent
 * weak matches (that is, other match rules can be still attempted).
 *
 * Sidenote: Whilst a jobject could also represent a jclass, jstring, etc,
 * the stricter approach is taken: the most exact C++ type must be used.
 */

#ifndef NATIVEHELPER_JNI_MACROS_H
#define NATIVEHELPER_JNI_MACROS_H

// The below basic macros do not perform automatic stringification,
// invoked e.g. as MAKE_JNI_NATIVE_METHOD("some_name", "()V", void_fn)

// An expression that evaluates to JNINativeMethod { name, signature, function },
//   and applies the above compile-time checking for signature+function.
// The equivalent Java Language code must not be annotated with @FastNative/@CriticalNative.
#define MAKE_JNI_NATIVE_METHOD(name, signature, function)                      \
  _NATIVEHELPER_JNI_MAKE_METHOD(kNormalNative, name, signature, function)

// An expression that evaluates to JNINativeMethod { name, signature, function },
//   and applies the above compile-time checking for signature+function.
// The equivalent Java Language code must be annotated with @FastNative.
#define MAKE_JNI_FAST_NATIVE_METHOD(name, signature, function)                 \
  _NATIVEHELPER_JNI_MAKE_METHOD(kFastNative, name, signature, function)

// An expression that evaluates to JNINativeMethod { name, signature, function },
//   and applies the above compile-time checking for signature+function.
// The equivalent Java Language code must be annotated with @CriticalNative.
#define MAKE_JNI_CRITICAL_NATIVE_METHOD(name, signature, function)             \
  _NATIVEHELPER_JNI_MAKE_METHOD(kCriticalNative, name, signature, function)

// Automatically signature-inferencing macros are also available,
// which also checks the C++ function types for validity:

// An expression that evalutes to JNINativeMethod { name, infersig(function), function) }
// by inferring the signature at compile-time. Only works when the C++ function type
// corresponds to one unambigous JNI parameter (e.g. 'jintArray' -> '[I' but 'jobject' -> ???).
//
// The equivalent Java Language code must not be annotated with @FastNative/@CriticalNative.
#define MAKE_JNI_NATIVE_METHOD_AUTOSIG(name, function)                         \
  _NATIVEHELPER_JNI_MAKE_METHOD_AUTOSIG(kNormalNative, name, function)

// An expression that evalutes to JNINativeMethod { name, infersig(function), function) }
// by inferring the signature at compile-time. Only works when the C++ function type
// corresponds to one unambigous JNI parameter (e.g. 'jintArray' -> '[I' but 'jobject' -> ???).
//
// The equivalent Java Language code must be annotated with @FastNative.
#define MAKE_JNI_FAST_NATIVE_METHOD_AUTOSIG(name, function)                    \
  _NATIVEHELPER_JNI_MAKE_METHOD_AUTOSIG(kFastNative, name, function)

// An expression that evalutes to JNINativeMethod { name, infersig(function), function) }
// by inferring the signature at compile-time.
//
// The equivalent Java Language code must be annotated with @CriticalNative.
#define MAKE_JNI_CRITICAL_NATIVE_METHOD_AUTOSIG(name, function)                 \
  _NATIVEHELPER_JNI_MAKE_METHOD_AUTOSIG(kCriticalNative, name, function)

// Convenience macros when the functions follow the naming convention:
//       .java file           .cpp file
//       JavaLanguageName <-> ${ClassName}_${JavaLanguageName}
//
// Stringification is done automatically, invoked as:
//   NATIVE_[FAST_|CRITICAL]_METHOD(ClassName, JavaLanguageName, Signature)
//
// Intended to construct a JNINativeMethod.
//   (Assumes the C name is the ClassName_JavaMethodName).
//
// The Java Language code must be annotated with one of (none,@FastNative,@CriticalNative)
// for the (none,FAST_,CRITICAL_) variants of these macros.

#ifdef NATIVE_METHOD  // Remove definition from JniConstants.h
#undef NATIVE_METHOD
#endif

#define NATIVE_METHOD(className, functionName, signature)                \
  MAKE_JNI_NATIVE_METHOD(#functionName, signature, className ## _ ## functionName)

#define OVERLOADED_NATIVE_METHOD(className, functionName, signature, identifier) \
  MAKE_JNI_NATIVE_METHOD(#functionName, signature, className ## _ ## identifier)

#define NATIVE_METHOD_AUTOSIG(className, functionName) \
  MAKE_JNI_NATIVE_METHOD_AUTOSIG(#functionName, className ## _ ## functionName)

#define FAST_NATIVE_METHOD(className, functionName, signature)           \
  MAKE_JNI_FAST_NATIVE_METHOD(#functionName, signature, className ## _ ## functionName)

#define OVERLOADED_FAST_NATIVE_METHOD(className, functionName, signature, identifier) \
  MAKE_JNI_FAST_NATIVE_METHOD(#functionName, signature, className ## _ ## identifier)

#define FAST_NATIVE_METHOD_AUTOSIG(className, functionName) \
  MAKE_JNI_FAST_NATIVE_METHOD_AUTOSIG(#functionName, className ## _ ## functionName)

#define CRITICAL_NATIVE_METHOD(className, functionName, signature)           \
  MAKE_JNI_CRITICAL_NATIVE_METHOD(#functionName, signature, className ## _ ## functionName)

#define OVERLOADED_CRITICAL_NATIVE_METHOD(className, functionName, signature, identifier) \
  MAKE_JNI_CRITICAL_NATIVE_METHOD(#functionName, signature, className ## _ ## identifier)

#define CRITICAL_NATIVE_METHOD_AUTOSIG(className, functionName) \
  MAKE_JNI_CRITICAL_NATIVE_METHOD_AUTOSIG(#functionName, className ## _ ## functionName)

////////////////////////////////////////////////////////
//                IMPLEMENTATION ONLY.
//                DO NOT USE DIRECTLY.
////////////////////////////////////////////////////////

#if defined(__cplusplus) && __cplusplus >= 201402L
#include "nativehelper/detail/signature_checker.h"  // for MAKE_CHECKED_JNI_NATIVE_METHOD
#endif

// Expands to an expression whose type is JNINativeMethod.
// This is for older versions of C++ or C, so it has no compile-time checking.
#define _NATIVEHELPER_JNI_MAKE_METHOD_OLD(kind, name, sig, fn)     \
  (                                                                \
    (JNINativeMethod) {                                            \
        (name),                                                    \
        (sig),                                                     \
        _NATIVEHELPER_JNI_MACRO_CAST(reinterpret_cast, void *)(fn) \
    }                                                             \
  )

// C++14 or better, use compile-time checking.
#if defined(__cplusplus) && __cplusplus >= 201402L
// Expands to a compound expression whose type is JNINativeMethod.
#define _NATIVEHELPER_JNI_MAKE_METHOD(kind, name, sig, fn) \
  MAKE_CHECKED_JNI_NATIVE_METHOD(kind, name, sig, fn)

// Expands to a compound expression whose type is JNINativeMethod.
#define _NATIVEHELPER_JNI_MAKE_METHOD_AUTOSIG(kind, name, function) \
  MAKE_INFERRED_JNI_NATIVE_METHOD(kind, name, function)

#else
// Older versions of C++ or C code get the regular macro that's unchecked.
// Expands to a compound expression whose type is JNINativeMethod.
#define _NATIVEHELPER_JNI_MAKE_METHOD(kind, name, sig, fn)         \
  _NATIVEHELPER_JNI_MAKE_METHOD_OLD(kind, name, sig, fn)

// Need C++14 or newer to use the AUTOSIG macros.
#define _NATIVEHELPER_JNI_MAKE_METHOD_AUTOSIG(kind, name, function) \
  static_assert(false, "Cannot infer JNI signatures prior to C++14 for function " #function);

#endif  // C++14 check

// C-style cast for C, C++-style cast for C++ to avoid warnings/errors.
#if defined(__cplusplus)
#define _NATIVEHELPER_JNI_MACRO_CAST(which_cast, to) \
    which_cast<to>
#else
#define _NATIVEHELPER_JNI_MACRO_CAST(which_cast, to) \
    (to)
#endif

#endif  // NATIVEHELPER_JNI_MACROS_H
