#do_populate_sysroot:class-native:clang-native[noexec] = "1"
#SYSROOT_DIRS:class-native:clang-native = ""
#SYSROOT_DIRS_NATIVE:class-native:clang-native = ""
#SYSROOT_DIRS_NATIVE:pn-clang-native = ""
#do_populate_sysroot:pn-clang-native[noexec] = "1"
# clang-native_%.bbappend

# Define a unique subdirectory for this specific version/build
CLANG_PRIVATE_DIR = "${STAGING_BINDIR_NATIVE}/clang-special"

# Pass the new prefix to the build system (Clang uses CMake)
EXTRA_OECMAKE:append:class-native = " -DCMAKE_INSTALL_PREFIX=${CLANG_PRIVATE_DIR}"

# Ensure BitBake doesn't try to stage it into the default /usr/bin/
SYSROOT_DIRS:append:class-native = " ${CLANG_PRIVATE_DIR}"
