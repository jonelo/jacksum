# Adopted BLAKE3 (io.github.rctcwyvrn.blake3)

`net.jacksum.zzadopt.io.github.rctcwyvrn.blake3.Blake3` is a copy of `io.github.rctcwyvrn.blake3.Blake3` from
<https://github.com/rctcwyvrn/blake3>, a translation of the BLAKE3 reference implementation
(<https://github.com/BLAKE3-team/BLAKE3>) from Rust to Java, relocated from `io.github.rctcwyvrn.blake3`.

Jacksum uses it in `net.jacksum.algorithms.wrappers.Blake3Wrapper`, the primary implementation of
`net.jacksum.selectors.Blake3_Selector` (algorithm `blake3`), since Jacksum 3.0.0. The alternate
implementation (`-A`) is Bouncy Castle's `Blake3Digest` (see `bouncycastle/README.md`).

## Origin

| | |
|---|---|
| Project | <https://github.com/rctcwyvrn/blake3> by rctcwyvrn (Lily Lin) |
| File | `src/main/java/io/github/rctcwyvrn/blake3/Blake3.java` |
| Base | commit `420245a1c2` of May 15, 2020 ("Use differently named factory methods for normal,keyed,kdf modes ..."), the last change of the file and the state of `master` in October 2026 |
| Status | inactive since 2021 (last push August 19, 2021), not archived. Open pull requests: #3 (August 2021, commits `26644bef53` "Eliminating a memory leak" and `bdf87c4f46` "Some aesthetic changes to make it more user friendly") and #2 (junit update) |

## License

MIT license, "Copyright (c) 2020 Lily Lin", see the header of `Blake3.java` and
`src/main/resources/net/jacksum/legal/copyright.txt` (`--copyright`).

## Jacksum's modifications

Compared with commit `420245a1c2` (comments ignored, token by token), Jacksum's copy has exactly the changes
that its header lists (applied on September 29, 2021):

| Modification | Why |
|---|---|
| new method `reset()`: clears `cvStack`, sets `cvStackLen` to 0 and initializes the state with the IV again | Jacksum reuses one instance for several files. The comment "initialize cvStack again" refers to clearing the stack |
| `popStack()` decrements `cvStackLen` first, then reads the entry and sets it to `null` (the original code stays commented out) | releases the chaining values that are no longer needed. This is the memory leak fix of PR #3 (`26644bef53`), but corrected: that commit read and cleared the entry *before* decrementing, i.e. one position too far, which leads to a `NullPointerException` later (the PR's second commit `bdf87c4f46` reverted the change) |
| `newKeyedHasher()` checks `key.length != KEY_LEN` and throws an `IllegalArgumentException` instead of an `IllegalStateException` | from PR #3; a wrong key is an illegal argument |
| import of `java.security.MessageDigest`, Javadoc, header comment with the license and the list of changes | documentation |
| package `net.jacksum.zzadopt.io.github.rctcwyvrn.blake3` instead of `io.github.rctcwyvrn.blake3` | no clash with the original on the classpath |

The other changes of PR #3 (`update()` returns `this`, `KEY_LEN` is `public`) are not included.

Jacksum's fix of 4.0.1 (an `update()` with an offset other than 0 hashed the wrong bytes, because
`Arrays.copyOfRange()` got the length instead of the end index) is in the wrapper `Blake3Wrapper`, not in
the adopted class.

## Known limitation

`reset()` initializes the instance for the normal hash mode (IV, no flags). An instance created by
`newKeyedHasher()` or `newKeyDerivationHasher()` would lose its key or context. Jacksum creates its instances
with `newInstance()` only, so this does not matter for Jacksum.

## Verification

The official test vectors of the BLAKE3 team (`test_vectors/test_vectors.json` of
<https://github.com/BLAKE3-team/BLAKE3>, 35 input lengths from 0 to 102,400 bytes) pass with Jacksum's copy
(October 2026), each case checked five times:

- hash mode with the full extended output of 131 bytes, and with the default output of 32 bytes,
- hash mode with the same instance after `reset()`, fed in random pieces,
- keyed hash mode and key derivation mode (131 bytes each).

`jacksum -a blake3` and `jacksum -a blake3 -A` (Bouncy Castle) give the expected values for 0, 1023, 1025
and 102,400 bytes.
