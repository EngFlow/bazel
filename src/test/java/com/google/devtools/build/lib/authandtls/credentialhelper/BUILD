load("@rules_java//java:defs.bzl", "java_library", "java_test")

filegroup(
    name = "srcs",
    srcs = glob(["**"]),
    visibility = ["//src:__subpackages__"],
)

java_test(
    name = "CredentialHelperTests",
    srcs = glob(["*.java"]),
    data = [
        ":test_credential_helper",
    ],
    test_class = "com.google.devtools.build.lib.AllTests",
    runtime_deps = [
        "//src/test/java/com/google/devtools/build/lib:test_runner",
    ],
    deps = [
        "//src/main/java/com/google/devtools/build/lib/authandtls/credentialhelper",
        "//src/main/java/com/google/devtools/build/lib/shell",
        "//src/main/java/com/google/devtools/build/lib/util:command",
        "//third_party:truth",
        "@bazel_tools//tools/java/runfiles",
    ],
)

cc_binary(
    name = "test_credential_helper",
    srcs = [
        "test_credential_helper.cc",
    ],
    deps = [
        "//src/main/protobuf:credential_helper_cc_proto",
    ],
)
