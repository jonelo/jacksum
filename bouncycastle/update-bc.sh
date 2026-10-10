#!/usr/bin/env bash
#
# Generates src/main/java/org/bouncycastle from
#   pristine Bouncy Castle sources (Maven Central, bcprov-jdk18on-<version>-sources.jar)
#   + overlay/   (whole files that replace or add to upstream files)
#   + patches/   (unified diffs against the pristine upstream files)
# and keeps only those classes that Jacksum's own code (src/main/java/net) needs, transitively.
# The result is relocated from org.bouncycastle to net.jacksum.zzadopt.org.bouncycastle, so that it cannot
# clash with a Bouncy Castle on the same classpath. overlay/ and patches/ stay in the upstream namespace.
#
# Usage: bouncycastle/update-bc.sh [<bc-version>]     (default: bc.version from bc.properties)
#
# See bouncycastle/README.md for the rationale and the list of all Jacksum modifications.
# Requires: bash, curl, unzip, patch, perl, shasum, javac/java 21+

set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(dirname "$HERE")"
PROPS="$HERE/bc.properties"
# the generated tree lives in src/main/java/$RELOC_PREFIX/org/bouncycastle, package $RELOC_PKG
RELOC_PREFIX="net/jacksum/zzadopt"
RELOC_PKG="net.jacksum.zzadopt.org.bouncycastle"
TARGET_TREE="$ROOT/src/main/java/$RELOC_PREFIX/org/bouncycastle"
# location of the tree up to Jacksum 4.0.1 (not relocated), removed by this script
LEGACY_TREE="$ROOT/src/main/java/org/bouncycastle"

prop() { grep "^$1=" "$PROPS" | cut -d= -f2-; }
die()  { echo "ERROR: $*" >&2; exit 1; }
info() { echo ">>> $*"; }

VERSION="${1:-$(prop bc.version)}"
ARTIFACT="$(prop bc.artifact)"
TESTDATA_COMMIT="$(prop bc.testdata.commit)"
WORK="$ROOT/target/bc-update/$VERSION"
JAR="$WORK/$ARTIFACT-$VERSION-sources.jar"
URL="https://repo1.maven.org/maven2/org/bouncycastle/$ARTIFACT/$VERSION/$ARTIFACT-$VERSION-sources.jar"

# javac/java messages in English, so that the -verbose output can be parsed
JAVAC=(javac -J-Duser.language=en -J-Duser.country=US)

# ---------------------------------------------------------------------------------------------
info "1/8 Download $ARTIFACT $VERSION sources"
mkdir -p "$WORK"
if [ ! -f "$JAR" ]; then
    curl -sfL -o "$JAR.tmp" "$URL" || die "cannot download $URL"
    mv "$JAR.tmp" "$JAR"
fi
SHA1_EXPECTED="$(curl -sfL "$URL.sha1" | cut -c1-40)" || die "cannot download $URL.sha1"
SHA1_ACTUAL="$(shasum -a 1 "$JAR" | cut -c1-40)"
[ "$SHA1_EXPECTED" = "$SHA1_ACTUAL" ] || die "SHA-1 mismatch of $JAR (Maven Central: $SHA1_EXPECTED, local: $SHA1_ACTUAL)"
SHA256="$(shasum -a 256 "$JAR" | cut -c1-64)"
if [ "$VERSION" = "$(prop bc.version)" ]; then
    [ "$SHA256" = "$(prop bc.sources.sha256)" ] || die "SHA-256 of $JAR does not match bc.sources.sha256 in bc.properties"
else
    info "    new version, SHA-256 is $SHA256 (bc.properties will be updated at the end)"
fi

