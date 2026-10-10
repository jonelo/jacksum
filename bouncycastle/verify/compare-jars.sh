#!/usr/bin/env bash
#
# Compares two Jacksum jars (e.g. before and after a Bouncy Castle update): computes all algorithms
# (-a all) over inputs of critical lengths (block boundaries, > 64 KiB, ...), a few HMACs, --info of
# the BC based XOFs/LWC hashes and the --hmacs list, and diffs the output.
#
# Usage: bouncycastle/verify/compare-jars.sh <old.jar> <new.jar>

set -euo pipefail
[ $# -eq 2 ] || { echo "Usage: $0 <old.jar> <new.jar>" >&2; exit 2; }
OLD="$1"; NEW="$2"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

mkdir -p "$WORK/in"
: > "$WORK/in/f0"
for n in 1 3 16 63 64 127 128 129 511 512 513 1007 4097 65537 200003 1048579; do
    head -c "$n" /dev/urandom > "$WORK/in/f$n"
done

run() {
    local jar="$1"
    for f in $(ls "$WORK/in" | sort -t f -k2 -n); do
        echo "## $f"
        java -jar "$jar" -a all -F "#ALGONAME{i} #HASH{i}" "$WORK/in/$f" 2>&1
    done
    for a in sha3-256 blake2b-512 blake2s-256 streebog512 sm3 kupyna-256 skein-512-512 tiger-192-4-php ripemd320; do
        echo "## hmac $a"
        java -jar "$jar" -a "hmac:$a" -k hex:00112233445566778899 -F "#HASH" "$WORK/in/f1007" 2>&1
    done
    for a in kangarootwelve marsupilamifourteen esch256 esch384 xoodyak photon-beetle ascon-hash ascon-xof blake2bp blake2sp blake3; do
        echo "## info $a"
        java -jar "$jar" --info -a "$a" 2>&1
    done
    echo "## hmacs"
    java -jar "$jar" --hmacs 2>&1
}

run "$OLD" > "$WORK/old.txt"
run "$NEW" > "$WORK/new.txt"
echo "$(grep -vc '^##' "$WORK/old.txt") lines compared"
if diff "$WORK/old.txt" "$WORK/new.txt"; then
    echo "IDENTICAL"
else
    echo "DIFFERENCES found (< old, > new)"
    exit 1
fi
