SUMMARY = "Swift native toolchain for Linux"
HOMEPAGE = "https://swift.org/install/"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

require swift-version.inc
PV = "6.2.4"

def swift_native_arch_suffix(d):
    host_arch = d.getVar('HOST_ARCH')
    if host_arch == 'x86_64':
        return ''
    else:
        return f'-{host_arch}'

def swift_host_arch(d):
    return swift_native_arch_suffix(d).lstrip('-')

SWIFT_ARCH_SUFFIX = "${@swift_native_arch_suffix(d)}"
SWIFT_HOST_ARCH = "${@swift_host_arch(d)}"

#tag=${SWIFT_TAG}
SRC_DIR = "swift-project"
SRC_URI = "git://github.com/swiftlang/swift.git;tag=${SWIFT_TAG};nobranch=1;protocol=https;destsuffix=git/swift-project/swift"
SRC_URI += "file://hashes"

DEPENDS += "\
    cmake-native \
    ninja-native \
    python3-native \
    libxml2-native \
    zlib-native \
    curl-native \
    icu-native \
    clang-native \
"
RDEPENDS:${PN} = "ncurses-native"

S = "${WORKDIR}/git/swift-project/swift"
B = "${WORKDIR}/build"

inherit native
do_configure[network] = "1"
do_configure() {
    cd ${S}
    cp ${WORKDIR}/hashes ${S}/hashes
    ./utils/update-checkout --clone --scheme repro --config hashes || true     
}

do_compile() {
    #cd ${S}
    #./utils/build-script \
    #    --release \
    #    --bootstrapping bootstrapping --reconfigure

    cd ${S}
    ./utils/build-script --preset bootstrap_stage0 build_subdir=bootstrap_stage0 install_destdir=${B}/stage0 && \
    PATH="${B}/stage0/usr/bin:$PATH" ./utils/build-script --preset bootstrap_stage2 build_subdir=bootstrap_stage2 install_destdir=${B}/stage2
}

########################################################################
# This informs bitbake that we want to install a non-default directory #
# in the native sysroot.                                               #
########################################################################

PACKAGES = "\
    ${PN}-libdispatch \
    ${PN}-libdispatch-dev \
    ${PN}-libdispatch-staticdev \
    ${PN}-stdlib \
    ${PN}-stdlib-dev \
    ${PN}-stdlib-staticdev \
    ${PN}-stdlib-embedded-dev \
    ${PN}-stdlib-embedded-staticdev \
    ${PN}-foundation \
    ${PN}-foundation-dev \
    ${PN}-foundation-staticdev \
    ${PN}-foundation-essentials \
    ${PN}-foundation-essentials-dev \
    ${PN}-foundation-essentials-staticdev \
    ${PN}-foundation-icu \
    ${PN}-foundation-icu-dev \
    ${PN}-testing \
    ${PN}-testing-dev \
    ${PN}-xctest \
    ${PN}-xctest-dev \
"

