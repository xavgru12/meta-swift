DESCRIPTION = "Swift Hello World!"
LICENSE = "CLOSED"

SWIFT_BUILD_TESTS = "0"

#RDEPENDS:${PN} += "swift-xctest swift-testing"

SRC_URI = "\
    file://Package.swift \
    file://Sources \
"

S = "${SWIFT_UNPACKDIR}"
B = "${WORKDIR}/build"

inherit swift

python () {
    import subprocess
    import os

    tmpdir = d.getVar('TMPDIR')
    if not tmpdir:
        return

    # 1. Root of the native swift build
    swift_native_base = os.path.join(tmpdir, "work/x86_64-linux/swift-native")
    
    # 2. Find the lib directory (ignoring the nested 'image' folder)
    find_cmd = f"find {swift_native_base} -maxdepth 6 -not -path '*/image/*' -type d -path '*/recipe-sysroot-native/usr/lib' 2>/dev/null | head -n 1"
    
    try:
        swift_lib_dir = subprocess.check_output(find_cmd, shell=True).decode('utf-8').strip()
        
        if swift_lib_dir and os.path.exists(swift_lib_dir):
            # 3. Get existing path
            current_ld_path = d.getVar('LD_LIBRARY_PATH') or ""
            
            # 4. PREPEND the path. This is vital for libIndexStore.so
            if swift_lib_dir not in current_ld_path:
                new_path = f"{swift_lib_dir}:{current_ld_path}".strip(':')
                d.setVar('LD_LIBRARY_PATH', new_path)
            
            # 5. Force the variable to be exported to all shell tasks (do_compile, etc.)
            d.setVarFlag('LD_LIBRARY_PATH', 'export', '1')
            
            bb.note(f"swift.bbclass: Successfully prepended Swift native libs: {swift_lib_dir}")
    except Exception as e:
        bb.debug(1, f"swift.bbclass: Library discovery failed: {e}")
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${BUILD_DIR}/hello-world ${D}${bindir}
}

INSANE_SKIP:${PN} = "buildpaths"
INSANE_SKIP:${PN}-dbg = "buildpaths"