# ---------------------------------------------------------------------------------------------
info "2/8 Extract and patch"
rm -rf "$WORK/upstream" "$WORK/patched" "$WORK/staged" "$WORK/relocated" "$WORK/classes" "$WORK/verify-classes"
mkdir -p "$WORK/upstream"
unzip -q "$JAR" 'org/bouncycastle/*' -d "$WORK/upstream"
cp -R "$WORK/upstream" "$WORK/patched"
for p in "$HERE"/patches/*.patch; do
    [ -e "$p" ] || continue
    if ! (cd "$WORK/patched" && patch -p1 -N -s --dry-run < "$p" > /dev/null); then
        die "patch $(basename "$p") does not apply to BC $VERSION. Check whether the bug has been fixed upstream (then delete the patch), otherwise rebase the patch."
    fi
    (cd "$WORK/patched" && patch -p1 -N -s < "$p")
    info "    applied $(basename "$p")"
done
find "$WORK/patched" -name '*.orig' -delete

# ---------------------------------------------------------------------------------------------
info "3/8 Apply the overlay and relocate to $RELOC_PKG"
cp -R "$WORK/patched" "$WORK/staged"
cp -R "$HERE/overlay/." "$WORK/staged/"
mkdir -p "$WORK/relocated/$RELOC_PREFIX/org"
mv "$WORK/staged/org/bouncycastle" "$WORK/relocated/$RELOC_PREFIX/org/bouncycastle"
# org.bouncycastle -> net.jacksum.zzadopt.org.bouncycastle in package, import, Javadoc and fully qualified
# references (but not in "www.bouncycastle.org")
find "$WORK/relocated" -name '*.java' -print0 \
    | xargs -0 perl -pi -e 's/(?<![\w.])org\.bouncycastle(?=[.;\s])/'"$RELOC_PKG"'/g'

# ---------------------------------------------------------------------------------------------
info "4/8 Determine the required classes (javac -implicit:class over src/main/java/net)"
# Jacksum's own sources, without the generated tree (otherwise all of it would be "required")
find "$ROOT/src/main/java/net" -path "$TARGET_TREE" -prune -o -name '*.java' -print > "$WORK/net-sources.txt"
mkdir -p "$WORK/classes"
"${JAVAC[@]}" --release 21 -nowarn -Xlint:none -proc:none -implicit:class -verbose \
    -sourcepath "$WORK/relocated" -d "$WORK/classes" @"$WORK/net-sources.txt" \
    > "$WORK/javac.log" 2>&1 || {
    # a class that javac cannot find, but that is in the current tree (it is replaced in step 7 only),
    # has been removed upstream while Jacksum still needs it
    OLD_TREE="$TARGET_TREE"
    [ -d "$OLD_TREE" ] || OLD_TREE="$LEGACY_TREE"
    removed=""
    for name in $(sed -nE 's/^ *symbol: +class ([A-Za-z0-9_]+).*/\1/p' "$WORK/javac.log" | sort -u); do
        [ -d "$OLD_TREE" ] || break
        for rel in $(cd "$OLD_TREE" && find . -name "$name.java" | sed 's|^\./|org/bouncycastle/|'); do
            [ -f "$HERE/overlay/$rel" ] || [ -f "$WORK/patched/$rel" ] || removed="$removed $rel"
        done
    done
    if [ -n "$removed" ]; then
        echo "These classes have been removed in BC $VERSION, but Jacksum still needs them:"
        for rel in $removed; do echo "    $rel"; done
        echo "Either adapt Jacksum's code, or pin each class from the current tree into the overlay"
        echo "(the overlay is in the upstream namespace, so the copy is relocated back to org.bouncycastle):"
        for rel in $removed; do
            old="${OLD_TREE#"$ROOT/"}/${rel#org/bouncycastle/}"
            echo "    mkdir -p bouncycastle/overlay/$(dirname "$rel") && sed 's/net\.jacksum\.zzadopt\.org\.bouncycastle/org.bouncycastle/g' $old > bouncycastle/overlay/$rel"
        done
        echo "add a JACKSUM-MOD: header to each pinned file and run the update again. If javac then misses further"
        echo "(e.g. package-private base) classes, pin them in the same way. See bouncycastle/README.md."
        die "Jacksum does not compile against BC $VERSION, because of removed classes (see $WORK/javac.log)"
    fi
    tail -30 "$WORK/javac.log"; die "Jacksum does not compile against BC $VERSION (see $WORK/javac.log)"; }