do_install() {
    TOOLCHAIN_DIR=$(find build -maxdepth 3 -type d -name "swift-linux-*")

    install -d ${D}${bindir}
    cp -r ${TOOLCHAIN_DIR}/usr/bin/* ${D}${bindir}

    install -d ${D}${libdir}
    cp -rd ${TOOLCHAIN_DIR}/usr/lib/* ${D}${libdir}

    install -d ${D}${includedir}
    cp -rd ${TOOLCHAIN_DIR}/usr/include/* ${D}${includedir}

    install -d ${D}${datadir}
    cp -rd ${TOOLCHAIN_DIR}/usr/share/* ${D}${datadir}
}

FILES:${PN} = "\
    ${datadir}/doc \
    ${datadir}/docc \
    ${datadir}/swift \
    ${libdir}/libswiftDemangle.so \
"

FILES:${PN}-dev = "\
    ${bindir} \
    ${includedir} \
    ${libdir}/clang \
    ${libdir}/liblldb* \
    ${libdir}/libIndexStore.so* \
    ${libdir}/libsourcekitdInProc.so* \
    ${libdir}/liblldb.so* \
    ${libdir}/libLTO.so* \
    ${libdir}/swiftToCxx \
    ${libdir}/swift/host \
    ${libdir}/swift/migrator \
    ${libdir}/swift/os \
    ${libdir}/swift/pm \
    ${libdir}/swift/_InternalSwiftStaticMirror \
    ${libdir}/swift/FrameworkABIBaseline \
    ${datadir}/clang \
    ${datadir}/man \
    ${datadir}/pm \
"

FILES:${PN}-stdlib = "\
    ${libdir}/swift/linux/clang/* \
    ${libdir}/swift/linux/apinotes/* \
    ${libdir}/swift/linux/_InternalSwiftScan/* \
    ${libdir}/swift/linux/libswift_RegexParser.so \
    ${libdir}/swift/linux/libswiftSwiftPrivateThreadExtras.so \
    ${libdir}/swift/linux/libswift_Concurrency.so \
    ${libdir}/swift/linux/libswift_Differentiation.so \
    ${libdir}/swift/linux/libswiftDifferentiationUnittest.so \
    ${libdir}/swift/linux/libswiftDistributed.so \
    ${libdir}/swift/linux/libswiftRegexBuilder.so \
    ${libdir}/swift/linux/libswiftObservation.so \
    ${libdir}/swift/linux/libswiftSwiftOnoneSupport.so \
    ${libdir}/swift/linux/libswiftSwiftPrivateLibcExtras.so \
    ${libdir}/swift/linux/libswiftRuntimeUnittest.so \
    ${libdir}/swift/linux/libswift_StringProcessing.so \
    ${libdir}/swift/linux/libswiftGlibc.so \
    ${libdir}/swift/linux/libswiftCore.so \
    ${libdir}/swift/linux/libswift_Builtin_float.so \
    ${libdir}/swift/linux/libswiftSwiftPrivate.so \
    ${libdir}/swift/linux/libswiftSynchronization.so \
    ${libdir}/swift/linux/libswiftStdlibUnittest.so \
"

FILES:${PN}-stdlib-dev = "\
    ${includedir}/swift \
    ${libdir}/swift/shims \
    ${libdir}/swift/os \
    ${libdir}/swift/apinotes \
    ${libdir}/swift/linux/libswiftCommandLineSupport.a \
    ${libdir}/swift/linux/libswiftCxxStdlib.a \
    ${libdir}/swift/linux/libswiftCxx.a \
    ${libdir}/swift/linux/libcxxshim.modulemap \
    ${libdir}/swift/linux/libstdcxx.modulemap \
    ${libdir}/swift/linux/libstdcxx.h \
    ${libdir}/swift/linux/libcxxshim.h \
    ${libdir}/swift/linux/libcxxstdlibshim.h \
    ${libdir}/swift/linux/${SWIFT_HOST_ARCH} \
    ${libdir}/swift/linux/Cxx.swiftmodule/* \
    ${libdir}/swift/linux/CxxStdlib.swiftmodule/* \
    ${libdir}/swift/linux/Distributed.swiftmodule/* \
    ${libdir}/swift/linux/Glibc.swiftmodule/* \
    ${libdir}/swift/linux/Observation.swiftmodule/* \
    ${libdir}/swift/linux/RegexBuilder.swiftmodule/* \
    ${libdir}/swift/linux/Swift.swiftmodule/* \
    ${libdir}/swift/linux/SwiftOnoneSupport.swiftmodule/* \
    ${libdir}/swift/linux/Synchronization.swiftmodule/* \
    ${libdir}/swift/linux/_Backtracing.swiftmodule/* \
    ${libdir}/swift/linux/_Builtin_float.swiftmodule/* \
    ${libdir}/swift/linux/_RegexParser.swiftmodule/* \
    ${libdir}/swift/linux/_Concurrency.swiftmodule/* \
    ${libdir}/swift/linux/_Differentiation.swiftmodule/* \
    ${libdir}/swift/linux/_StringProcessing.swiftmodule/* \
    ${libdir}/swift/linux/_Volatile.swiftmodule/* \
    ${libdir}/swift/linux/libswiftCore.so \
    ${libdir}/swift/linux/libswiftDispatch.so \
    ${libdir}/swift/linux/libswiftDistributed.so \
    ${libdir}/swift/linux/libswiftGlibc.so \
    ${libdir}/swift/linux/libswiftObservation.so \
    ${libdir}/swift/linux/libswiftRegexBuilder.so \
    ${libdir}/swift/linux/libswiftSwiftOnoneSupport.so \
    ${libdir}/swift/linux/libswiftSynchronization.so \
    ${libdir}/swift/linux/libswift_Backtracing.so \
    ${libdir}/swift/linux/libswift_Builtin_float.so \
    ${libdir}/swift/linux/libswift_Concurrency.so \
    ${libdir}/swift/linux/libswift_Differentiation.so \
    ${libdir}/swift/linux/libswift_StringProcessing.so \
    ${libdir}/swift/linux/libswift_Volatile.so \
    ${libdir}/swift/linux/${SWIFT_HOST_ARCH}/* \
"

FILES:${PN}-stdlib-staticdev = "\
    ${libdir}/swift_static/shims \
    ${libdir}/swift_static/os \
    ${libdir}/swift_static/linux/static-executable-args.lnk \
    ${libdir}/swift_static/linux/libcxxshim.h \
    ${libdir}/swift_static/linux/libcxxshim.modulemap \
    ${libdir}/swift_static/linux/libstdcxx.h \
    ${libdir}/swift_static/linux/libstdcxx.modulemap \
    ${libdir}/swift_static/linux/libcxxstdlibshim.h \
    ${libdir}/swift_static/linux/Cxx.swiftmodule/* \
    ${libdir}/swift_static/linux/CxxStdlib.swiftmodule/* \
    ${libdir}/swift_static/linux/Distributed.swiftmodule/* \
    ${libdir}/swift_static/linux/Glibc.swiftmodule/* \
    ${libdir}/swift_static/linux/Observation.swiftmodule/* \
    ${libdir}/swift_static/linux/RegexBuilder.swiftmodule/* \
    ${libdir}/swift_static/linux/Swift.swiftmodule/* \
    ${libdir}/swift_static/linux/SwiftOnoneSupport.swiftmodule/* \
    ${libdir}/swift_static/linux/Synchronization.swiftmodule/* \
    ${libdir}/swift_static/linux/_Backtracing.swiftmodule/* \
    ${libdir}/swift_static/linux/_Builtin_float.swiftmodule/* \
    ${libdir}/swift_static/linux/_RegexParser.swiftmodule/* \
    ${libdir}/swift_static/linux/_Concurrency.swiftmodule/* \
    ${libdir}/swift_static/linux/_Differentiation.swiftmodule/* \
    ${libdir}/swift_static/linux/_StringProcessing.swiftmodule/* \
    ${libdir}/swift_static/linux/_Volatile.swiftmodule/* \
    ${libdir}/swift_static/linux/libswiftCore.a \
    ${libdir}/swift_static/linux/libswiftDispatch.a \
    ${libdir}/swift_static/linux/libswiftDistributed.a \
    ${libdir}/swift_static/linux/libswiftGlibc.a \
    ${libdir}/swift_static/linux/libswiftObservation.a \
    ${libdir}/swift_static/linux/libswiftRegexBuilder.a \
    ${libdir}/swift_static/linux/libswiftSwiftOnoneSupport.a \
    ${libdir}/swift_static/linux/libswiftSynchronization.a \
    ${libdir}/swift_static/linux/libswift_Backtracing.a \
    ${libdir}/swift_static/linux/libswift_Builtin_float.a \
    ${libdir}/swift_static/linux/libswift_Concurrency.a \
    ${libdir}/swift_static/linux/libswift_Differentiation.a \
    ${libdir}/swift_static/linux/libswift_StringProcessing.a \
    ${libdir}/swift_static/linux/libswift_Volatile.a \
    ${libdir}/swift_static/linux/${SWIFT_HOST_ARCH}/* \
"

FILES:${PN}-stdlib-embedded-dev = "\
    ${libdir}/swift/embedded \
"

FILES:${PN}-stdlib-embedded-staticdev = "\
    ${libdir}/swift_static/embedded \
"

FILES:${PN}-foundation = "\
    ${libdir}/swift/linux/libFoundation.so \
    ${libdir}/swift/linux/libFoundationNetworking.so \
    ${libdir}/swift/linux/libFoundationXML.so \
"

FILES:${PN}-foundation-dev = "\
    ${libdir}/swift/CoreFoundation/* \
    ${libdir}/swift/linux/Foundation.swiftmodule/* \
    ${libdir}/swift/linux/FoundationNetworking.swiftmodule/* \
    ${libdir}/swift/linux/FoundationXML.swiftmodule/* \
"

FILES:${PN}-foundation-staticdev = "\
    ${libdir}/swift/linux/libFoundation.a \
    ${libdir}/swift/linux/libFoundationNetworking.a \
    ${libdir}/swift/linux/libFoundationXML.a \
    ${libdir}/swift/linux/lib_CFURLSessionInterface.a \
"

FILES:${PN}:foundation-essentials = "\
    ${libdir}/swift/linux/libFoundationEssentials.so \
    ${libdir}/swift/linux/libFoundationInternationalization.so \
"

FILES:${PN}-foundation-essentials-dev = "\
    ${libdir}/swift/_FoundationCShims/* \
    ${libdir}/swift/linux/FoundationEssentials.swiftmodule/* \
    ${libdir}/swift/linux/FoundationInternationalization.swiftmodule/* \
    ${libdir}/swift/linux/_FoundationCollections.swiftmodule/* \
"

FILES:${PN}-foundation-essentials-staticdev = "\
    ${libdir}/lib_SwiftLibraryPluginProviderCShims.a \
    ${libdir}/swift_static/linux/libFoundationEssentials.a \
"

FILES:${PN}-foundation-icu = "\
    ${libdir}/swift/linux/lib_FoundationICU.so \
"

FILES:${PN}-foundation-icu-dev = "\
    ${libdir}/swift/_foundation_unicode/* \
    ${libdir}/swift/linux/${SWIFT_HOST_ARCH}/FoundationICU.swiftmodule/* \
"

FILES:${PN}-testing = "\
    ${libdir}/swift/linux/libTesting.so \
"

FILES:${PN}-testing-dev = "\
    ${libdir}/swift/linux/Testing.swiftmodule \
    ${libdir}/swift/linux/Testing.swiftdoc \
    ${libdir}/swift/linux/Testing.swiftinterface \
"

FILES:${PN}-xctest = "\
    ${libdir}/swift/linux/libXCTest.so \
"

FILES:${PN}-xctest-dev = "\
    ${libdir}/swift/linux/XCTest.swiftmodule \
    ${libdir}/swift/linux/XCTest.swiftdoc \
"

FILES:${PN}-libdispatch = "\
    ${libdir}/swift/linux/libdispatch.so \
    ${libdir}/swift/linux/libswiftDispatch.so \
    ${libdir}/swift/linux/libBlocksRuntime.so \
"

FILES:${PN}-libdispatch-dev = "\
    ${libdir}/swift/linux/${SWIFT_TARGET_ARCH}/Dispatch.swiftdoc \
    ${libdir}/swift/linux/${SWIFT_TARGET_ARCH}/Dispatch.swiftmodule \
    ${libdir}/dispatch \
    ${libdir}/Block \
"

FILES:${PN}-libdispatch-staticdev = "\
    ${libdir}/swift_static/linux/libdispatch.a \
    ${libdir}/swift_static/linux/libswiftDispatch.a \
    ${libdir}/swift_static/linux/libBlocksRuntime.a \
    ${libdir}/swift_static/linux/libDispatchStubs.a \
    ${libdir}/swift_static/linux/${SWIFT_TARGET_ARCH}/Dispatch.swiftdoc \
    ${libdir}/swift_static/linux/${SWIFT_TARGET_ARCH}/Dispatch.swiftmodule \
    ${libdir}/swift_static/linux/dispatch \
    ${libdir}/swift_static/linux/Block \
"
