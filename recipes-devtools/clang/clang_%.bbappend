# Define a unique subdirectory for this specific version/build
CLANG_NATIVE_DIR = "${STAGING_BINDIR_NATIVE}/clang-native"

# Pass the new prefix to the build system (Clang uses CMake)
EXTRA_OECMAKE:append:class-native = " -DCMAKE_INSTALL_PREFIX=${CLANG_NATIVE_DIR}"

# Ensure BitBake doesn't try to stage it into the default /usr/bin/
SYSROOT_DIRS:append:class-native = " ${CLANG_NATIVE_DIR}"