# every source file that javac has parsed from the relocated tree is required, javac logs them as
# "[parsing started DirectoryFileObject[<sourcepath-dir>:<relative-path>]]"; files.txt lists them with
# their upstream path (org/bouncycastle/...), which is also the path in overlay/ and patches/
grep -o '\[parsing started DirectoryFileObject\[[^]]*\]' "$WORK/javac.log" \
    | sed -E 's/^\[parsing started DirectoryFileObject\[//; s/\]$//' | sort -u | while read -r f; do
    dir="${f%%:*}"
    rel="${f#*:}"
    [ "$dir" = "$WORK/relocated" ] || die "unexpected source file $f"
    rel="${rel#"$RELOC_PREFIX/"}"
    if [ -f "$HERE/overlay/$rel" ]; then
        echo "overlay  $rel"
    elif cmp -s "$WORK/upstream/$rel" "$WORK/patched/$rel"; then
        echo "upstream $rel"
    else
        echo "patched  $rel"
    fi
done | sort -k2 > "$WORK/files.txt"
COUNT="$(wc -l < "$WORK/files.txt" | tr -d ' ')"
[ "$COUNT" -gt 0 ] || die "no Bouncy Castle sources found in the javac output, see $WORK/javac.log"
info "    $COUNT source files required"

# every overlay file must be used, otherwise it is obsolete
for f in $(cd "$HERE/overlay" && find . -name '*.java' | sed 's|^\./||'); do
    grep -q " $f\$" "$WORK/files.txt" || echo "WARNING: overlay/$f is not used anymore by Jacksum, consider to delete it"
done

# ---------------------------------------------------------------------------------------------
info "5/8 Sanity checks"
if grep -qE ' org/bouncycastle/(asn1|math/ec|jcajce|jce|pqc)/' "$WORK/files.txt"; then
    grep -E ' org/bouncycastle/(asn1|math/ec|jcajce|jce|pqc)/' "$WORK/files.txt" | head -10
    die "the stripped tree pulls in heavy packages, probably a new dependency on BC's infrastructure that the overlay does not cover yet"
fi
while read -r origin rel; do
    if unzip -l "$JAR" "META-INF/versions/*/$rel" > /dev/null 2>&1; then
        echo "WARNING: $rel has a multi-release variant in META-INF/versions, only the base version is used"
    fi
    # a deprecated class will probably be removed upstream some day (overlay files are pinned on purpose)
    if [ "$origin" != overlay ] && awk '/@deprecated|@Deprecated/ { d = 1 }
            /^(public |abstract |final )*(class|interface|enum) / { exit }
            END { exit d ? 0 : 1 }' "$WORK/patched/$rel"; then
        echo "NOTE: Jacksum uses the deprecated BC class $rel, it may be removed in a future BC version (see bouncycastle/README.md)"
    fi
    # relocation must not change string literals (e.g. property or class names)
    if grep -n "\"$RELOC_PKG" "$WORK/relocated/$RELOC_PREFIX/$rel"; then
        die "$rel contains a string literal with org.bouncycastle, the relocation would change its meaning"
    fi
done < "$WORK/files.txt"

