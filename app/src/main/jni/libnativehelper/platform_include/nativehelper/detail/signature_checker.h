/*
 * Copyright (C) 2018 The Android Open Source Project
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


/*
 * WARNING: Do not include and use these directly. Use jni_macros.h instead!
 * The "detail" namespace should be a strong hint not to depend on the internals,
 * which could change at any time.
 *
 * This implements the underlying mechanism for compile-time JNI signature/ctype checking
 * and inference.
 *
 * This file provides the constexpr basic blocks such as strings, arrays, vectors
 * as well as the JNI-specific parsing functionality.
 *
 * Everything is implemented via generic-style (templates without metaprogramming)
 * wherever possible. Traditional template metaprogramming is used sparingly.
 *
 * Everything in this file except ostream<< is constexpr.
 */

#pragma once

#include <iostream>     // std::ostream
#include <jni.h>        // jni typedefs, JniNativeMethod.
#include <type_traits>  // std::common_type, std::remove_cv

namespace nativehelper {
namespace detail {

// If CHECK evaluates to false then X_ASSERT will halt compilation.
//
// Asserts meant to be used only within constexpr context.
#if defined(JNI_SIGNATURE_CHECKER_DISABLE_ASSERTS)
# define X_ASSERT(CHECK) do { if ((false)) { (CHECK) ? void(0) : void(0); } } while (false)
#else
# define X_ASSERT(CHECK) \
    ( (CHECK) ? void(0) : jni_assertion_failure(#CHECK) )
#endif

// The runtime 'jni_assert' will never get called from a constexpr context;
// instead compilation will abort with a stack trace.
//
// Inspect the frame above this one to see the exact nature of the failure.
inline void jni_assertion_failure(const char* /*msg*/) __attribute__((noreturn));
inline void jni_assertion_failure(const char* /*msg*/) {
  std::terminate();
}

// An immutable constexpr string view, similar to std::string_view but for C++14.
// For a mutable string see instead ConstexprVector<char>.
//
// As it is a read-only view into a string, it is not guaranteed to be zero-terminated.
struct ConstexprStringView {
  // Implicit conversion from string literal:
  //     ConstexprStringView str = "hello_world";
  template<size_t N>
  constexpr ConstexprStringView(const char (& lit)[N])  // NOLINT: explicit.
      : _array(lit), _size(N - 1) {
    // Using an array of characters is not allowed because the inferred size would be wrong.
    // Use the other constructor instead for that.
    X_ASSERT(lit[N - 1] == '\0');
  }

  constexpr ConstexprStringView(const char* ptr, size_t size)
      : _array(ptr), _size(size) {
    // See the below constructor instead.
    X_ASSERT(ptr != nullptr);
  }

  // Implicit conversion from nullptr, creates empty view.
  //   ConstexprStringView str = nullptr;
  explicit constexpr ConstexprStringView(const decltype(nullptr)&)
      : _array(""), _size(0u) {
  }

  // No-arg constructor: Create empty view.
  constexpr ConstexprStringView() : _array(""), _size(0u) {}

  constexpr size_t size() const {
    return _size;
  }

  constexpr bool empty() const {
    return size() == 0u;
  }

  constexpr char operator[](size_t i) const {
    X_ASSERT(i <= size());
    return _array[i];
  }

  // Create substring from this[start..start+len).
  constexpr ConstexprStringView substr(size_t start, size_t len) const {
    X_ASSERT(start <= size());
    X_ASSERT(start + len <= size());

    return ConstexprStringView(&_array[start], len);
  }

  // Create maximum length substring that begins at 'start'.
  constexpr ConstexprStringView substr(size_t start) const {
    X_ASSERT(start <= size());
    return substr(start, size() - start);
  }

  using const_iterator = const char*;

  constexpr const_iterator begin() const {
    return &_array[0];
  }

  constexpr const_iterator end() const {
    return &_array[size()];
  }

 private:
  const char* _array;  // Never-null for simplicity.
  size_t _size;
};

constexpr bool
operator==(const ConstexprStringView& lhs, const ConstexprStringView& rhs) {
  if (lhs.size() != rhs.size()) {
    return false;
  }
  for (size_t i = 0; i < lhs.size(); ++i) {
    if (lhs[i] != rhs[i]) {
      return false;
    }
  }
  return true;
}

constexpr bool
operator!=(const ConstexprStringView& lhs, const ConstexprStringView& rhs) {
  return !(lhs == rhs);
}

inline std::ostream& operator<<(std::ostream& os, const ConstexprStringView& str) {
  for (char c : str) {
    os << c;
  }
  return os;
}

constexpr bool IsValidJniDescriptorShorty(char shorty) {
  constexpr char kValidJniTypes[] =
      {'V', 'Z', 'B', 'C', 'S', 'I', 'J', 'F', 'D', 'L', '[', '(', ')'};

  for (char c : kValidJniTypes) {
    if (c == shorty) {
      return true;
    }
  }

  return false;
}

// A constexpr "vector" that supports storing a variable amount of Ts
// in an array-like interface.
//
// An up-front kMaxSize must be given since constexpr does not support
// dynamic allocations.
template<typename T, size_t kMaxSize>
struct ConstexprVector {
 public:
  constexpr explicit ConstexprVector() : _size(0u), _array{} {
  }

 private:
  // Custom iterator to support ptr-one-past-end into the union array without
  // undefined behavior.
  template<typename Elem>
  struct VectorIterator {
    Elem* ptr;

    constexpr VectorIterator& operator++() {
      ++ptr;
      return *this;
    }

    constexpr VectorIterator operator++(int) const {
      VectorIterator tmp(*this);
      ++tmp;
      return tmp;
    }

    constexpr auto& operator*() {
      // Use 'auto' here since using 'T' is incorrect with const_iterator.
      return ptr->_value;
    }

    constexpr const T& operator*() const {
      return ptr->_value;
    }

    constexpr bool operator==(const VectorIterator& other) const {
      return ptr == other.ptr;
    }

    constexpr bool operator!=(const VectorIterator& other) const {
      return !(*this == other);
    }
  };

  // Do not require that T is default-constructible by using a union.
  struct MaybeElement {
    union {
      T _value;
    };
  };

 public:
  using iterator = VectorIterator<MaybeElement>;
  using const_iterator = VectorIterator<const MaybeElement>;

  constexpr iterator begin() {
    return {&_array[0]};
  }

  constexpr iterator end() {
    return {&_array[size()]};
  }

  constexpr const_iterator begin() const {
    return {&_array[0]};
  }

  constexpr const_iterator end() const {
    return {&_array[size()]};
  }

  constexpr void push_back(const T& value) {
    X_ASSERT(_size + 1 <= kMaxSize);

    _array[_size]._value = value;
    _size++;
  }

  // A pop operation could also be added since constexpr T's
  // have default destructors, it would just be _size--.
  // We do not need a pop() here though.

  constexpr const T& operator[](size_t i) const {
    return _array[i]._value;
  }

  constexpr T& operator[](size_t i) {
    return _array[i]._value;
  }

  constexpr size_t size() const {
    return _size;
  }
 private:

  size_t _size;
  MaybeElement _array[kMaxSize];
};

// Parsed and validated "long" form of a single JNI descriptor.
// e.g. one of "J", "Ljava/lang/Object;" etc.
struct JniDescriptorNode {
  ConstexprStringView longy;

  constexpr JniDescriptorNode(ConstexprStringView longy)
      : longy(longy) {  // NOLINT: explicit.
    X_ASSERT(!longy.empty());
  }
  constexpr JniDescriptorNode() : longy() {}

  constexpr char shorty() {
    // Must be initialized with the non-default constructor.
    X_ASSERT(!longy.empty());
    return longy[0];
  }
};

inline std::ostream& operator<<(std::ostream& os, const JniDescriptorNode& node) {
  os << node.longy;
  return os;
}

// Equivalent of C++17 std::optional.
//
// An optional is essentially a type safe
//    union {
//      void Nothing,
//      T    Some;
//    };
//
template<typename T>
struct ConstexprOptional {
  // Create a default optional with no value.
  constexpr ConstexprOptional() : _has_value(false), _nothing() {
  }

  // Create an optional with a value.
  constexpr ConstexprOptional(const T& value)
      : _has_value(true), _value(value) {
  }

  constexpr explicit operator bool() const {
    return _has_value;
  }

  constexpr bool has_value() const {
    return _has_value;
  }

  constexpr const T& value() const {
    X_ASSERT(has_value());
    return _value;
  }

  constexpr const T* operator->() const {
    return &(value());
  }

 private:
  bool _has_value;
  // The "Nothing" is likely unnecessary but improves readability.
  struct Nothing {};
  union {
    Nothing _nothing;
    T _value;
  };
};

template<typename T>
constexpr bool
operator==(const ConstexprOptional<T>& lhs, const ConstexprOptional<T>& rhs) {
  if (lhs && rhs) {
    return lhs.value() == rhs.value();
  }
  return lhs.has_value() == rhs.has_value();
}

template<typename T>
constexpr bool
operator!=(const ConstexprOptional<T>& lhs, const ConstexprOptional<T>& rhs) {
  return !(lhs == rhs);
}

template<typename T>
inline std::ostream& operator<<(std::ostream& os, const ConstexprOptional<T>& val) {
  if (val) {
    os << val.value();
  }
  return os;
}

// Equivalent of std::nullopt
// Allows implicit conversion to any empty ConstexprOptional<T>.
// Mostly useful for macros that need to return an empty constexpr optional.
struct NullConstexprOptional {
  template<typename T>
  constexpr operator ConstexprOptional<T>() const {
    return ConstexprOptional<T>();
  }
};

inline std::ostream& operator<<(std::ostream& os, NullConstexprOptional) {
  return os;
}

#if !defined(PARSE_FAILURES_NONFATAL)
// Unfortunately we cannot have custom messages here, as it just prints a stack trace with the macros expanded.
// This is at least more flexible than static_assert which requires a string literal.
// NOTE: The message string literal must be on same line as the macro to be seen during a compilation error.
#define PARSE_FAILURE(msg) X_ASSERT(! #msg)
#define PARSE_ASSERT_MSG(cond, msg) X_ASSERT(#msg && (cond))
#define PARSE_ASSERT(cond) X_ASSERT(cond)
#else
#define PARSE_FAILURE(msg) return NullConstexprOptional{};
#define PARSE_ASSERT_MSG(cond, msg) if (!(cond)) { PARSE_FAILURE(msg); }
#define PARSE_ASSERT(cond) if (!(cond)) { PARSE_FAILURE(""); }
#endif

// This is a placeholder function and should not be called directly.
constexpr void ParseFailure(const char* msg) {
  (void) msg;  // intentionally no-op.
}

// Temporary parse data when parsing a function descriptor.
struct ParseTypeDescriptorResult {
  // A single argument descriptor, e.g. "V" or "Ljava/lang/Object;"
  ConstexprStringView token;
  // The remainder of the function descriptor yet to be parsed.
  ConstexprStringView remainder;

  constexpr bool has_token() const {
    return token.size() > 0u;
  }

  constexpr bool has_remainder() const {
    return remainder.size() > 0u;
  }

  constexpr JniDescriptorNode as_node() const {
    X_ASSERT(has_token());
    return {token};
  }
};

// Parse a single type descriptor out of a function type descriptor substring,
// and return the token and the remainder string.
//
// If parsing fails (i.e. illegal syntax), then:
//    parses are fatal -> assertion is triggered (default behavior),
//    parses are nonfatal -> returns nullopt (test behavior).
constexpr ConstexprOptional<ParseTypeDescriptorResult>
ParseSingleTypeDescriptor(ConstexprStringView single_type,
                          bool allow_void = false) {
  constexpr NullConstexprOptional kUnreachable = {};

  // Nothing else left.
  if (single_type.size() == 0) {
    return ParseTypeDescriptorResult{};
  }

  ConstexprStringView token;
  ConstexprStringView remainder = single_type.substr(/*start*/1u);

  char c = single_type[0];
  PARSE_ASSERT(IsValidJniDescriptorShorty(c));

  enum State {
    kSingleCharacter,
    kArray,
    kObject
  };

  State state = kSingleCharacter;

  // Parse the first character to figure out if we should parse the rest.
  switch (c) {
    case '!': {
      constexpr bool fast_jni_is_deprecated = false;
      PARSE_ASSERT(fast_jni_is_deprecated);
      break;
    }
    case 'V':
      if (!allow_void) {
        constexpr bool void_type_descriptor_only_allowed_in_return_type = false;
        PARSE_ASSERT(void_type_descriptor_only_allowed_in_return_type);
      }
      [[clang::fallthrough]];
    case 'Z':
    case 'B':
    case 'C':
    case 'S':
    case 'I':
    case 'J':
    case 'F':
    case 'D':
      token = single_type.substr(/*start*/0u, /*len*/1u);
      break;
    case 'L':
      state = kObject;
      break;
    case '[':
      state = kArray;
      break;
    default: {
      // See JNI Chapter 3: Type Signatures.
      PARSE_FAILURE("Expected a valid type descriptor character.");
      return kUnreachable;
    }
  }

  // Possibly parse an arbitary-long remainder substring.
  switch (state) {
    case kSingleCharacter:
      return {{token, remainder}};
    case kArray: {
      // Recursively parse the array component, as it's just any non-void type descriptor.
      ConstexprOptional<ParseTypeDescriptorResult>
          maybe_res = ParseSingleTypeDescriptor(remainder, /*allow_void*/false);
      PARSE_ASSERT(maybe_res);  // Downstream parsing has asserted, bail out.

      ParseTypeDescriptorResult res = maybe_res.value();

      // Reject illegal array type descriptors such as "]".
      PARSE_ASSERT_MSG(res.has_token(),
                       "All array types must follow by their component type (e.g. ']I', ']]Z', etc. ");

      token = single_type.substr(/*start*/0u, res.token.size() + 1u);

      return {{token, res.remainder}};
    }
    case kObject: {
      // Parse the fully qualified class, e.g. Lfoo/bar/baz;
      // Note checking that each part of the class name is a valid class identifier
      // is too complicated (JLS 3.8).
      // This simple check simply scans until the next ';'.
      bool found_semicolon = false;
      size_t semicolon_len = 0;
      for (size_t i = 0; i < single_type.size(); ++i) {
        if (single_type[i] == ';') {
          semicolon_len = i + 1;
          found_semicolon = true;
          break;
        }
      }

      PARSE_ASSERT(found_semicolon);

      token = single_type.substr(/*start*/0u, semicolon_len);
      remainder = single_type.substr(/*start*/semicolon_len);

      bool class_name_is_empty = token.size() <= 2u;  // e.g. "L;"
      PARSE_ASSERT(!class_name_is_empty);

      return {{token, remainder}};
    }
    default:
      X_ASSERT(false);
  }

  X_ASSERT(false);
  return kUnreachable;
}

// Abstract data type to represent container for Ret(Args,...).
template<typename T, size_t kMaxSize>
struct FunctionSignatureDescriptor {
  ConstexprVector<T, kMaxSize> args;
  T ret;

  static constexpr size_t max_size = kMaxSize;
};


template<typename T, size_t kMaxSize>
inline std::ostream& operator<<(std::ostream& os,
                                const FunctionSignatureDescriptor<T,
                                                                  kMaxSize>& signature) {
  size_t count = 0;
  os << "args={";
  for (auto& arg : signature.args) {
    os << arg;

    if (count != signature.args.size() - 1) {
      os << ",";
    }

    ++count;
  }
  os << "}, ret=";
  os << signature.ret;
  return os;
}

// Ret(Args...) of JniDescriptorNode.
template<size_t kMaxSize>
using JniSignatureDescriptor = FunctionSignatureDescriptor<JniDescriptorNode,
                                                           kMaxSize>;

// Parse a JNI function signature descriptor into a JniSignatureDescriptor.
//
// If parsing fails (i.e. illegal syntax), then:
//    parses are fatal -> assertion is triggered (default behavior),
//    parses are nonfatal -> returns nullopt (test behavior).
template<size_t kMaxSize>
constexpr ConstexprOptional<JniSignatureDescriptor<kMaxSize>>
ParseSignatureAsList(ConstexprStringView signature) {
  // The list of JNI descritors cannot possibly exceed the number of characters
  // in the JNI string literal. We leverage this to give an upper bound of the strlen.
  // This is a bit wasteful but in constexpr there *must* be a fixed upper size for data structures.
  ConstexprVector<JniDescriptorNode, kMaxSize> jni_desc_node_list;
  JniDescriptorNode return_jni_desc;

  enum State {
    kInitial = 0,
    kParsingParameters = 1,
    kParsingReturnType = 2,
    kCompleted = 3,
  };

  State state = kInitial;

  while (!signature.empty()) {
    switch (state) {
      case kInitial: {
        char c = signature[0];
        PARSE_ASSERT_MSG(c == '(',
                         "First character of a JNI signature must be a '('");
        state = kParsingParameters;
        signature = signature.substr(/*start*/1u);
        break;
      }
      case kParsingParameters: {
        char c = signature[0];
        if (c == ')') {
          state = kParsingReturnType;
          signature = signature.substr(/*start*/1u);
          break;
        }

        ConstexprOptional<ParseTypeDescriptorResult>
            res = ParseSingleTypeDescriptor(signature, /*allow_void*/false);
        PARSE_ASSERT(res);

        jni_desc_node_list.push_back(res->as_node());

        signature = res->remainder;
        break;
      }
      case kParsingReturnType: {
        ConstexprOptional<ParseTypeDescriptorResult>
            res = ParseSingleTypeDescriptor(signature, /*allow_void*/true);
        PARSE_ASSERT(res);

        return_jni_desc = res->as_node();
        signature = res->remainder;
        state = kCompleted;
        break;
      }
      default: {
        // e.g. "()VI" is illegal because the V terminates the signature.
        PARSE_FAILURE("Signature had left over tokens after parsing return type");
        break;
      }
    }
  }

  switch (state) {
    case kCompleted:
      // Everything is ok.
      break;
    case kParsingParameters:
      PARSE_FAILURE("Signature was missing ')'");
      break;
    case kParsingReturnType:
      PARSE_FAILURE("Missing return type");
    case kInitial:
      PARSE_FAILURE("Cannot have an empty signature");
    default:
      X_ASSERT(false);
  }

  return {{jni_desc_node_list, return_jni_desc}};
}

// What kind of JNI does this type belong to?
enum NativeKind {
  kNotJni,        // Illegal parameter used inside of a function type.
  kNormalJniCallingConventionParameter,
  kNormalNative,
  kFastNative,      // Also valid in normal.
  kCriticalNative,  // Also valid in fast/normal.
};

// Is this type final, i.e. it cannot be subtyped?
enum TypeFinal {
  kNotFinal,
  kFinal         // e.g. any primitive or any "final" class such as String.
};

// What position is the JNI type allowed to be in?
// Ignored when in a CriticalNative context.
enum NativePositionAllowed {
  kNotAnyPosition,
  kReturnPosition,
  kZerothPosition,
  kFirstOrLaterPosition,
  kSecondOrLaterPosition,
};

constexpr NativePositionAllowed ConvertPositionToAllowed(size_t position) {
  switch (position) {
    case 0:
      return kZerothPosition;
    case 1:
      return kFirstOrLaterPosition;
    default:
      return kSecondOrLaterPosition;
  }
}

// Type traits for a JNI parameter type. See below for specializations.
template<typename T>
struct jni_type_trait {
  static constexpr NativeKind native_kind = kNotJni;
  static constexpr const char type_descriptor[] = "(illegal)";
  static constexpr NativePositionAllowed position_allowed = kNotAnyPosition;
  static constexpr TypeFinal type_finality = kNotFinal;
  static constexpr const char type_name[] = "(illegal)";
};

// Access the jni_type_trait<T> from a non-templated constexpr function.
// Identical non-static fields to jni_type_trait, see Reify().
struct ReifiedJniTypeTrait {
  NativeKind native_kind;
  ConstexprStringView type_descriptor;
  NativePositionAllowed position_allowed;
  TypeFinal type_finality;
  ConstexprStringView type_name;

  template<typename T>
  static constexpr ReifiedJniTypeTrait Reify() {
    // This should perhaps be called 'Type Erasure' except we don't use virtuals,
    // so it's not quite the same idiom.
    using TR = jni_type_trait<T>;
    return {TR::native_kind,
            TR::type_descriptor,
            TR::position_allowed,
            TR::type_finality,
            TR::type_name};
  }

  // Find the most similar ReifiedJniTypeTrait corresponding to the type descriptor.
  //
  // Any type can be found by using the exact canonical type descriptor as listed
  // in the jni type traits definitions.
  //
  // Non-final JNI types have limited support for inexact similarity:
  //   [[* | [L* -> jobjectArray
  //   L* -> jobject
  //
  // Otherwise return a nullopt.
  static constexpr ConstexprOptional<ReifiedJniTypeTrait>
  MostSimilarTypeDescriptor(ConstexprStringView type_descriptor);
};

constexpr bool
operator==(const ReifiedJniTypeTrait& lhs, const ReifiedJniTypeTrait& rhs) {
  return lhs.native_kind == rhs.native_kind
      && rhs.type_descriptor == lhs.type_descriptor &&
      lhs.position_allowed == rhs.position_allowed
      && rhs.type_finality == lhs.type_finality &&
      lhs.type_name == rhs.type_name;
}

inline std::ostream& operator<<(std::ostream& os, const ReifiedJniTypeTrait& rjft) {
  // os << "ReifiedJniTypeTrait<" << rjft.type_name << ">";
  os << rjft.type_name;
  return os;
}

// Template specialization for any JNI typedefs.
#define JNI_TYPE_TRAIT(jtype, the_type_descriptor, the_native_kind, the_type_finality, the_position) \
template <>                                                                    \
struct jni_type_trait< jtype > {                                               \
  static constexpr NativeKind native_kind = the_native_kind;                   \
  static constexpr const char type_descriptor[] = the_type_descriptor;         \
  static constexpr NativePositionAllowed position_allowed = the_position;      \
  static constexpr TypeFinal type_finality = the_type_finality;                \
  static constexpr const char type_name[] = #jtype;                            \
};

#define DEFINE_JNI_TYPE_TRAIT(TYPE_TRAIT_FN)                                                                  \
TYPE_TRAIT_FN(jboolean,          "Z",                      kCriticalNative,   kFinal, kSecondOrLaterPosition) \
TYPE_TRAIT_FN(jbyte,             "B",                      kCriticalNative,   kFinal, kSecondOrLaterPosition) \
TYPE_TRAIT_FN(jchar,             "C",                      kCriticalNative,   kFinal, kSecondOrLaterPosition) \
TYPE_TRAIT_FN(jshort,            "S",                      kCriticalNative,   kFinal, kSecondOrLaterPosition) \
TYPE_TRAIT_FN(jint,              "I",                      kCriticalNative,   kFinal, kSecondOrLaterPosition) \
TYPE_TRAIT_FN(jlong,             "J",                      kCriticalNative,   kFinal, kSecondOrLaterPosition) \
TYPE_TRAIT_FN(jfloat,            "F",                      kCriticalNative,   kFinal, kSecondOrLaterPosition) \
TYPE_TRAIT_FN(jdouble,           "D",                      kCriticalNative,   kFinal, kSecondOrLaterPosition) \
TYPE_TRAIT_FN(jobject,           "Ljava/lang/Object;",     kFastNative,    kNotFinal, kFirstOrLaterPosition)  \
TYPE_TRAIT_FN(jclass,            "Ljava/lang/Class;",      kFastNative,       kFinal, kFirstOrLaterPosition)  \
TYPE_TRAIT_FN(jstring,           "Ljava/lang/String;",     kFastNative,       kFinal, kSecondOrLaterPosition) \
TYPE_TRAIT_FN(jarray,            "Ljava/lang/Object;",     kFastNative,    kNotFinal, kSecondOrLaterPosition) \
TYPE_TRAIT_FN(jobjectArray,      "[Ljava/lang/Object;",    kFastNative,    kNotFinal, kSecondOrLaterPosition) \
TYPE_TRAIT_FN(jbooleanArray,     "[Z",                     kFastNative,       kFinal, kSecondOrLaterPosition) \
TYPE_TRAIT_FN(jbyteArray,        "[B",                     kFastNative,       kFinal, kSecondOrLaterPosition) \
TYPE_TRAIT_FN(jcharArray,        "[C",                     kFastNative,       kFinal, kSecondOrLaterPosition) \
TYPE_TRAIT_FN(jshortArray,       "[S",                     kFastNative,       kFinal, kSecondOrLaterPosition) \
TYPE_TRAIT_FN(jintArray,         "[I",                     kFastNative,       kFinal, kSecondOrLaterPosition) \
TYPE_TRAIT_FN(jlongArray,        "[J",                     kFastNative,       kFinal, kSecondOrLaterPosition) \
TYPE_TRAIT_FN(jfloatArray,       "[F",                     kFastNative,       kFinal, kSecondOrLaterPosition) \
TYPE_TRAIT_FN(jdoubleArray,      "[D",                     kFastNative,       kFinal, kSecondOrLaterPosition) \
TYPE_TRAIT_FN(jthrowable,        "Ljava/lang/Throwable;",  kFastNative,    kNotFinal, kSecondOrLaterPosition) \
TYPE_TRAIT_FN(JNIEnv*,           "",                       kNormalJniCallingConventionParameter, kFinal, kZerothPosition) \
TYPE_TRAIT_FN(void,              "V",                      kCriticalNative,   kFinal, kReturnPosition)        \

DEFINE_JNI_TYPE_TRAIT(JNI_TYPE_TRAIT)

// See ReifiedJniTypeTrait for documentation.
constexpr ConstexprOptional<ReifiedJniTypeTrait>
ReifiedJniTypeTrait::MostSimilarTypeDescriptor(ConstexprStringView type_descriptor) {
#define MATCH_EXACT_TYPE_DESCRIPTOR_FN(type, type_desc, native_kind, ...) \
    if (type_descriptor == type_desc && native_kind >= kNormalNative) {                        \
      return { Reify<type>() };                                \
    }

  // Attempt to look up by the precise type match first.
  DEFINE_JNI_TYPE_TRAIT(MATCH_EXACT_TYPE_DESCRIPTOR_FN);

  // Otherwise, we need to do an imprecise match:
  char shorty = type_descriptor.size() >= 1 ? type_descriptor[0] : '\0';
  if (shorty == 'L') {
    // Something more specific like Ljava/lang/Throwable, string, etc
    // is already matched by the macro-expanded conditions above.
    return {Reify<jobject>()};
  } else if (type_descriptor.size() >= 2) {
    auto shorty_shorty = type_descriptor.substr(/*start*/0, /*size*/2u);
    if (shorty_shorty == "[[" || shorty_shorty == "[L") {
      // JNI arrays are covariant, so any type T[] (T!=primitive) is castable to Object[].
      return {Reify<jobjectArray>()};
    }
  }

  // To handle completely invalid values.
  return NullConstexprOptional{};
}

// Check if a jni parameter type is valid given its position and native_kind.
template <typename T>
constexpr bool IsValidJniParameter(NativeKind native_kind, NativePositionAllowed position) {
  // const,volatile does not affect JNI compatibility since it does not change ABI.
  using expected_trait = jni_type_trait<typename std::remove_cv<T>::type>;
  NativeKind expected_native_kind = expected_trait::native_kind;

  // Most types 'T' are not valid for JNI.
  if (expected_native_kind == NativeKind::kNotJni) {
    return false;
  }

  // The rest of the types might be valid, but it depends on the context (native_kind)
  // and also on their position within the parameters.

  // Position-check first. CriticalNatives ignore positions since the first 2 special parameters are stripped.
  while (native_kind != kCriticalNative) {
    NativePositionAllowed expected_position = expected_trait::position_allowed;
    X_ASSERT(expected_position != kNotAnyPosition);

    // Is this a return-only position?
    if (expected_position == kReturnPosition) {
      if (position != kReturnPosition) {
        // void can only be in the return position.
        return false;
      }
      // Don't do the other non-return position checks for a return-only position.
      break;
    }

    // JNIEnv* can only be in the first spot.
    if (position == kZerothPosition && expected_position != kZerothPosition) {
      return false;
      // jobject, jclass can be 1st or anywhere afterwards.
    } else if (position == kFirstOrLaterPosition
        && expected_position != kFirstOrLaterPosition) {
      return false;
      // All other parameters must be in 2nd+ spot, or in the return type.
    } else if (position == kSecondOrLaterPosition
        || position == kReturnPosition) {
      if (expected_position != kFirstOrLaterPosition
          && expected_position != kSecondOrLaterPosition) {
        return false;
      }
    }

    break;
  }

  // Ensure the type appropriate is for the native kind.
  if (expected_native_kind == kNormalJniCallingConventionParameter) {
    // It's always wrong to use a JNIEnv* anywhere but the 0th spot.
    if (native_kind == kCriticalNative) {
      // CriticalNative does not allow using a JNIEnv*.
      return false;
    }

    return true;  // OK: JniEnv* used in 0th position.
  } else if (expected_native_kind == kCriticalNative) {
    // CriticalNative arguments are always valid JNI types anywhere used.
    return true;
  } else if (native_kind == kCriticalNative) {
    // The expected_native_kind was non-critical but we are in a critical context.
    // Illegal type.
    return false;
  }

  // Everything else is fine, e.g. fast/normal native + fast/normal native parameters.
  return true;
}

// Is there sufficient number of parameters given the kind of JNI that it is?
constexpr bool IsJniParameterCountValid(NativeKind native_kind, size_t count) {
  if (native_kind == kNormalNative || native_kind == kFastNative) {
    return count >= 2u;
  } else if (native_kind == kCriticalNative) {
    return true;
  }

  constexpr bool invalid_parameter = false;
  X_ASSERT(invalid_parameter);
  return false;
}

// Basic template interface. See below for partial specializations.
//
// Each instantiation will have a 'value' field that determines whether or not
// all of the Args are valid JNI arguments given their native_kind.
template<NativeKind native_kind, size_t position, typename ... Args>
struct is_valid_jni_argument_type {
  // static constexpr bool value = ?;
};

template<NativeKind native_kind, size_t position>
struct is_valid_jni_argument_type<native_kind, position> {
  static constexpr bool value = true;
};

template<NativeKind native_kind, size_t position, typename T>
struct is_valid_jni_argument_type<native_kind, position, T> {
  static constexpr bool value =
      IsValidJniParameter<T>(native_kind, ConvertPositionToAllowed(position));
};

template<NativeKind native_kind, size_t position, typename T, typename ... Args>
struct is_valid_jni_argument_type<native_kind, position, T, Args...> {
  static constexpr bool value =
      IsValidJniParameter<T>(native_kind, ConvertPositionToAllowed(position))
          && is_valid_jni_argument_type<native_kind,
                                        position + 1,
                                        Args...>::value;
};

// This helper is required to decompose the function type into a list of arg types.
template<NativeKind native_kind, typename T, T fn>
struct is_valid_jni_function_type_helper;

template<NativeKind native_kind, typename R, typename ... Args, R fn(Args...)>
struct is_valid_jni_function_type_helper<native_kind, R(Args...), fn> {
  static constexpr bool value =
      IsJniParameterCountValid(native_kind, sizeof...(Args))
          && IsValidJniParameter<R>(native_kind, kReturnPosition)
          && is_valid_jni_argument_type<native_kind, /*position*/
                                        0,
                                        Args...>::value;
};

// Is this function type 'T' a valid C++ function type given the native_kind?
template<NativeKind native_kind, typename T, T fn>
constexpr bool IsValidJniFunctionType() {
  return is_valid_jni_function_type_helper<native_kind, T, fn>::value;
  // TODO: we could replace template metaprogramming with constexpr by
  // using FunctionTypeMetafunction.
}

// Many parts of std::array is not constexpr until C++17.
template<typename T, size_t N>
struct ConstexprArray {
  // Intentionally public to conform to std::array.
  // This means all constructors are implicit.
  // *NOT* meant to be used directly, use the below functions instead.
  //
  // The reason std::array has it is to support direct-list-initialization,
  // e.g. "ConstexprArray<T, sz>{T{...}, T{...}, T{...}, ...};"
  //
  // Note that otherwise this would need a very complicated variadic
  // argument constructor to only support list of Ts.
  T _array[N];

  constexpr size_t size() const {
    return N;
  }

  using iterator = T*;
  using const_iterator = const T*;

  constexpr iterator begin() {
    return &_array[0];
  }

  constexpr iterator end() {
    return &_array[N];
  }

  constexpr const_iterator begin() const {
    return &_array[0];
  }

  constexpr const_iterator end() const {
    return &_array[N];
  }

  constexpr T& operator[](size_t i) {
    return _array[i];
  }

  constexpr const T& operator[](size_t i) const {
    return _array[i];
  }
};

// Why do we need this?
// auto x = {1,2,3} creates an initializer_list,
//   but they can't be returned because it contains pointers to temporaries.
// auto x[] = {1,2,3} doesn't even work because auto for arrays is not supported.
//
// an alternative would be to pull up std::common_t directly into the call site
//   std::common_type_t<Args...> array[] = {1,2,3}
// but that's even more cludgier.
//
// As the other "stdlib-wannabe" functions, it's weaker than the library
// fundamentals std::make_array but good enough for our use.
template<typename... Args>
constexpr auto MakeArray(Args&& ... args) {
  return ConstexprArray<typename std::common_type<Args...>::type,
                        sizeof...(Args)>{args...};
}

// See below.
template<typename T, T fn>
struct FunctionTypeMetafunction {
};

// Enables the "map" operation over the function component types.
template<typename R, typename ... Args, R fn(Args...)>
struct FunctionTypeMetafunction<R(Args...), fn> {
  // Count how many arguments there are, and add 1 for the return type.
  static constexpr size_t
      count = sizeof...(Args) + 1u;  // args and return type.

  // Return an array where the metafunction 'Func' has been applied
  // to every argument type. The metafunction must be returning a common type.
  template<template<typename Arg> class Func>
  static constexpr auto map_args() {
    return map_args_impl<Func>(holder < Args > {}...);
  }

  // Apply the metafunction 'Func' over the return type.
  template<template<typename Ret> class Func>
  static constexpr auto map_return() {
    return Func<R>{}();
  }

 private:
  template<typename T>
  struct holder {
  };

  template<template<typename Arg> class Func, typename Arg0, typename... ArgsRest>
  static constexpr auto map_args_impl(holder<Arg0>, holder<ArgsRest>...) {
    // One does not simply call MakeArray with 0 template arguments...
    auto array = MakeArray(
        Func<Args>{}()...
    );

    return array;
  }

  template<template<typename Arg> class Func>
  static constexpr auto map_args_impl() {
    // This overload provides support for MakeArray() with 0 arguments.
    using ComponentType = decltype(Func<void>{}());

    return ConstexprArray<ComponentType, /*size*/0u>{};
  }
};

// Apply ReifiedJniTypeTrait::Reify<T> for every function component type.
template<typename T>
struct ReifyJniTypeMetafunction {
  constexpr ReifiedJniTypeTrait operator()() const {
    auto res = ReifiedJniTypeTrait::Reify<T>();
    X_ASSERT(res.native_kind != kNotJni);
    return res;
  }
};

// Ret(Args...) where every component is a ReifiedJniTypeTrait.
template<size_t kMaxSize>
using ReifiedJniSignature = FunctionSignatureDescriptor<ReifiedJniTypeTrait,
                                                        kMaxSize>;

// Attempts to convert the function type T into a list of ReifiedJniTypeTraits
// that correspond to the function components.
//
// If conversion fails (i.e. non-jni compatible types), then:
//    parses are fatal -> assertion is triggered (default behavior),
//    parses are nonfatal -> returns nullopt (test behavior).
template <NativeKind native_kind,
          typename T,
          T fn,
          size_t kMaxSize = FunctionTypeMetafunction<T, fn>::count>
constexpr ConstexprOptional<ReifiedJniSignature<kMaxSize>>
MaybeMakeReifiedJniSignature() {
  if (!IsValidJniFunctionType<native_kind, T, fn>()) {
    PARSE_FAILURE("The function signature has one or more types incompatible with JNI.");
  }

  ReifiedJniTypeTrait return_jni_trait =
      FunctionTypeMetafunction<T,
                         fn>::template map_return<ReifyJniTypeMetafunction>();

  constexpr size_t
      kSkipArgumentPrefix = (native_kind != kCriticalNative) ? 2u : 0u;
  ConstexprVector<ReifiedJniTypeTrait, kMaxSize> args;
  auto args_list =
      FunctionTypeMetafunction<T, fn>::template map_args<ReifyJniTypeMetafunction>();
  size_t args_index = 0;
  for (auto& arg : args_list) {
    // Ignore the 'JNIEnv*, jobject' / 'JNIEnv*, jclass' prefix,
    // as its not part of the function descriptor string.
    if (args_index >= kSkipArgumentPrefix) {
      args.push_back(arg);
    }

    ++args_index;
  }

  return {{args, return_jni_trait}};
}

#define COMPARE_DESCRIPTOR_CHECK(expr) if (!(expr)) return false
#define COMPARE_DESCRIPTOR_FAILURE_MSG(msg) if ((true)) return false

// Compares a user-defined JNI descriptor (of a single argument or return value)
// to a reified jni type trait that was derived from the C++ function type.
//
// If comparison fails (i.e. non-jni compatible types), then:
//    parses are fatal -> assertion is triggered (default behavior),
//    parses are nonfatal -> returns false (test behavior).
constexpr bool
CompareJniDescriptorNodeErased(JniDescriptorNode user_defined_descriptor,
                               ReifiedJniTypeTrait derived) {

  ConstexprOptional<ReifiedJniTypeTrait> user_reified_opt =
      ReifiedJniTypeTrait::MostSimilarTypeDescriptor(user_defined_descriptor.longy);

  if (!user_reified_opt.has_value()) {
    COMPARE_DESCRIPTOR_FAILURE_MSG(
        "Could not find any JNI C++ type corresponding to the type descriptor");
  }

  char user_shorty = user_defined_descriptor.longy.size() > 0 ?
                     user_defined_descriptor.longy[0] :
                     '\0';

  ReifiedJniTypeTrait user = user_reified_opt.value();
  if (user == derived) {
    // If we had a similar match, immediately return success.
    return true;
  } else if (derived.type_name == "jthrowable") {
    if (user_shorty == 'L') {
      // Weakly allow any objects to correspond to a jthrowable.
      // We do not know the managed type system so we have to be permissive here.
      return true;
    } else {
      COMPARE_DESCRIPTOR_FAILURE_MSG(
          "jthrowable must correspond to an object type descriptor");
    }
  } else if (derived.type_name == "jarray") {
    if (user_shorty == '[') {
      // a jarray is the base type for all other array types. Allow.
      return true;
    } else {
      // Ljava/lang/Object; is the root for all array types.
      // Already handled above in 'if user == derived'.
      COMPARE_DESCRIPTOR_FAILURE_MSG(
          "jarray must correspond to array type descriptor");
    }
  }
  // Otherwise, the comparison has failed and the rest of this is only to
  // pick the most appropriate error message.
  //
  // Note: A weaker form of comparison would allow matching 'Ljava/lang/String;'
  // against 'jobject', etc. However the policy choice here is to enforce the strictest
  // comparison that we can to utilize the type system to its fullest.

  if (derived.type_finality == kFinal || user.type_finality == kFinal) {
    // Final types, e.g. "I", "Ljava/lang/String;" etc must match exactly
    // the C++ jni descriptor string ('I' -> jint, 'Ljava/lang/String;' -> jstring).
    COMPARE_DESCRIPTOR_FAILURE_MSG(
        "The JNI descriptor string must be the exact type equivalent of the "
            "C++ function signature.");
  } else if (user_shorty == '[') {
    COMPARE_DESCRIPTOR_FAILURE_MSG(
        "The array JNI descriptor must correspond to j${type}Array or jarray");
  } else if (user_shorty == 'L') {
    COMPARE_DESCRIPTOR_FAILURE_MSG(
        "The object JNI descriptor must correspond to jobject.");
  } else {
    X_ASSERT(false);  // We should never get here, but either way this means the types did not match
    COMPARE_DESCRIPTOR_FAILURE_MSG(
        "The JNI type descriptor string does not correspond to the C++ JNI type.");
  }
}

// Matches a user-defined JNI function descriptor against the C++ function type.
//
// If matches fails, then:
//    parses are fatal -> assertion is triggered (default behavior),
//    parses are nonfatal -> returns false (test behavior).
template<NativeKind native_kind, typename T, T fn, size_t kMaxSize>
constexpr bool
MatchJniDescriptorWithFunctionType(ConstexprStringView user_function_descriptor) {
  constexpr size_t kReifiedMaxSize = FunctionTypeMetafunction<T, fn>::count;

  ConstexprOptional<ReifiedJniSignature<kReifiedMaxSize>>
      reified_signature_opt =
      MaybeMakeReifiedJniSignature<native_kind, T, fn>();
  if (!reified_signature_opt) {
    // Assertion handling done by MaybeMakeReifiedJniSignature.
    return false;
  }

  ConstexprOptional<JniSignatureDescriptor<kMaxSize>> user_jni_sig_desc_opt =
      ParseSignatureAsList<kMaxSize>(user_function_descriptor);

  if (!user_jni_sig_desc_opt) {
    // Assertion handling done by ParseSignatureAsList.
    return false;
  }

  ReifiedJniSignature<kReifiedMaxSize>
      reified_signature = reified_signature_opt.value();
  JniSignatureDescriptor<kMaxSize>
      user_jni_sig_desc = user_jni_sig_desc_opt.value();

  if (reified_signature.args.size() != user_jni_sig_desc.args.size()) {
    COMPARE_DESCRIPTOR_FAILURE_MSG(
        "Number of parameters in JNI descriptor string"
            "did not match number of parameters in C++ function type");
  } else if (!CompareJniDescriptorNodeErased(user_jni_sig_desc.ret,
                                             reified_signature.ret)) {
    // Assertion handling done by CompareJniDescriptorNodeErased.
    return false;
  } else {
    for (size_t i = 0; i < user_jni_sig_desc.args.size(); ++i) {
      if (!CompareJniDescriptorNodeErased(user_jni_sig_desc.args[i],
                                          reified_signature.args[i])) {
        // Assertion handling done by CompareJniDescriptorNodeErased.
        return false;
      }
    }
  }

  return true;
}

// Supports inferring the JNI function descriptor string from the C++
// function type when all type components are final.
template<NativeKind native_kind, typename T, T fn>
struct InferJniDescriptor {
  static constexpr size_t kMaxSize = FunctionTypeMetafunction<T, fn>::count;

  // Convert the C++ function type into a JniSignatureDescriptor which holds
  // the canonical (according to jni_traits) descriptors for each component.
  // The C++ type -> JNI mapping must be nonambiguous (see jni_macros.h for exact rules).
  //
  // If conversion fails (i.e. C++ signatures is illegal for JNI, or the types are ambiguous):
  //    if parsing is fatal -> assertion failure (default behavior)
  //    if parsing is nonfatal -> returns nullopt (test behavior).
  static constexpr ConstexprOptional<JniSignatureDescriptor<kMaxSize>> FromFunctionType() {
    constexpr size_t kReifiedMaxSize = kMaxSize;
    ConstexprOptional<ReifiedJniSignature<kReifiedMaxSize>>
        reified_signature_opt =
        MaybeMakeReifiedJniSignature<native_kind, T, fn>();
    if (!reified_signature_opt) {
      // Assertion handling done by MaybeMakeReifiedJniSignature.
      return NullConstexprOptional{};
    }

    ReifiedJniSignature<kReifiedMaxSize>
        reified_signature = reified_signature_opt.value();

    JniSignatureDescriptor<kReifiedMaxSize> signature_descriptor;

    if (reified_signature.ret.type_finality != kFinal) {
      // e.g. jint, jfloatArray, jstring, jclass are ok. jobject, jthrowable, jarray are not.
      PARSE_FAILURE("Bad return type. Only unambigous (final) types can be used to infer a signature.");  // NOLINT
    }
    signature_descriptor.ret =
        JniDescriptorNode{reified_signature.ret.type_descriptor};

    for (size_t i = 0; i < reified_signature.args.size(); ++i) {
      const ReifiedJniTypeTrait& arg_trait = reified_signature.args[i];
      if (arg_trait.type_finality != kFinal) {
        PARSE_FAILURE("Bad parameter type. Only unambigous (final) types can be used to infer a signature.");  // NOLINT
      }
      signature_descriptor.args.push_back(JniDescriptorNode{
          arg_trait.type_descriptor});
    }

    return {signature_descriptor};
  }

  // Calculate the exact string size that the JNI descriptor will be
  // at runtime.
  //
  // Without this we cannot allocate enough space within static storage
  // to fit the compile-time evaluated string.
  static constexpr size_t CalculateStringSize() {
    ConstexprOptional<JniSignatureDescriptor<kMaxSize>>
        signature_descriptor_opt =
        FromFunctionType();
    if (!signature_descriptor_opt) {
      // Assertion handling done by FromFunctionType.
      return 0u;
    }

    JniSignatureDescriptor<kMaxSize> signature_descriptor =
        signature_descriptor_opt.value();

    size_t acc_size = 1u;  // All sigs start with '('.

    // Now add every parameter.
    for (size_t j = 0; j < signature_descriptor.args.size(); ++j) {
      const JniDescriptorNode& arg_descriptor = signature_descriptor.args[j];
      // for (const JniDescriptorNode& arg_descriptor : signature_descriptor.args) {
      acc_size += arg_descriptor.longy.size();
    }

    acc_size += 1u;   // Add space for ')'.

    // Add space for the return value.
    acc_size += signature_descriptor.ret.longy.size();

    return acc_size;
  }

  static constexpr size_t kMaxStringSize = CalculateStringSize();
  using ConstexprStringDescriptorType = ConstexprArray<char,
                                                       kMaxStringSize + 1>;

  static constexpr bool kAllowPartialStrings = false;

  // Convert the JniSignatureDescriptor we get in FromFunctionType()
  // into a flat constexpr char array.
  //
  // This is done by repeated string concatenation at compile-time.
  static constexpr ConstexprStringDescriptorType GetString() {
    ConstexprStringDescriptorType c_str{};

    ConstexprOptional<JniSignatureDescriptor<kMaxSize>>
        signature_descriptor_opt =
        FromFunctionType();
    if (!signature_descriptor_opt.has_value()) {
      // Assertion handling done by FromFunctionType.
      c_str[0] = '\0';
      return c_str;
    }

    JniSignatureDescriptor<kMaxSize> signature_descriptor =
        signature_descriptor_opt.value();

    size_t pos = 0u;
    c_str[pos++] = '(';

    // Copy all parameter descriptors.
    for (size_t j = 0; j < signature_descriptor.args.size(); ++j) {
      const JniDescriptorNode& arg_descriptor = signature_descriptor.args[j];
      ConstexprStringView longy = arg_descriptor.longy;
      for (size_t i = 0; i < longy.size(); ++i) {
        if (kAllowPartialStrings && pos >= kMaxStringSize) {
          break;
        }
        c_str[pos++] = longy[i];
      }
    }

    if (!kAllowPartialStrings || pos < kMaxStringSize) {
      c_str[pos++] = ')';
    }

    // Copy return descriptor.
    ConstexprStringView longy = signature_descriptor.ret.longy;
    for (size_t i = 0; i < longy.size(); ++i) {
      if (kAllowPartialStrings && pos >= kMaxStringSize) {
        break;
      }
      c_str[pos++] = longy[i];
    }

    if (!kAllowPartialStrings) {
      X_ASSERT(pos == kMaxStringSize);
    }

    c_str[pos] = '\0';

    return c_str;
  }

  // Turn a pure constexpr string into one that can be accessed at non-constexpr
  // time. Note that the 'static constexpr' storage must be in the scope of a
  // function (prior to C++17) to avoid linking errors.
  static const char* GetStringAtRuntime() {
    static constexpr ConstexprStringDescriptorType str = GetString();
    return &str[0];
  }
};

// Expression to return JNINativeMethod, performs checking on signature+fn.
#define MAKE_CHECKED_JNI_NATIVE_METHOD(native_kind, name_, signature_, fn) \
  ([]() {                                                                \
    using namespace nativehelper::detail;                                \
    static_assert(                                                       \
        MatchJniDescriptorWithFunctionType<native_kind,                  \
                                           decltype(fn),                 \
                                           fn,                           \
                                           sizeof(signature_)>(signature_),\
        "JNI signature doesn't match C++ function type.");               \
    /* Suppress implicit cast warnings by explicitly casting. */         \
    return JNINativeMethod {                                             \
        const_cast<decltype(JNINativeMethod::name)>(name_),              \
        const_cast<decltype(JNINativeMethod::signature)>(signature_),    \
        reinterpret_cast<void*>(&fn)};                                   \
  })()

// Expression to return JNINativeMethod, infers signature from fn.
#define MAKE_INFERRED_JNI_NATIVE_METHOD(native_kind, name_, fn)          \
  ([]() {                                                                \
    using namespace nativehelper::detail;                                \
    /* Suppress implicit cast warnings by explicitly casting. */         \
    return JNINativeMethod {                                             \
        const_cast<decltype(JNINativeMethod::name)>(name_),              \
        const_cast<decltype(JNINativeMethod::signature)>(                \
            InferJniDescriptor<native_kind,                              \
                               decltype(fn),                             \
                               fn>::GetStringAtRuntime()),               \
        reinterpret_cast<void*>(&fn)};                                   \
  })()

}  // namespace detail
}  // namespace nativehelper

