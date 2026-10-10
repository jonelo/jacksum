#!/usr/bin/env bash
#
# Generates src/main/java/org/bouncycastle from
#   pristine Bouncy Castle sources (Maven Central, bcprov-jdk18on-<version>-sources.jar)
#   + overlay/   (whole files that replace or add to upstream files)
#   + patches/   (unified diffs against the pristine upstream files)
# and keeps only those classes that Jacksum's own code (src/main/java/net) needs, transitively.
#
# Usage: bouncycastle/update-bc.sh [<bc-version>]     (default: bc.version from bc.properties)
#
# See bouncycastle/README.md for the rationale and the list of all Jacksum modifications.
# Requires: bash, curl, unzip, patch, shasum, javac/java 21+

set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(dirname "$HERE")"
PROPS="$HERE/bc.properties"
TARGET_TREE="$ROOT/src/main/java/org/bouncycastle"

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
info "1/7 Download $ARTIFACT $VERSION sources"
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
info "2/7 Extract and patch"
rm -rf "$WORK/upstream" "$WORK/patched" "$WORK/classes" "$WORK/verify-classes"
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
info "3/7 Determine the required classes (javac -implicit:class over src/main/java/net)"
find "$ROOT/src/main/java/net" -name '*.java' > "$WORK/net-sources.txt"
mkdir -p "$WORK/classes"
"${JAVAC[@]}" --release 21 -nowarn -Xlint:none -proc:none -implicit:class -verbose \
    -sourcepath "$HERE/overlay:$WORK/patched" -d "$WORK/classes" @"$WORK/net-sources.txt" \
    > "$WORK/javac.log" 2>&1 || { tail -30 "$WORK/javac.log"; die "Jacksum does not compile against BC $VERSION (see $WORK/javac.log)"; }

# every source file that javac has parsed from the overlay or the patched upstream tree is required,
# javac logs them as "[parsing started DirectoryFileObject[<sourcepath-dir>:<relative-path>]]"
grep -o '\[parsing started DirectoryFileObject\[[^]]*\]' "$WORK/javac.log" \
    | sed -E 's/^\[parsing started DirectoryFileObject\[//; s/\]$//' | sort -u | while read -r f; do
    dir="${f%%:*}"
    rel="${f#*:}"
    case "$dir" in
        "$HERE/overlay") echo "overlay  $rel" ;;
        "$WORK/patched")
            if cmp -s "$WORK/upstream/$rel" "$WORK/patched/$rel"; then echo "upstream $rel"; else echo "patched  $rel"; fi ;;
        *) die "unexpected source file $f" ;;
    esac
done | sort -k2 > "$WORK/files.txt"
COUNT="$(wc -l < "$WORK/files.txt" | tr -d ' ')"
[ "$COUNT" -gt 0 ] || die "no Bouncy Castle sources found in the javac output, see $WORK/javac.log"
info "    $COUNT source files required"

# every overlay file must be used, otherwise it is obsolete
for f in $(cd "$HERE/overlay" && find . -name '*.java' | sed 's|^\./||'); do
    grep -q " $f\$" "$WORK/files.txt" || echo "WARNING: overlay/$f is not used anymore by Jacksum, consider to delete it"
done

# ---------------------------------------------------------------------------------------------
info "4/7 Sanity checks"
if grep -qE ' org/bouncycastle/(asn1|math/ec|jcajce|jce|pqc)/' "$WORK/files.txt"; then
    grep -E ' org/bouncycastle/(asn1|math/ec|jcajce|jce|pqc)/' "$WORK/files.txt" | head -10
    die "the stripped tree pulls in heavy packages, probably a new dependency on BC's infrastructure that the overlay does not cover yet"
fi
while read -r origin rel; do
    if unzip -l "$JAR" "META-INF/versions/*/$rel" > /dev/null 2>&1; then
        echo "WARNING: $rel has a multi-release variant in META-INF/versions, only the base version is used"
    fi
done < "$WORK/files.txt"

# ---------------------------------------------------------------------------------------------
info "5/7 Known answer tests (LWC hash KATs from bcgit/bc-test-data@${TESTDATA_COMMIT:0:10})"
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
info "6/7 Replace $TARGET_TREE"
rm -rf "$TARGET_TREE"
while read -r origin rel; do
    case "$origin" in
        overlay) src="$HERE/overlay/$rel" ;;
        *)       src="$WORK/patched/$rel" ;;
    esac
    mkdir -p "$ROOT/src/main/java/$(dirname "$rel")"
    cp "$src" "$ROOT/src/main/java/$rel"
done < "$WORK/files.txt"
# BC's license must ship with the code
cp "$WORK/upstream/org/bouncycastle/LICENSE.java" "$TARGET_TREE/LICENSE.java"
grep -q ' org/bouncycastle/LICENSE.java$' "$WORK/files.txt" || echo "upstream org/bouncycastle/LICENSE.java" >> "$WORK/files.txt"

{
    echo "# Generated by bouncycastle/update-bc.sh - do not edit."
    echo "# Bouncy Castle $VERSION ($ARTIFACT), origin of each file in src/main/java/org/bouncycastle:"
    echo "#   upstream = pristine BC file, patched = BC file + patches/*.patch, overlay = file from overlay/"
    sort -k2 "$WORK/files.txt"
} > "$HERE/files.txt"

# ---------------------------------------------------------------------------------------------
info "7/7 Update bc.properties"
sed -i.bak -e "s/^bc.version=.*/bc.version=$VERSION/" -e "s/^bc.sources.sha256=.*/bc.sources.sha256=$SHA256/" "$PROPS"
rm -f "$PROPS.bak"

echo
info "Done: $(wc -l < "$WORK/files.txt" | tr -d ' ') files from Bouncy Castle $VERSION" \
     "($(grep -c '^overlay' "$WORK/files.txt" || true) overlay, $(grep -c '^patched' "$WORK/files.txt" || true) patched)."
echo "Next steps:"
echo "  1. mvn clean package   (clean is required, an incremental build after the update can be broken)"
echo "  2. bouncycastle/verify/compare-jars.sh <old-jacksum.jar> target/jacksum-<version>.jar"
echo "  3. git diff --stat -- src/main/java/org/bouncycastle bouncycastle/files.txt"
echo "  4. document the update in RELEASE-NOTES.txt"