# ---------------------------------------------------------------------------------------------
info "6/8 Known answer tests (LWC hash KATs from bcgit/bc-test-data@${TESTDATA_COMMIT:0:10})"
KAT="$ROOT/target/bc-update/kat-$TESTDATA_COMMIT"
mkdir -p "$KAT"
for p in ascon/asconhash_LWC_HASH_KAT_256.txt ascon/asconhasha_LWC_HASH_KAT_256.txt \
         ascon/asconxof_LWC_HASH_KAT_256.txt ascon/asconxofa_LWC_HASH_KAT_256.txt \
         photonbeetle/LWC_HASH_KAT_256.txt sparkle/LWC_HASH_KAT_256.txt sparkle/LWC_HASH_KAT_384.txt \
         xoodyak/LWC_HASH_KAT_256.txt; do
    f="$KAT/$(echo "$p" | tr / _)"
    [ -f "$f" ] || curl -sfL -o "$f" "https://raw.githubusercontent.com/bcgit/bc-test-data/$TESTDATA_COMMIT/crypto/$p" \
        || die "cannot download KAT $p"
done
mkdir -p "$WORK/verify-classes"
"${JAVAC[@]}" --release 21 -nowarn -Xlint:none -cp "$WORK/classes" -d "$WORK/verify-classes" "$HERE/verify/LwcKat.java" 2>&1 \
    | grep -v '^Note:' || true
java -cp "$WORK/classes:$WORK/verify-classes" LwcKat "$KAT" || die "known answer tests failed, the source tree has NOT been replaced"

# ---------------------------------------------------------------------------------------------
info "7/8 Replace $TARGET_TREE"
rm -rf "$TARGET_TREE"
while read -r origin rel; do
    mkdir -p "$ROOT/src/main/java/$RELOC_PREFIX/$(dirname "$rel")"
    cp "$WORK/relocated/$RELOC_PREFIX/$rel" "$ROOT/src/main/java/$RELOC_PREFIX/$rel"
done < "$WORK/files.txt"
# BC's license must ship with the code
cp "$WORK/relocated/$RELOC_PREFIX/org/bouncycastle/LICENSE.java" "$TARGET_TREE/LICENSE.java"
# the tree up to Jacksum 4.0.1 was not relocated
if [ -d "$LEGACY_TREE" ]; then
    rm -rf "$LEGACY_TREE"
    rmdir "$ROOT/src/main/java/org" 2>/dev/null || true
    info "    removed the old, not relocated tree src/main/java/org/bouncycastle"
fi
grep -q ' org/bouncycastle/LICENSE.java$' "$WORK/files.txt" || echo "upstream org/bouncycastle/LICENSE.java" >> "$WORK/files.txt"

{
    echo "# Generated by bouncycastle/update-bc.sh - do not edit."
    echo "# Bouncy Castle $VERSION ($ARTIFACT), relocated to src/main/java/$RELOC_PREFIX/org/bouncycastle (package $RELOC_PKG)."
    echo "# Origin of each file, by its upstream path:"
    echo "#   upstream = pristine BC file, patched = BC file + patches/*.patch, overlay = file from overlay/"
    sort -k2 "$WORK/files.txt"
} > "$HERE/files.txt"

# ---------------------------------------------------------------------------------------------
info "8/8 Update bc.properties"
sed -i.bak -e "s/^bc.version=.*/bc.version=$VERSION/" -e "s/^bc.sources.sha256=.*/bc.sources.sha256=$SHA256/" "$PROPS"
rm -f "$PROPS.bak"

echo
info "Done: $(wc -l < "$WORK/files.txt" | tr -d ' ') files from Bouncy Castle $VERSION" \
     "($(grep -c '^overlay' "$WORK/files.txt" || true) overlay, $(grep -c '^patched' "$WORK/files.txt" || true) patched)."
echo "Next steps:"
echo "  1. mvn clean package   (clean is required, an incremental build after the update can be broken)"
echo "  2. bouncycastle/verify/compare-jars.sh <old-jacksum.jar> target/jacksum-<version>.jar"
echo "  3. git diff --stat -- src/main/java/$RELOC_PREFIX/org/bouncycastle bouncycastle/files.txt"
echo "  4. document the update in RELEASE-NOTES.txt"
