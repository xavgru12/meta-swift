SUMMARY = "swift-testing"
DESCRIPTION = "A package with expressive and intuitive APIs that make testing your Swift code a breeze."
HOMEPAGE = "https://github.com/swiftlang/swift-testing"

#DEPENDS += "swift-native"

SWIFT_BUILD_TESTS = "0"

LICENSE = "Apache-2.0" 
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=9426349f482bb39d6a4a080793545176"

require swift-version.inc
PV = "${SWIFT_VERSION}+git${SRCPV}"
SRCREV_FORMAT = "swift_testing"

EXTRA_OESWIFT = "-Xswiftc -enable-testing"

SRC_URI = "git://github.com/swiftlang/swift-testing.git;protocol=https;tag=${SWIFT_TAG};nobranch=1"
SRC_URI += "file://0001-build-as-dynamic-library.patch;striplevel=1;"

S = "${UNPACKDIR}/git"
B = "${WORKDIR}/build"

inherit swift

# sources/meta-swift/classes/swift.bbclass

python () {
    import subprocess
    import os

    # 1. Get the absolute path via TMPDIR
    tmpdir = d.getVar('TMPDIR')
    if not tmpdir:
        return

    swift_native_base = os.path.join(tmpdir, "work/x86_64-linux/swift-native")
    
    # 2. Find the lib directory - EXCLUDING the 'image' directory to avoid path nesting
    find_cmd = f"find {swift_native_base} -maxdepth 6 -not -path '*/image/*' -type d -path '*/recipe-sysroot-native/usr/lib' 2>/dev/null | head -n 1"
    
    try:
        swift_lib_dir = subprocess.check_output(find_cmd, shell=True).decode('utf-8').strip()
        
        if swift_lib_dir:
            # 3. Inject it into the environment for ALL tasks in this recipe
            # Get current value to avoid redundant appending
            current_ld_path = d.getVar('LD_LIBRARY_PATH') or ""
            
            if swift_lib_dir not in current_ld_path:
                d.appendVar('LD_LIBRARY_PATH', f":{swift_lib_dir}")
            
            # This 'exports' it so the shell environment sees it
            d.setVarFlag('LD_LIBRARY_PATH', 'export', '1')
            
            bb.note(f"swift.bbclass: Global LD_LIBRARY_PATH injection: {swift_lib_dir}")
    except Exception as e:
        bb.debug(1, f"swift.bbclass: Could not find swift-native libs: {e}")
}

do_install() {
    install -d ${D}${libdir}/swift/linux

    install -m 0644 ${BUILD_DIR}/libTesting.so ${D}${libdir}/swift/linux
    install -m 0644 ${BUILD_DIR}/lib_TestingInterop.so ${D}${libdir}/swift/linux
    cp -r ${BUILD_DIR}/Modules/Testing.swiftmodule ${D}${libdir}/swift/linux/
    if [ -f ${BUILD_DIR}/Modules/Testing.swiftdoc ]; then
        install -m 0644 ${BUILD_DIR}/Modules/Testing.swiftdoc ${D}${libdir}/swift/linux
    fi
    if [ -f ${BUILD_DIR}/Modules/Testing.swiftinterface ]; then
        install -m 0644 ${BUILD_DIR}/Modules/Testing.swiftinterface ${D}${libdir}/swift/linux
    fi

    rm -f ${BUILD_DIR}/Modules/*.swiftsourceinfo
}

FILES:${PN} = "\
    ${libdir}/swift/linux/libTesting.so \
    ${libdir}/swift/linux/lib_TestingInterop.so \
"

FILES:${PN}-dev = "\
    ${libdir}/swift/linux/Testing.swiftmodule \
    ${libdir}/swift/linux/Testing.swiftdoc \
    ${libdir}/swift/linux/Testing.swiftinterface \
"

INSANE_SKIP:${PN} = "buildpaths"
INSANE_SKIP:${PN}-dbg = "buildpaths"
