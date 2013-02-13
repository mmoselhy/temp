DESCRIPTION = "A collection of utilities and DSOs to handle compiled objects."
HOMEPAGE = "https://fedorahosted.org/elfutils"
SECTION = "base"
LICENSE = "(GPL-2+ & Elfutils-Exception)"
LIC_FILES_CHKSUM = "file://COPYING;md5=0636e73ff0215e8d672dc4c32c317bb3\
                    file://EXCEPTION;md5=570adcb0c1218ab57f2249c67d0ce417"
DEPENDS = "libtool bzip2 zlib virtual/libintl"

PR = "r9"

SRC_URI = "https://fedorahosted.org/releases/e/l/elfutils/elfutils-${PV}.tar.bz2"

SRC_URI[md5sum] = "a0bed1130135f17ad27533b0034dba8d"
SRC_URI[sha256sum] = "8aebfa4a745db21cf5429c9541fe482729b62efc7e53e9110151b4169fe887da"

# pick the patch from debian
# http://ftp.de.debian.org/debian/pool/main/e/elfutils/elfutils_0.148-1.debian.tar.gz

SRC_URI += "\
        file://redhat-portability.diff \
        file://redhat-robustify.diff \
        file://hppa_backend.diff \
        file://arm_backend.diff \
        file://mips_backend.diff \
        file://m68k_backend.diff \
        file://do-autoreconf.diff \
        file://testsuite-ignore-elflint.diff \
        file://elf_additions.diff \
	file://elfutils-fsize.patch \
	file://remove-unused.patch \
	file://mempcpy.patch \
	file://fix_for_gcc-4.7.patch \
	file://dso-link-change.patch \
"
# Only apply when building uclibc based target recipe
SRC_URI_append_libc-uclibc = " file://uclibc-support.patch"

# The buildsystem wants to generate 2 .h files from source using a binary it just built,
# which can not pass the cross compiling, so let's work around it by adding 2 .h files
# along with the do_configure_prepend()

SRC_URI += "\
        file://i386_dis.h \
        file://x86_64_dis.h \
"
inherit autotools gettext

EXTRA_OECONF = "--program-prefix=eu- --without-lzma"
EXTRA_OECONF_append_virtclass-native = " --without-bzlib"
EXTRA_OECONF_append_libc-uclibc = " --enable-uclibc"

do_configure_prepend() {
	sed -i 's:./i386_gendis:echo\ \#:g' ${S}/libcpu/Makefile.am

	cp ${WORKDIR}/*dis.h ${S}/libcpu
}

# we can not build complete elfutils when using uclibc
# but some recipes e.g. gcc 4.5 depends on libelf so we
# build only libelf for uclibc case

EXTRA_OEMAKE_libc-uclibc = "-C libelf"
EXTRA_OEMAKE_virtclass-native = ""
EXTRA_OEMAKE_virtclass-nativesdk = ""

BBCLASSEXTEND = "native nativesdk"

# Package utilities separately
PACKAGES =+ "${PN}-binutils"
FILES_${PN}-binutils = "\
    ${bindir}/eu-addr2line \
    ${bindir}/eu-ld \
    ${bindir}/eu-nm \
    ${bindir}/eu-readelf \
    ${bindir}/eu-size \
    ${bindir}/eu-strip"

# Some packages have the version preceeding the .so instead properly
# versioned .so.<version>, so we need to reorder and repackage.
FILES_${PN} += "${libdir}/*-${PV}.so ${base_libdir}/*-${PV}.so"
FILES_SOLIBSDEV = "${libdir}/libasm.so ${libdir}/libdw.so ${libdir}/libelf.so"

# The package contains symlinks that trip up insane
INSANE_SKIP_${PN} = "dev-so"
