**Table of Contents**
 - [What This Guide Answers](#what_this_guide_answers)
 - [Vocabulary](#vocabulary)
 - [The Avalanche Effect](#avalanche)
 - [Three Families, Three Jobs](#three_families)
 - [Where CRCs Still Belong](#crcs)
 - [What Makes an Algorithm Cryptographic](#cryptographic)
 - [What "Broken" Means](#broken_means)
   - [Length Extension](#length_extension)
 - [How Much Scrutiny Has the Design Had?](#scrutiny)
 - [Concatenating Algorithms](#concatenation)
 - [Speed, and Why It Is Usually the Wrong Tie-Breaker](#speed)
 - [Two Implementations Behind One Algorithm ID](#implementations)
 - [The Operating System Constraint](#os_constraint)
 - [Algorithms Not to Use Any More](#blacklist)
   - [Tier A: Too Narrow](#tier_a)
   - [Tier B: Defective by Construction](#tier_b)
   - [Tier C: Broken Cryptographic Hash Functions](#tier_c)
   - [Same Name, Different Variant](#variants)
 - [Why a Broken Algorithm Can Still Be Used: HMAC](#hmac)
 - [The Decision Procedure](#decision)
 - [Worked Example: Thousands of Files](#workflow)
 - [Anti-Patterns](#antipatterns)
 - [Cheat Sheet](#cheatsheet)
 - [Footnotes and Further Reading](#footnotes)


<a name="what_this_guide_answers"></a>

# What This Guide Answers

You have a disk with thousands of files. You want to record a fingerprint for every one of
them today, so that months or years from now you can prove that nothing has changed. Which of
Jacksum's 586 algorithms do you choose?

This guide is about making that decision deliberately instead of by habit. It explains the
concepts you need (hash function, CRC, avalanche effect, collision resistance, what "broken"
means), it names the algorithms you should stop using, and it shows how to get Jacksum to
*tell you* what you need to know rather than trusting a table in a blog post.

**The short answer**, if you only read one paragraph:

| Situation | Choose |
|---|---|
| An adversary is possible, and other tools must be able to verify the list | `sha-256` |
| An adversary is possible, and Jacksum runs on both ends | `sha3-256` |
| Long-term archive, the data outlives the algorithm | `sha256+sha3-256` |
| You want more margin than 256 bits | `sha-512/256` or `sha-512` |
| The check list itself is stored somewhere an attacker could reach | `hmac:sha256` with `-k` |
| Only accidental corruption matters, or a protocol dictates the value | `crc32c`, `crc64_nvme` |

Everything after this table is the reasoning, so that you can defend the choice, adapt it when
your constraints differ, and recognise when someone else's advice is out of date.

Two things this guide deliberately does **not** do. It does not rank algorithms by a single
"strength" score, because the right choice depends on your threat model and on who has to
verify the result. And it does not ask you to trust its numbers: every figure quoted here can
be reproduced with the Jacksum commands shown next to it.

See also

- [Algorithms](ALGORITHMS.md) — the full list of what Jacksum supports and where it comes from
- [Features](FEATURES.md), [Examples](EXAMPLES.md), [Jacksum Hacks](JACKSUM_HACKS.md)
- [Manpage](https://github.com/jonelo/jacksum/wiki/Manpage#algorithms)


<a name="vocabulary"></a>

# Vocabulary

A handful of terms carry the whole argument. They are worth pinning down precisely, because
most bad algorithm choices come from conflating two of them.

**Message.** The input. For our purposes, the bytes of a file. Jacksum can also hash strings,
stdin, disks, and NTFS alternate data streams, but the reasoning is the same.

**Hash function.** A function that maps a message of any length to a value of fixed length.
"Any length" and "fixed length" together are what makes the interesting properties possible
and the problems unavoidable: there are infinitely many messages and only finitely many
outputs.

**Hash value.** The output. Also called *digest*, *fingerprint*, *checksum* or *check value*
depending on tradition. Jacksum uses "hash value" throughout, and lets you print it in 17
encodings with `-E` (hex, Base32, Base64, BubbleBabble, z-base-32, decimal, and more).

**Collision.** Two different messages with the same hash value. Collisions always exist — the
pigeonhole principle guarantees it. The question is only whether anyone can *find* one.

**Collision resistance.** It should be infeasible to find *any* pair of messages that collide.
The attacker gets to choose both messages, which is what makes this the easiest of the three
properties to break.

**Preimage resistance.** Given a hash value, it should be infeasible to find *a* message that
produces it. This is what "one-way function" means.

**Second preimage resistance.** Given a message, it should be infeasible to find a *different*
message with the same hash value. This is the property that matters most for file integrity:
the attacker has your file and wants a different file that passes your check.

**Block size.** Iterated hash functions process the message in fixed-size blocks. Jacksum
reports it under `Block size:` in `--info`. It matters for HMAC, which requires the block size
to exceed the output size.

**Merkle–Damgård construction.** The classic design: chop the message into blocks, and feed
each block plus the running state into a compression function. MD5, SHA-1 and the whole SHA-2
family work this way. It is simple and well understood, and it has one structural quirk — see
[length extension](#length_extension).

**Sponge construction.** The SHA-3 design: absorb the message into a large internal state, then
squeeze the output out of it. The state is bigger than the output, which removes the length
extension quirk and makes variable output lengths natural (that is what an XOF is).

**Hash tree (Merkle tree).** Hash the leaves, then hash the hashes, up to a single root.
Jacksum offers TTH and TTH2; BLAKE3 uses a tree mode internally. Trees allow parallelism and
partial verification. The tree mode itself adds no weakness, so the result is exactly as strong
as the underlying hash function.

**CRC (Cyclic Redundancy Check).** Not a hash function in the cryptographic sense, but the
remainder of a polynomial division over GF(2). The message is interpreted as a polynomial and
divided by a fixed *generator polynomial*; the remainder is the CRC. This algebraic structure
is exactly why CRCs are excellent at detecting the error patterns they were designed for, and
exactly why they are worthless against an attacker.

**Rocksoft (tm) Model.** The six-parameter description that pins down a CRC completely: width,
polynomial, initial value, whether input and output bits are reflected, and a final XOR value.
Jacksum implements it, so `-a crc:<width>,<poly>,<init>,<refIn>,<refOut>,<xorOut>` lets you
reproduce any CRC you encounter. Two optional parameters go beyond the classic model and mix
the message length into the value. See `jacksum -h crc:` and
[ALGORITHMS.md](ALGORITHMS.md#customizable_crcs).

**Classic checksum.** The pre-CRC family: add up the bytes (`sum8` … `sum64`), XOR them
(`xor8`), or a slightly cleverer variant with two running sums (Adler-32, Fletcher). Cheap,
and — as we will see — often broken in ways that matter even without an attacker.

**Cryptographic hash function.** A hash function that additionally claims preimage, second
preimage and collision resistance against an *intelligent adversary who knows the algorithm*.
That last clause is the whole difference from a CRC or checksum, and Jacksum records it in the
`type:` field of every algorithm's documentation.

**HMAC.** A keyed construction built on a hash function. Not a hash of the key concatenated
with the message, but a specific nested scheme (RFC 2104 / FIPS 198-1) whose security rests on
different assumptions than plain hashing — which is why it survives some broken hashes.


<a name="avalanche"></a>

# The Avalanche Effect

A good hash function behaves like a random function: flip one bit of the input and every output
bit should flip with probability 1/2, independently. That property is called the **avalanche
effect** (or strict avalanche criterion), and the target value is therefore **50 %** — not
100 %. A function where all output bits flip every time would be just as predictable as one
where none do.

Jacksum measures it for you. `--info --verbose details` (or `-V details`) prints an avalanche
block:

```
$ jacksum -a sha-256 --info --verbose details
```

```
  Avalanche effect:
    input length in bytes:                9
    input length in bits:                 72
    hash calculations:                    73
    input [hex]:                          313233343536373839
    input [bin]:                          001100010011001000110011001101000011010100110110001101110011100000111001
    avalanche min effect:                 42.19 %
    avalanche avg effect:                 50.04 %
    avalanche max effect:                 57.03 %
```

**How the measurement works.** Jacksum hashes the input once to get a baseline, then flips each
input bit individually and hashes again — 72 bits means 72 flips, so 73 hash calculations in
total. For each flip it computes the Hamming distance between the new hash value and the
baseline, i.e. how many output bits changed, and expresses it as a percentage of the output
width. The min, avg and max of those 72 percentages are reported. The default input is the
9-byte string `123456789`; `-q <sequence>` substitutes your own:

```
$ jacksum -a sum32 --info -V details -q txt:"Hello World"
```

**How to read it.** The average should sit close to 50 %. Just as important, the *spread*
should be narrow: a min of 42 % and a max of 57 % means no single input bit has unusually
little or unusually much influence. Compare these measured values:

| Algorithm | min | avg | max | Verdict |
|---|---|---|---|---|
| `sha-256` | 42.19 % | 50.04 % | 57.03 % | textbook |
| `sha3-256` | 42.58 % | 49.39 % | 54.69 % | textbook, tightest spread |
| `md5` | 39.84 % | 50.20 % | 61.72 % | textbook (avalanche is not what broke MD5) |
| `crc32` | 34.38 % | 45.53 % | 62.50 % | respectable for a CRC |
| `adler32` | 6.25 % | 15.80 % | — | poor |
| `sum32` | 3.13 % | 7.07 % | 12.50 % | almost no diffusion |
| `elf` | 3.13 % | 5.56 % | — | almost no diffusion |
| `xor8` | 12.50 % | 12.50 % | 12.50 % | none at all |

`xor8` is the perfect teaching case: min, avg and max are all exactly 12.50 %, which is 1/8.
Flipping any single input bit flips exactly one output bit, always. There is no diffusion
whatsoever — the output is a linear function of the input, and every bit of it is traceable to
the bits it came from.

**Two caveats, both important.**

*A good avalanche value is necessary but not sufficient.* CRC-32 avalanches respectably at
45.53 % and is still trivially invertible: because the operation is affine over GF(2), anyone
can compute a four-byte patch that forces a file to any CRC-32 value they like. MD5 avalanches
beautifully and is thoroughly broken. Avalanche is a *design smell test* — a bad value proves
the function is unfit, but a good value proves nothing about cryptographic strength.

*The figure depends on the input length.* A wide function needs enough input to fill its state.
The default `crc64` measures 6.47 % average avalanche on the 9-byte default input and still
only 13.84 % on a 64-byte input, because its generator polynomial is very sparse. If you are
comparing candidates, feed them an input whose size resembles your real data:

```
$ jacksum -a crc64 --info -V details -q txt:"0123456789012345678901234567890123456789012345678901234567890123"
```


<a name="three_families"></a>

# Three Families, Three Jobs

Jacksum's 586 algorithms fall into three families that were designed for three different jobs.
Every algorithm's documentation states which one it belongs to, in the `type:` field of
`jacksum -h <algo>`.

| | Classic checksum | CRC | Cryptographic hash function |
|---|---|---|---|
| Designed for | detecting transmission slips cheaply | detecting the error patterns of a physical channel | resisting an intelligent adversary |
| Typical width | 8–32 bits | 8–64 bits | 128–512 bits |
| Detects | some random single-byte errors | all burst errors up to the polynomial's degree, with proofs | any change, by anyone, with overwhelming probability |
| Does *not* detect | byte reordering, inserted zero bytes, many multi-byte errors | deliberate modification | nothing known, while the function stands |
| Cost | trivial | very low | low to moderate |
| Jacksum `type:` | `checksum` | `CRC` | `cryptographic hash function` |
| Invertible by an attacker | yes | yes | no |

The distinction that decides your choice is **the threat model**, and it has exactly two cases.

**Case 1: accidents.** A cable drops a bit, a disk sector rots, RAM flips a bit, a download
truncates. Errors like these are *random and unmotivated*. A 32-bit CRC catches essentially all
of them: any single burst of up to 32 bit errors is detected with certainty, and a random
corruption slips through with probability about 2^-32. That is a genuine engineering guarantee,
and it is why CRCs are everywhere in hardware.

**Case 2: an adversary.** Somebody *wants* your check to pass on a file they modified. Now the
error is not random — it is chosen, by someone who has read the algorithm's specification.

Against an adversary a CRC offers nothing, and the reason is structural rather than a matter of
width. A CRC is an affine function over GF(2): `crc(a XOR b) = crc(a) XOR crc(b) XOR c` for a
constant `c`. From that identity it follows that anyone can take a modified file, compute the
four bytes that need to be appended or patched in, and land on any target CRC value they
choose. There is no computation to speak of; it is solving a small linear system. A wider CRC
does not help, because the attack does not search — it solves. The same applies to `sum`, `xor`
and Adler: they are linear too.

This is the single most consequential idea in this guide. **A CRC answers "did the data change
by accident?" A cryptographic hash answers "did the data change at all?"** Those are different
questions, and only one of them is about security.


<a name="crcs"></a>

# Where CRCs Still Belong

None of the above makes CRCs obsolete. They are current technology, actively specified into new
standards, and Jacksum documents where each one is used. The point is to use them for the job
they are good at.

**They are in today's specifications, not just yesterday's.** From Jacksum's own algorithm
documentation:

- **`crc64_nvme`** — the NVM Express NVM Command Set Specification, revision 1d, of December
  2023. The same CRC is used by **Amazon S3**: the AWS SDK computes it while data is being
  uploaded, and returns it Base64-encoded. A CRC standardised in 2023 and deployed by the
  largest object store in the world is hard to call legacy.
- **`crc32c`** — Castagnoli's CRC-32, specified for iSCSI in RFC 7143 §13.1, and widely used
  for filesystem and network metadata. Modern CPUs have an instruction for it.
- **`crc24`** — the checksum in OpenPGP's ASCII armor (RFC 2440).
- **`crc8`** — the System Management Bus (SMBus) and the Free Lossless Audio Codec (FLAC).
- **`crc32`** — the ISO 3309 / ITU-T V.42 lineage that Ethernet, ZIP, gzip and PNG use;
  `crc32_bzip2` is bzip2's variant of the same polynomial.
- **`crc82_darc`** — the Data Radio Channel of ETSI EN 300 751.
- **`cksum`** — POSIX 1003.2, still the checksum `cksum(1)` prints.

**Why they keep winning those jobs.** A CRC is cheap enough to implement in a few gates, which
matters when it has to run in a disk controller or a radio chip. It is fast: `crc32c` is the
single fastest algorithm in Jacksum's speed ranking. And unlike a hash function, it comes with
*proofs* about which error patterns it detects — a property a cryptographic hash cannot offer,
because its guarantees are probabilistic rather than combinatorial. For a 512-byte sector or a
network frame, a CRC is the right tool and a SHA-256 would be overkill.

**Filesystems.** Jacksum's documentation notes that SHA-256 can optionally be used by ZFS, and
that ZFS's `edonr` checksum is Edon-R-512 *salted with a pool-specific key* — so ZFS's values
deliberately do not match the plain Edon-R values Jacksum computes. (ZFS's default checksum is
Fletcher-4, which Jacksum does not implement; only `fletcher16` is available.)

**Matching an arbitrary CRC.** Because Jacksum implements the Rocksoft (tm) Model, you can
reproduce any CRC a device or protocol specifies, and inspect the polynomial in every
representation:

```
$ jacksum -a crc:32,04C11DB7,FFFFFFFF,true,true,FFFFFFFF --info
$ jacksum -h crc:
```

`--info` prints the polynomial in mathematical, normal, reversed and Koopman notation, plus the
reciprocal polynomial, which is invaluable when a datasheet and a source file appear to
disagree. See [ALGORITHMS.md](ALGORITHMS.md#customizable_crcs).

**The rule.** Use a CRC to catch accidents, and to interoperate with something that mandates
one. Never use it as a security control, and never as the primary fingerprint for a large
collection of files — for the reasons in [Tier A](#tier_a).


<a name="cryptographic"></a>

# What Makes an Algorithm Cryptographic

A cryptographic hash function is not simply a better-mixed checksum. It makes three explicit
promises, and each has a *generic* attack cost that no design can beat — the cost of brute
force against an ideal function of the same width `n`:

| Property | What an attacker must do | Generic cost |
|---|---|---|
| Preimage resistance | find a message matching a given hash value | 2^n |
| Second preimage resistance | find a *different* message matching a given message's hash | 2^n |
| Collision resistance | find *any* two messages that collide | 2^(n/2) |

Collision resistance costs only 2^(n/2) because of the **birthday paradox**: the attacker is
not aiming at one target, but for any coincidence among many candidates. After hashing about
√(2^n) messages, a matching pair becomes likely. So a 256-bit hash offers 256 bits of preimage
resistance but only **128 bits of collision resistance** — and 128 bits is the level the
industry treats as the long-term floor. That is why 256 bits of output, not 128, is the modern
default.

**One distinction that is usually glossed over, and that changes what width you need.**

*Per-file verification.* You recorded one hash value per file, and later you recompute and
compare. There is no birthday effect here: each file is checked against its own stored value.
For random corruption the probability of an undetected change is about 2^-b, and it does not
degrade as you add files. Against accidents, a 32-bit value gives you roughly one undetected
corruption in four billion — per file, and independent of how many files you have.

*Cross-file identification.* You are deduplicating, hunting for duplicates, or matching a
[wanted list](#workflow) against a whole tree. Now every file is compared against every other,
the birthday bound applies to the *entire set*, and the guarantee decays as the collection
grows. This is where narrow values collapse: see the table in [Tier A](#tier_a).

So the same 32-bit value can be adequate for one task and hopeless for the other. And as soon
as an adversary enters, both cases change: they are not waiting for a coincidence, they are
constructing one.

Jacksum labels the family in the `type:` field, so you can always check what you are dealing
with:

```
$ jacksum -h sha3-256 | head -20
```


<a name="broken_means"></a>

# What "Broken" Means

In cryptography a function is **broken** when someone publishes an attack that beats the
generic cost above for at least one of its claimed properties. Note what this does *and does
not* say:

- It is a statement about *published knowledge*, not about your particular files.
- It does not require the attack to be practical. An attack costing 2^127.5 where 2^128 was
  promised is a break in the academic sense and irrelevant in practice.
- It does not mean the function fails at everything. MD5's collision resistance is annihilated
  while its preimage resistance still stands (best known attack: 2^123.4).

Jacksum tracks this per algorithm and reports it in a `Security:` block. Five states exist
(`net.jacksum.algorithms.BrokenState`):

| State | Meaning |
|---|---|
| `no` | No attack better than generic is known against the full function |
| `partly` | At least one, but not all, claimed properties are broken |
| `yes` | The function is broken |
| `depends` | Depends on a parameter, e.g. the hash underlying an HMAC |
| `n/a` | The question does not apply — a CRC or checksum claims no such properties |

```
$ jacksum -a md5 --info --verbose details
```

```
  Security:
    broken:                               yes
      yes, 2004: identical-prefix collisions can be computed in
      seconds and chosen-prefix collisions in hours on a standard PC;
      the attack has been demonstrated against real X.509
      certificates (2008) and was abused by the Flame malware (2012);
      the preimage resistance is not broken (best attack 2^123.4),
      but MD5 must not be used for signatures
      see also https://eprint.iacr.org/2004/199.pdf
```

**Read the sentence, not just the token.** Without `-V details` you get `broken: yes` and
nothing else; with it you get the reasoning, the dates, and a primary source. The difference
between "theoretically weakened" and "collisions in seconds on a laptop" lives entirely in
that text. Four examples worth internalising:

- **`md5` → `yes`.** Identical-prefix collisions in seconds, chosen-prefix collisions in hours,
  demonstrated against real X.509 certificates in 2008, weaponised by Flame in 2012.
- **`sha-1` → `yes`.** The first identical-prefix collision was computed in February 2017
  (SHAttered, 2^63.1); in January 2020 the first *chosen-prefix* collision followed (SHA-1 is a
  Shambles, 2^63.4, about 45 000 USD of rented GPU time). Chosen-prefix is the one that breaks
  real protocols, because the attacker controls what precedes the colliding block.
- **`sha-256` → `no`.** "The best collision attacks reach 31 of 64 steps and are practical at
  that step count (2024), the best preimage attacks reach 41 of 64 steps; the full SHA-256 is
  unaffected." Round-reduced results like these are how confidence is built: the margin between
  31 and 64 steps is the safety margin.
- **`sha3-256` → `no`.** Best collision attacks reach 5 of 24 rounds, best preimages 4 of 24 —
  an enormous margin, and immune to length extension as a bonus.

**Partial breaks are not full breaks.** `md2` is `partly` broken (the preimage resistance of the
full function fell in 2005) while `md4` is `yes` (collisions in a fraction of a second). Both
belong in [Tier C](#tier_c), but they are not the same statement, and treating `partly` as safe
is [an anti-pattern](#antipatterns) in its own right.

<a name="length_extension"></a>

## Length Extension

One weakness deserves separate treatment, because it affects functions that are *not* broken.

Every Merkle–Damgård hash — MD5, SHA-1, the whole SHA-2 family — leaks its internal state in
its output. Given only `sha256(secret || message)` and the *length* of `secret`, an attacker who
never learns `secret` can compute `sha256(secret || message || padding || anything)`. The state
at the end of your message is exactly what they need to keep hashing.

Jacksum flags it in SHA-256's own security note: "as a Merkle-Damgard construction it permits
length-extension attacks: use HMAC or sha-512/256 where that matters".

Three ways out, all available:

- **`hmac:sha256`** — the HMAC construction is specifically designed so this does not work.
- **`sha-512/256`** — SHA-512 with a different initial value, truncated to 256 bits. Jacksum:
  "the truncation removes the length-extension weakness of sha-512". The withheld half of the
  state is what the attacker cannot reconstruct.
- **`sha3-256`** — a sponge. Its state is larger than its output, so the output never reveals
  enough to continue absorbing.

For the file-integrity use case in this guide, plain `sha-256` on a file is *not* vulnerable to
this — you are not building `H(secret || message)`. It matters the moment you try to
authenticate something by prefixing a secret, which is precisely the mistake HMAC exists to
prevent.


<a name="scrutiny"></a>

# How Much Scrutiny Has the Design Had?

`broken: no` is only half the picture. The other half is: **how hard has anyone actually
tried?** A function nobody has studied and a function a hundred cryptographers have attacked
for a decade both report "no known attack", and they are not remotely equivalent claims.

This is the most under-appreciated axis in algorithm selection, and it has a simple statement:
**"no published attack" is evidence only in proportion to the attention paid.**

[ALGORITHMS.md](ALGORITHMS.md#standard_hash_functions_sorted_logically) already sorts every
algorithm by provenance. Read as tiers of vetting, strongest first:

**Tier 1 — national and international standards.** A formal standardisation process means years
of public review, an open comment period, and institutional accountability afterwards. SHA-2
(NIST FIPS 180-4), SHA-3 and SHAKE (FIPS 202), Kupyna (Ukraine, DSTU 7564:2014), Streebog
(Russia, GOST R 34.11-2012), LSH (Republic of Korea, KS X 3262), HAS-160 (KISA), SM3 (China),
belt-hash (Belarus, STB 34.101.31), Whirlpool (ISO/IEC 10118-3).

**Tier 2 — open-competition finalists.** These were cryptanalysed hard *by design*: many
independent teams, public rules, and a multi-year incentive to break each other's submissions.
The five SHA-3 round-3 finalists (BLAKE, Groestl, JH, Keccak, Skein) and the five NIST
Lightweight Cryptography finalists that support hashing (Ascon, Esch, PHOTON-Beetle, Romulus-H,
Xoodyak). A competition is arguably a *more* adversarial review than a standards process.

**Tier 3 — widely deployed non-standards with strong independent review.** No formal standard,
but heavy real-world use and sustained third-party analysis: BLAKE2 (RFC 7693), BLAKE3,
RIPEMD-160, Tiger and Tiger2.

**Tier 4 — competition also-rans.** Round-2 and round-1 candidates that did not advance. Being
a candidate is **not** an endorsement — several were eliminated precisely *because* the analysis
found problems. Among the ones Jacksum ships, `hamsi` and `edonr` are flagged `partly` broken
today, and Fugue had to be tweaked into Fugue2. They are valuable to have available; they are
not archival choices.

**Tier 5 — one-off proposals with thin cryptanalysis.** The 2005 pre-SHA-3 workshop cohort is
the textbook illustration. It contained `dha256`, `fork256` and `vsh`. FORK-256 was broken in
2007. VSH was partly broken in 2006. DHA-256 has no published break — but given what happened
to its two cohort-mates, that clean record is *weak evidence*, not a recommendation.

## Jacksum already tells you this

The skill worth learning is that Jacksum's `broken:` text encodes the scrutiny axis explicitly,
not just the yes/no verdict. Compare:

```
$ jacksum -a dha256 --info -V details
```
> "no, but boomerang attacks reach 46 of 64 steps and a pseudo-collision for the full function
> is known (2^127.5); the full DHA-256 has no collision or preimage attack, but **it has
> received far less public analysis than SHA-256**, which it was designed to improve upon"

```
$ jacksum -a belt-hash --info -V details
```
> "no; no attack on belt-hash itself has been published; note however that **the public
> cryptanalysis of belt-hash is thin compared with SHA-2 or SHA-3**: the third-party results
> published so far target the underlying Bel-T block cipher and reach at most 6 of its 8 rounds"

```
$ jacksum -a groestl256 --info -V details
```
> "no; **Groestl was the most deeply analysed of the five SHA-3 finalists**: the best collision
> attacks on the hash function reach 3 of 10 rounds and the best distinguisher covers 9 of 10
> rounds of the permutation (2^368, 2012), the full Groestl is unaffected"

Three algorithms, all `broken: no`, three very different levels of confidence. And note that
Jacksum will also tell you when a *standardised* function is nonetheless too narrow:

```
$ jacksum -a has160 --info -V details
```
> "no, but the best collision attacks reach 53 of 80 steps (2^55) … the full HAS-160 is
> unaffected, but **its 160-bit output limits the collision resistance to 2^80, which is not
> sufficient anymore; prefer at least 256 bits**"

**The rule.** For data you must still trust in ten years, choose from tier 1 or 2, with at least
256 bits of output. Tiers 3 to 5 are interesting, sometimes excellent, and occasionally faster
— but "no known attacks" on an obscure design may simply mean nobody has looked.


<a name="concatenation"></a>

# Concatenating Algorithms

Jacksum lets you chain any number of algorithms with `+`:

```
$ jacksum -a sha256+sha3-256 -q txt:"Hello World"
$ jacksum -a sha256+crc32c --info
```

This is worth understanding properly, because the intuitive argument for it is wrong and the
real argument is stronger.

## What Jacksum does

Each file is read **only once**, no matter how many algorithms you select, and the algorithms
can run on separate threads (`--threads-hashing`). So the marginal cost of a second algorithm is
CPU, not I/O — which for thousands of files on a disk is close to free, since I/O is the
bottleneck. `--info` confirms what you built:

```
$ jacksum -a sha256+crc32c --info
```

```
  Algorithm:
    name:                                 sha256+crc32c
    actual combined algorithms:           2

  Hash length:
    bits:                                 288
```

By default the values are printed as one concatenated string. For a check list you probably want
them separated, which `-F` does with indexed tokens:

```
$ jacksum -a sha256+sha3-256 -F "#ALGONAME{i}: #HASH{i}" myfile
sha256: 222d5dc399137f3d9a9b74681e273430e3af626d4b9630966cd87e95d58af3c6
sha3-256: fafb96e583781353478913250870b1ee6029c39db298e6ae2e1fe4970eec2031
```

```
$ jacksum -a sha256+sha3-256 -F "#HASH{0} #HASH{1} #FILESIZE #FILENAME" -r max mydir
```

`#HASH{<algo>}` works too, so `-F "#HASH{sha-256} #HASH{sha3-256} #FILENAME"` is equivalent and
self-documenting. Combining algorithms has been possible since Jacksum 1.7.0 (July 2006).

## The honest theory: concatenation does not add security levels

The tempting reasoning is "256 bits plus 256 bits gives me 512 bits of security". It does not.

Joux showed in 2004 (*Multicollisions in Iterated Hash Functions*, CRYPTO 2004) that for two
iterated Merkle–Damgård hashes of `n1` and `n2` bits, finding a collision for the concatenation
`H1(m) ‖ H2(m)` costs roughly

```
(n2/2) · 2^(n1/2)  +  2^(n2/2)
```

rather than `2^((n1+n2)/2)`. That is barely more than attacking the *stronger* of the two alone.
The technique builds a multicollision — many messages sharing one `H1` value — cheaply, by
exploiting the iterated structure, and then searches within that set for an `H2` collision. So
`sha256+sha512` provides much less than its 768-bit output suggests.

Two consequences worth acting on:

- Do not concatenate in order to reach a bigger number. If you want more margin than SHA-256,
  use `sha-512` or `sha-512/256`; that is cheaper and more honest than stacking.
- Because the multicollision technique feeds on the *iterated* structure, pairing
  **structurally different** designs is a genuinely better hedge than pairing two of the same
  shape. `sha256+sha3-256` combines a Merkle–Damgård function with a sponge; `sha256+sha512`
  combines two close relatives.

## Why it is still worth doing

The real argument is engineering, not information theory: **concatenation is insurance against
the algorithm you chose being broken later.**

Look at how breaks have actually happened. MD5 and SHA-1 both fell to *chosen-prefix collision
attacks* — highly specialised constructions built around one function's differential structure,
the product of years of dedicated cryptanalysis of that specific design. Such an attack produces
a colliding pair for *that function*. It says nothing about, and cannot simultaneously satisfy,
an unrelated second function. Nobody has ever demonstrated a simultaneous collision for two
independent, structurally different hash functions.

For the scenario this guide is about, that matters concretely. Suppose you fingerprint a disk
today with `sha-256` alone. In ten years SHA-256 is deprecated. Now you want to re-establish
trust in the archive — but re-hashing requires reading the original data, and by then the disk
may be gone, or may already have been tampered with. Your check list has become unusable at
exactly the moment you needed it.

If the check list also recorded `sha3-256`, you still have a trustworthy fingerprint, **without
re-reading anything**. That is worth one CPU thread.

**Recommended pairings**

| Pairing | Rationale |
|---|---|
| `sha256+sha3-256` | Merkle–Damgård + sponge, different design teams, both tier 1. The default choice for archives. |
| `sha256+blake3` | If future re-verification speed matters more than tooling availability. |
| `sha-512/256+sha3-512` | Maximum margin, both length-extension-immune. |

**Not recommended as a security measure:** a cryptographic hash plus a CRC. It is an appealing
idea and it does not work. Because a CRC is affine, an attacker who has constructed a hash
collision can additionally force the CRC to any value they like — it is a linear equation, not a
search — so the CRC contributes no adversarial security whatsoever. A CRC alongside a hash stays
useful as a cheap pre-filter for accidental corruption, which is a real but much smaller claim.


<a name="speed"></a>

# Speed, and Why It Is Usually the Wrong Tie-Breaker

`--info` ranks every algorithm by speed:

```
$ jacksum -a sha-256 --info
```

```
  Speed:
    relative rank:                        15/586
```

Rank 1 is the fastest. The ranking comes from a weight table in Jacksum's source
(`net.jacksum.multicore.manyalgos.HashAlgorithm`), where a lower weight means faster:

| Algorithm | Weight | Note |
|---|---|---|
| `crc32c` | 4 | fastest in Jacksum |
| `crc32` | 5 | |
| `adler32` | 6 | |
| `sum32` | 10 | |
| `sha-1` | 11 | |
| **`sha-256`** | **11** | **rank 15/586** |
| `sha-512`, `sha-512/256` | 19 | |
| `sha3-256` | 32 | rank 38/586 |
| **`md5`** | **35** | **rank 42/586** |
| `blake2b-256` | 43 | |
| `blake3` | 95 | rank 468/586 |

Two conclusions, both of which contradict widespread advice.

**SHA-256 is faster than MD5.** Weight 11 versus 35; rank 15 versus 42. Modern CPUs have SHA-2
instructions and the JDK uses them, while MD5 gets no such help. "Use MD5 because it is faster"
was true in 1998 and has been false for years. There is no performance argument left for
choosing a broken function.

**A rank is a property of this implementation, not of the algorithm.** BLAKE3 is one of the
fastest hash functions in existence — in optimised C with SIMD. In Jacksum's Java implementation
it ranks 468 of 586. The algorithm is excellent; this particular implementation is not fast.
Never transfer benchmark numbers between implementations.

**And usually none of it matters.** For thousands of files on a disk, the bottleneck is I/O, not
arithmetic. SHA-256 runs at gigabytes per second; a spinning disk does not deliver that. What
actually moves the needle:

```
$ jacksum -a sha-256 --threads-reading max -r max mydir     # parallel reads, good for SSD/NVMe
$ jacksum -a sha256+sha3-256 --threads-hashing 2 -r max mydir  # both hashes, one read pass
```

Because `-a a+b` reads each file once, adding a second algorithm costs CPU only — see
[concatenation](#concatenation).

**Measure on your own hardware** rather than trusting any table, including this one:

```
$ for a in crc32c sha-256 sha3-256 blake3 whirlpool; do
>   printf '%-12s' "$a"; /usr/bin/time -p jacksum -a $a bigfile.bin 2>&1 | awk '/real/{print $2" s"}'
> done
```

Use a file large enough that JVM startup (roughly 50 ms) is noise — a few hundred MB at least.


<a name="implementations"></a>

# Two Implementations Behind One Algorithm ID

For 28 algorithms, Jacksum can compute the same hash value two different ways. This is worth
knowing for performance, and it is worth even more for trust.

## How it works

By default Jacksum uses the implementation the **Java API** provides —
`java.security.MessageDigest`, `java.util.zip.CRC32`, and so on — because JVM vendors optimise
those heavily and often dispatch to CPU intrinsics or native code. The option `-A` switches to
Jacksum's own **pure-Java** implementation where one exists. Per `jacksum -h`, these algorithms
have an alternative:

```
adler32, blake3, crc16, crc32, crc32_fddi, crc32c, fnv-0_32, fnv-0_64, fnv-1_32,
fnv-1_64, fnv-1a_32, fnv-1a_64, fugue224, fugue256, fugue384, fugue512, md2, md5,
sha-1, sha-256, sha-384, sha-512, sha-512/224, sha-512/256, sha3-224, sha3-256,
sha3-384, sha3-512
```

For every other algorithm `-A` is ignored. `--info` reports which one is active:

```
$ jacksum -A -a sha-256 --info
```

```
  Speed:
    relative rank:                        unknown, speed is calculated for primary algorithms only

  Alternative/secondary implementation:
    has been requested:                   true
    is available and would be used:       true
```

Which implementation you get by default is a detail of your JRE vendor and version, and it can
change between releases. The practical advice is unchanged from the day this option was
introduced: **run a current JRE and take the performance it gives you.** One measurement, on one
machine (JDK 25, aarch64, 47 MB file):

| Implementation | Wall clock |
|---|---|
| default (JDK) | 0.10 s |
| `-A` (pure Java) | 0.19 s |

Both figures include roughly 50 ms of JVM startup, so the hashing itself differs by more than
the ratio suggests. Treat this as one data point, not a law — on a different JVM or architecture
the gap will differ, and occasionally the pure-Java path wins.

## The reason that matters more: implementation trust

A hash value is only as trustworthy as the code that produced it. You are about to fingerprint
an entire disk and rely on those values for years; an implementation bug would be discovered far
too late. Two *independent* implementations agreeing on a known test vector is real evidence,
and `-A` makes that a one-liner:

```
$ jacksum -a sha-256 -q txt:"abc"
ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad

$ jacksum -A -a sha-256 -q txt:"abc"
ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad
```

That value is also the published NIST test vector for SHA-256 of `"abc"`. Add a third,
completely unrelated implementation from the [compatibility list](#os_constraint):

```
$ printf 'abc' | sha256sum
ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad  -
```

Three independent code paths, one value, matching the standard's own test vector. Run this once
before you commit an archive to an algorithm, and implementation correctness stops being
something you have to worry about.


<a name="os_constraint"></a>

# The Operating System Constraint

Jacksum is pure Java, so all 586 algorithms are available to you on every platform Java runs on.
That makes it easy to forget the constraint that actually bites:

> The question is not *"can I compute this hash?"* but **"can whoever verifies it later compute
> it with the tools they will have?"**

A check list is a message to the future. If the person reading it has only `sha256sum`, or only
Windows' `certutil`, then a BLAKE3 list is a dead letter no matter how good BLAKE3 is.

This is what the `compatibility:` block of `jacksum -h <algo>` is for. It lists, per algorithm,
the invocation for dozens of operating systems, tools and programming languages:

```
$ jacksum -h sha-256
```

```
            compatibility:
                - 7z:              7z h -scrcsha256
                - BusyBox:         /bin/sha256sum
                - FreeBSD 6+:      /sbin/sha256
                - GNU/Linux:       /usr/bin/sha256sum
                - gpg:             gpg --print-md sha256
                - macOS 10.12+:    /usr/bin/shasum -a 256
                - OpenSSL:         openssl dgst -sha256
                - PowerShell:      Get-FileHash -Algorithm SHA256
                - Python 2.5+:     hashlib.sha256()
                - Solaris 10+:     /usr/bin/digest -a sha256
                - Windows 7+:      certutil -hashfile <file> SHA256
```

Condensed for the candidates you are likely to consider (● native tool available, ○ none):

| | GNU/Linux | macOS | Windows | FreeBSD | Solaris | BusyBox | OpenSSL | 7-Zip | gpg | Python | Java | Go |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `sha-256` | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● |
| `sha-512` | ● | ● | ● | ● | ● | ● | ● | ○ | ● | ● | ● | ● |
| `sha-512/256` | ● | ● | ○ | ● | ● | ○ | ● | ○ | ○ | ○ | ● | ● |
| `sha3-256` | ○ | ○ | ○ | ○ | ● | ● | ● | ● | ○ | ● | ● | ● |
| `blake2b-256` | ● | ○ | ○ | ○ | ○ | ○ | ● | ○ | ○ | ● | ○ | ● |
| `blake3` | ○ | ○ | ○ | ○ | ○ | ○ | ○ | ○ | ○ | ○ | ○ | ○ |
| `md5` | ● | ● | ● | ● | ● | ● | ● | ● | ○ | ● | ● | ○ |
| `crc32` | ○ | ● | ○ | ● | ○ | ○ | ○ | ● | ○ | ● | ● | ● |

Read the rows carefully, because the results are not what reputation suggests:

- **`sha-256` is the only algorithm with genuinely universal support.** Every mainstream OS
  ships a tool for it, as do 7-Zip, gpg, OpenSSL, and every language you might script in. If
  interoperability is a requirement, this row ends the discussion.
- **`sha3-256` has no native tool on GNU/Linux, macOS, Windows or FreeBSD.** It is a NIST
  standard from 2015, it is stronger against length extension than SHA-256, and you still cannot
  verify it with the tools preinstalled on the three most common desktop systems. Solaris 11.4+,
  BusyBox, OpenSSL 1.1.1+, Python 3.6+, Java 9+ and 7zFM 24.09+ can — which is plenty *if you
  control the verification environment*.
- **`sha-512/256` has nothing on Windows.** `shasum -a 512256` covers Linux and macOS, FreeBSD
  has `/sbin/sha512t256`, Solaris 11.4+ has `digest -a 512_t -t 256`. Windows has neither
  `certutil` nor PowerShell support.
- **`blake3` has no entry at all.** In practice it is Jacksum-only. A superb function and the
  worst possible choice for a list somebody else must check.
- **`md5` and `crc32` are the most widely available**, which is exactly why they keep being
  chosen for jobs they cannot do. Availability is not suitability.

**The rules that follow**

1. If Jacksum runs on both ends, the constraint disappears — pick on cryptographic merit alone.
2. Otherwise pick the strongest algorithm the *verification* environment supports, and check it
   with `-h <algo>` rather than assuming.
3. Also pick a **format** the target tool can parse. The right algorithm in the wrong layout is
   still unusable, so choose a `--style`: `gnu-linux`, `bsd`, `bsd-r`, `solaris-digest`, `fciv`,
   `openssl-dgst`, `sfv`, and more. `jacksum -h` documents all 18.
4. If you must serve several environments, generate several lists from a single read pass, or
   record more than one algorithm — see [concatenation](#concatenation).


<a name="blacklist"></a>

# Algorithms Not to Use Any More

Jacksum ships 586 algorithms because it is a *comprehensive* framework: it must be able to read
a check list somebody generated in 1998, reproduce a device's CRC, and demonstrate historical
functions for teaching. Availability in Jacksum is emphatically not a recommendation.

For fingerprinting a large collection of files, the following should be off your list. They are
grouped by *why*, because the reasons are entirely different and the remedy differs too.

<a name="tier_a"></a>

## Tier A: Too Narrow

Output width caps collision resistance no matter how good the design is. Jacksum has 9
algorithms with an 8-bit output, 14 with 16 bits and 8 with 24 bits:

```
$ jacksum -a all:8 -l
blake2b-8 blake2s-8 crc8 md6-8 skein-256-8 skein-512-8 skein-1024-8 sum8 xor8
```

Assuming an ideal, uniformly distributed function, roughly `1.1774 · √(2^b)` items suffice for a
50 % chance that *some* pair collides:

| Width | 50 % chance of a collision | Collision guaranteed (pigeonhole) | Examples |
|---|---|---|---|
| 8 bit | ~19 files | 257 files | `sum8`, `xor8`, `crc8`, `blake2b-8` |
| 16 bit | ~301 files | 65 537 files | `sum16`, `sum_bsd`, `sum_sysv`, `crc16`, `fcs16`, `fletcher16` |
| 24 bit | ~4 800 files | 16 777 217 files | `sum24`, `crc24` |
| 32 bit | ~77 000 files | 2^32 + 1 files | `crc32`, `adler32`, `xxh32`, `elf` |
| 64 bit | ~5.1 · 10^9 files | — | `crc64_xz`, `sum64` |
| 128 bit | ~2.2 · 10^19 files | — | `md5`, `ripemd128`, `tiger128` |
| 256 bit | ~4 · 10^38 files | — | `sha-256`, `sha3-256`, `blake3` |

For "thousands of files on a disk", the 8-, 16- and 24-bit rows are simply disqualified: a few
hundred files already collide. The 32-bit row survives *per-file verification* but fails
*cross-file identification* — at 77 000 files you are at even odds of a duplicate report that
is not a duplicate. See the [distinction](#cryptographic).

**Good design cannot buy back missing width.** This is the most instructive case in the whole
guide:

```
$ jacksum -a blake2b-8 --info -V details
```

BLAKE2b truncated to 8 bits measures a textbook **51.91 %** average avalanche — better than
SHA-256's 50.04 %. It is derived from an excellent, well-reviewed function. And it is completely
useless for a disk full of files, because 8 bits is 8 bits: 256 possible values, so 257 files
must collide. A perfect avalanche in a tiny output space is still a tiny output space.

The same arithmetic retires several functions that are **not** broken:

| Algorithm | `broken:` | Why to skip it anyway |
|---|---|---|
| `has160` | `no` | 160 bits → 2^80 collision resistance. Jacksum: "not sufficient anymore; prefer at least 256 bits" |
| `ripemd128` | `no` | 128 bits → 2^64. Also Landelle/Peyrin 2013: full compression-function collision and a full-hash distinguisher |
| `tiger128`, `tiger160` | `no` | truncations of Tiger-192 → 2^64. Jacksum: "the truncation itself is not a weakness", the width is |
| `md6-16`, `skein-512-32`, … | `no` | fine functions, configured to a useless width |

**Do not truncate to save space.** A 32-byte hash for a million files is 32 MB. There is no
storage argument that justifies giving up collision resistance.

<a name="tier_b"></a>

## Tier B: Defective by Construction

These fail even in the *accidental corruption* case, which is the job they were nominally built
for. Each failure is demonstrable in one line.

**`sum8` … `sum64` — byte order is ignored, and zero bytes vanish.**

Jacksum's own documentation states it: the algorithm "does not consider the order of the bytes in
the data stream. And since the algorithm only adds up the values of the bytes, all zero bytes
are ignored." Watch:

```
$ jacksum -a sum32 -q txt:"abc"
294 3
$ jacksum -a sum32 -q txt:"cba"
294 3
$ jacksum -a sum32 -q txt:"bca"
294 3
```

Any permutation of a file's bytes produces the same value. Reordering records in a file, swapping
two fields, reversing a block — all invisible. And appending zero bytes changes nothing either:

```
$ jacksum -a sum32 -q hex:616263
294 3
$ jacksum -a sum32 -q hex:61626300000000
294 7
```

The checksum stays `294`; only the size field moved from 3 to 7.

**`xor8` — duplicated bytes cancel out.**

```
$ jacksum -a xor8 -q txt:"abc"
96 3
$ jacksum -a xor8 -q txt:"abcxx"
96 5
```

Since `x XOR x = 0`, any byte appearing an even number of times contributes nothing. Combined
with its 12.50 % flat avalanche, `xor8` detects almost nothing.

**`elf` — a symbol-table hash, not a checksum.** Average avalanche 5.56 %, minimum 3.13 %. It was
designed to distribute names in the ELF object-format hash table, and it is fine at that.

**`sum_bsd`, `sum_sysv`, `sum_minix`, `cksum` (Minix)** — 16-bit `sum(1)` variants kept purely so
Jacksum can reproduce what historical Unix tools printed.

**`prng`, `strsum`, `joaat`, `fnv-*`** — non-cryptographic hash functions for hash tables. Fast
and well distributed for that purpose; `prng` and `strsum` show avalanche minima of 3.13 %.

By contrast, the algorithms that *are* order-sensitive behave as you would hope:

```
$ jacksum -a adler32 -q txt:"abc"   →  38600999 3    $ jacksum -a adler32 -q txt:"cba"   →  38863143 3
$ jacksum -a crc32   -q txt:"abc"   →  891568578 3   $ jacksum -a crc32   -q txt:"cba"   →  3635344512 3
$ jacksum -a sha-256 -q txt:"abc"   →  ba7816bf…     $ jacksum -a sha-256 -q txt:"cba"   →  6d970874…
```

**One mitigating detail worth knowing.** Jacksum's default output and the `full` style record the
**file size** next to the value, which catches the zero-padding trick above (`294 3` versus
`294 7`). A size field is a useful extra guard, and it is not a substitute for a real hash — it
does nothing about byte reordering, and an attacker controls it as easily as the content.

**Avoid the SFV format** for anything that matters. Jacksum can write it, which is not the same
as recommending it. Its definition
(`src/main/resources/net/jacksum/compats/defs/sfv.properties`) is:

```
algorithm.default=crc32
formatter.format=#FILENAME #CHECKSUM{hex-uppercase}
```

CRC-32 only, and no file size at all — so it loses even the weak guard above.

<a name="tier_c"></a>

## Tier C: Broken Cryptographic Hash Functions

Use these only to *read* an old check list, never to create a new one. Jacksum flags 15
algorithms as `yes` or `partly` broken:

| Algorithm | State | Why |
|---|---|---|
| `md5` | `yes` | collisions in seconds (2004); X.509 demo 2008; Flame 2012 |
| `sha-1` | `yes` | SHAttered 2017 (2^63.1); chosen-prefix Shambles 2020 (2^63.4, ≈45 000 USD) |
| `md4` | `yes` | collisions in a fraction of a second |
| `md2` | `partly` | preimage resistance of the full function broken (2005) |
| `sha0` | `yes` | collision resistance of the full function broken (2004) |
| `ed2k` | `yes` | built from MD4 |
| `haval` | `yes` | collisions known for the full function at every round count |
| `gost` | `yes` | collision *and* preimage resistance broken (2008) |
| `mdc2` | `yes` | the block-cipher-based variant implemented here |
| `panama` | `yes` | hashing mode broken in practice (2007) |
| `fork256` | `yes` | collision resistance of the full function broken (2007) |
| `streebog512` | `partly` | second-preimage resistance of the full function (2014) |
| `hamsi<n>` | `partly` | second-preimage resistance of the full function (2010) |
| `edonr<n>` | `partly` | preimage resistance broken in theory (2009); secret-prefix MAC broken in practice |
| `vsh` | `partly` | preimage resistance broken (2006) |

`md5` and `sha-1` are the two that matter in practice, because they are still installed
everywhere and still offered by default in far too many tools. To repeat: **"widely available" is
not an argument**, and as [the speed section](#speed) shows, MD5 is not even fast any more.

<a name="variants"></a>

## Same Name, Different Variant

One trap that catches careful people. "CRC-64" is not one algorithm, and the variants are not
equally good:

| ID | Avalanche avg (9-byte input) | Avalanche avg (64-byte input) |
|---|---|---|
| `crc64` | 6.47 % | 13.84 % |
| `crc64_go-iso` | 6.47 % | — |
| `crc64_ecma` | 50.13 % | — |
| `crc64_xz` | 50.13 % | — |

`crc64` and `crc64_go-iso` use the sparse polynomial x^64 + x^4 + x^3 + x + 1 (the one the
SWISS-PROT protein databank used until 2009 — and, as
[ALGORITHMS.md](ALGORITHMS.md#footnotes) notes, often misattributed to ISO 3309). With only five
terms it diffuses very slowly. `crc64_ecma` and `crc64_xz` are based on ECMA-182 and behave as a
64-bit CRC should.

So the name tells you almost nothing; the parameters do. Always look:

```
$ jacksum -a crc64 --info -V details      # polynomial, init, reflection, xorOut, avalanche
$ jacksum -a crc64_xz --info -V details
```

**The closing rule for this whole section.** To fingerprint thousands of files for later
verification, choose a **cryptographic hash function of at least 256 bits, flagged `broken: no`,
from [vetting tier 1 or 2](#scrutiny)**. Everything in Tier A and Tier B exists for format
compatibility, legacy interoperability, protocol conformance and teaching — which is the only
reason Jacksum offers it.


<a name="hmac"></a>

# Why a Broken Algorithm Can Still Be Used: HMAC

Here is a result that surprises people: **HMAC-MD5 has no practical attack, even though MD5 is
thoroughly broken.**

That is not a loophole, it is a consequence of what HMAC assumes. Plain hashing relies on
collision resistance — the property MD5 lost in 2004. HMAC does not. Its security proof rests on
the compression function behaving like a pseudorandom function under a secret key, and a
collision attack does not establish anything about that. On top of it, the attacker does not know
the key, so they cannot even aim a collision at the right internal state.

Jacksum states this itself:

```
$ jacksum -a hmac:md5 -k txt:secret --info --verbose details
```

```
  Security:
    broken:                               depends
      depends on the underlying hash function; note that a broken
      hash function does not necessarily yield a broken HMAC:
      HMAC-MD5 has no practical attack although MD5 itself is broken,
      because the HMAC construction does not rely on collision
      resistance; nevertheless HMAC should be instantiated with a
      hash function that is not broken

  HMAC parameters:
    underlying cryptographic hash:        md5
    truncate to bits:                     no truncation
    trunc. length should have min. bits:  80
    key length should have min. bytes:    16
    key length follows above recom.:      false
    key will be hashed:                   false
```

Note the last four lines: Jacksum checks your key and truncation against the RFC 2104 / FIPS
198-1 recommendations and tells you when they fall short. `key length follows above recom.: false`
means the six-character key `secret` is below the recommended 16 bytes.

**"Can" is not "should".** For anything new, instantiate HMAC with a function that is not broken.
`hmac:md5` being safe today is a statement about the *current* state of cryptanalysis of MD5's
compression function, not a promise. There is no upside to spending it: `hmac:sha256` costs the
same and rests on far firmer ground.

There is a second, unrelated reason a broken hash is sometimes acceptable: when there is **no
adversary at all**. Deduplication inside your own storage, cache keys, "have I seen this file
before" — in these tasks nobody is trying to construct a collision, and MD5's 128-bit output and
excellent distribution do the job. The moment the value crosses a trust boundary, that reasoning
evaporates.

## What HMAC buys you for a check list

This is the part that matters for our scenario, and it is not about broken algorithms at all.

A plain hash list protects the *files*. It does not protect *itself*. An attacker who can modify
your files can usually also modify `files.sha256` — recompute the hashes of the tampered files,
write them into the list, and your verification passes cleanly. The list is only trustworthy if
its integrity is guaranteed by something outside the attacker's reach.

HMAC provides exactly that, using a key the attacker does not have:

```
$ jacksum -a hmac:sha256 -k password --style full -o files.hmac -r max mydir
$ jacksum -a hmac:sha256 -k password --style full -c files.hmac
```

`-k` accepts `txt:`, `hex:`, and other prefixes, or the literal `readline` / `password` to be
prompted interactively rather than leaving the key in your shell history. Without the key, an
attacker cannot produce values that will verify, no matter how much of the filesystem they own.

Alternatives that achieve the same goal differently: store the list on write-once or read-only
media, keep it on a separate trusted machine, or sign it (`gpg --detach-sign files.sha256`).
Pick whichever fits your setup — but pick one. A hash list on the same writable volume as the
data it protects is [an anti-pattern](#antipatterns).

**Scope.** 492 of Jacksum's 586 algorithms can be used with HMAC:

```
$ jacksum --hmacs -V summary          # the list, plus the count
$ jacksum --hmacs -V info             # per algorithm: output size, block size, recommended minimums
```

The remaining 94 are excluded because they are not cryptographic hash functions, or because
their block size does not exceed their output size (which RFC 2104 requires — this rules out the
sponge and XOF constructions), or because they are not plain iterated functions (the tree modes).
[ALGORITHMS.md](ALGORITHMS.md#hmac) has the details.


<a name="decision"></a>

# The Decision Procedure

Five questions, in order. Each one narrows the field, and none of them is about which algorithm
sounds most impressive.

**1. Is an adversary in the threat model?**
Not "is one likely" — *is one possible*. Could anyone benefit from your check passing on modified
data? If the files are software, backups, evidence, legal or financial records, or anything that
leaves your control, the answer is yes. If the answer is no and stays no, a CRC is genuinely
adequate and much cheaper.

**2. Who verifies, and with what?**
If Jacksum runs on both ends, skip to question 3. Otherwise consult
[`-h <algo>`](#os_constraint) for the environment that must read the list, and eliminate
everything it cannot compute. This question decides more real cases than cryptographic strength
does.

**3. Can the check list itself be protected?**
Read-only media, a separate machine, or a signature — good, a plain hash suffices. If the list
must live next to the data on writable storage, you need [HMAC](#hmac) with a key held elsewhere.

**4. How long must this hold?**
Weeks: any unbroken function will do. Years to decades: restrict yourself to
[vetting tier 1 or 2](#scrutiny) with ≥ 256 bits, and consider question 5.

**5. Will the data outlive the algorithm?**
If re-reading the source data in ten years is impossible or untrustworthy, record two
structurally different hashes now — see [concatenation](#concatenation). One CPU thread today
buys you a check list that survives one of its algorithms being deprecated.

```
                    Is an adversary possible?
                       │
             ┌─────────┴─────────┐
            no                  yes
             │                   │
    crc32c / crc64_nvme    Must other tools verify it?
    (fast, adequate            │
     for accidents)   ┌────────┴────────┐
                     yes               no
                      │                 │
                  sha-256          sha3-256
                      │                 │
                      └────────┬────────┘
                               │
                    Is the check list itself
                    outside the attacker's reach?
                               │
                      ┌────────┴────────┐
                     yes               no
                      │                 │
                 keep as is      hmac:<algo> -k
                      │
                    Must the data outlive
                    the algorithm?
                      │
             ┌────────┴────────┐
            no                yes
             │                 │
        keep as is     sha256+sha3-256
```

**The resulting recommendations**

| Situation | Choose | Why |
|---|---|---|
| Adversary possible, must interoperate | `sha-256` | the only universally supported unbroken function |
| Adversary possible, Jacksum on both ends | `sha3-256` | sponge, no length extension, huge attack margin; Jacksum's default since 3.0.0 |
| Long-term archive | `sha256+sha3-256` | survives either function being deprecated, one read pass |
| Extra margin wanted | `sha-512/256` or `sha-512` | 512-bit internals; `/256` also drops length extension |
| List stored where an attacker could reach it | `hmac:sha256 -k …` | authenticates the list, not just the files |
| Accidents only, or protocol conformance | `crc32c`, `crc64_nvme` | fastest, with real error-detection guarantees |
| Reading a legacy list | whatever it used | check its limits with `--info -V details`, and re-hash with something current |


<a name="workflow"></a>

# Worked Example: Thousands of Files

The complete sequence for the scenario this guide opened with. Every command below was run
against Jacksum 4.0.0.

**1. Justify the candidate.** Before trusting an algorithm with an archive, look at it:

```
$ jacksum -a sha-256 --info --verbose details
```

Check four things: `broken:` says `no`, the reasoning behind it is reassuring, the avalanche
average sits near 50 %, and the width is at least 256 bits.

**2. Check that the future can read it.**

```
$ jacksum -h sha-256
```

**3. Cross-check the implementation** (see [above](#implementations)):

```
$ jacksum -a sha-256 -q txt:"abc"
$ jacksum -A -a sha-256 -q txt:"abc"
$ printf 'abc' | sha256sum
```

All three must print `ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad`.

**4. Create the check list.** Note that options come *before* the file operands, and that
`--style full` records hash, timestamp, size and name:

```
$ jacksum -a sha-256 --style full -o files.sha256 -r max /data
```

Useful additions:

| Option | Effect |
|---|---|
| `-r max` | full recursion (the default when `-r` is omitted; `-r <n>` limits depth) |
| `--header` | write a header with version, OS, JVM, date and invocation |
| `--path-relative-to <path>` | store relative paths, so the list survives being moved |
| `-8` / `--utf8` | UTF-8 output — do this if filenames are not pure ASCII |
| `--charset-output-file <cs>` | explicit charset for the list |
| `-u errors.txt` | collect unreadable files instead of losing them in the scroll |
| `-E base64` | a more compact encoding than hex |

The `full` style is worth the extra bytes: size and timestamp cost nothing to record and give
you something to reason about when a verification fails.

**5. The archival variant** — two algorithms, one read pass, readable columns:

```
$ jacksum -a sha256+sha3-256 -F "#HASH{0} #HASH{1} #FILESIZE #FILENAME" -r max /data
```

**6. Protect the list.** Copy it to read-only or offline media, sign it
(`gpg --detach-sign files.sha256`), or generate it with [HMAC](#hmac) and keep the key elsewhere.
Skipping this step undoes most of the work.

**7. Verify later.** Use the *same* `-a` and `--style` you created it with — this matters:

```
$ jacksum -a sha-256 --style full -c files.sha256
```

If you omit `--style full`, Jacksum has no way to know the timestamp and size columns are there,
parses them as part of the filename, and reports everything as `MISSING`. If you omit `-a`, it
defaults to `sha3-256` and everything `FAILED`. Both failures look alarming and mean nothing.

The report distinguishes five outcomes:

```
Jacksum: matches (OK): 2
Jacksum: mismatches (FAILED): 0
Jacksum: new files (NEW): 0
Jacksum: missing files (MISSING): 0
Jacksum: files with errors (ERROR): 0
Jacksum: strict check: PASSED
```

| Outcome | Meaning |
|---|---|
| `OK` | the file is present and its hash matches |
| `FAILED` | present, hash differs — the file changed |
| `MISSING` | in the list, not on disk |
| `NEW` | on disk, not in the list |
| `ERROR` | could not be read |

**8. The strict audit.** `--check-strict` turns any of `FAILED`, `MISSING`, `NEW` or `ERROR` into
an overall failure, which is what you want for "prove nothing changed":

```
$ jacksum -a sha-256 --style full --check-strict -c files.sha256 /data
```

Note two things. Strict mode needs the directory operand as well, so it can spot files that
appeared. And it requires `--list-filter all` (the default) — filtering would suppress the
hashing that detection depends on, so combining it with `--list-filter bad` is rejected outright.

**Exit codes** make this scriptable:

| Code | Meaning |
|---|---|
| 0 | everything OK |
| 1 | at least one mismatch during verification |
| 2 | parameter error |
| 3 | check-file parse error |
| 4 | I/O error |
| 5 | a wanted hash was not found |
| 6 | the strict check failed |

So a plain mismatch exits 1, while a failed strict audit exits 6 — worth distinguishing in a
monitoring script.

**9. For very large runs**, filter the output to what needs attention (without `--check-strict`):

```
$ jacksum -a sha-256 --style full --list-filter bad -c files.sha256
```

`bad` shows only `FAILED`, `MISSING` and `ERROR`; `good` shows `OK` and `NEW`; `none` prints the
summary only.

**10. Tune throughput.**

```
$ jacksum -a sha-256 --threads-reading max --style full -o files.sha256 -r max /data
```

`--threads-reading max` helps on SSD and NVMe, where parallel reads scale; on a single spinning
disk it can hurt, because seeking is the cost. `--threads-hashing` controls the hashing side,
which is what a concatenated algorithm uses.

**11. Spot checks and searches**, without a full list:

```
$ jacksum -a sha-256 -q txt:"Hello World" -e a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e
$ jacksum -a sha-256 -e <hash> -r max /data          # find files matching one hash
$ jacksum -a sha-256 -w wanted.txt -r max /data      # find files matching any hash in a list
$ jacksum -a sha-256 --style gnu-linux --check-line "<one line from a list>"
```

**12. Identify an unknown hash.** If you have the data and the value but not the algorithm,
Jacksum can search for it (`-E` is required):

```
$ jacksum -a unknown:256 -E hex -q txt:"abc" -e ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad
Trying 55 algorithms with a width of 256 bits that are supported by Jacksum 4.0.0 ...
sha-256
    --> SHA-256 (SHA-2 family)
```


<a name="antipatterns"></a>

# Anti-Patterns

Each of these is common, and each has a specific reason for being wrong.

**Using a CRC as a security control.** A CRC is affine over GF(2); an attacker solves a small
linear system to force any value. Widening it does not help, because there is no search to
lengthen. See [three families](#three_families).

**Adding a CRC "in case the hash is ever broken".** `sha256+crc32c` looks like defence in depth
and is not. Whoever can construct a collision for the hash can force the CRC to any value in the
same step, so the second value costs CPU and buys no security against an attacker at all. Combine
two structurally different *cryptographic* hash functions instead — see
[concatenation](#concatenation).

**Choosing MD5 "because it is faster".** It is not. SHA-256 has weight 11 in Jacksum's ranking,
MD5 has 35 — modern CPUs accelerate SHA-2 and not MD5. The trade you think you are making does
not exist. See [speed](#speed).

**Truncating a hash to save disk space.** 32 bytes per file is 32 MB per million files.
Truncation costs collision resistance permanently in exchange for nothing. See
[Tier A](#tier_a).

**Storing the check list on the same writable volume as the data.** Whoever can change the files
can change the list. Use read-only media, a separate host, a signature, or [HMAC](#hmac).

**Verifying with different options than you created with.** Omitting `--style full` makes every
entry `MISSING`; omitting `-a` silently falls back to `sha3-256` and makes everything `FAILED`.
Record the exact invocation — `--header` does it for you.

**Comparing hashes across tools without matching the encoding.** The same value can be printed as
lowercase hex, uppercase hex, decimal, Base32 or Base64. Several Jacksum algorithms default to
*decimal* (all CRCs and classic checksums). If two tools disagree, check `-E` and the style
before suspecting corruption.

**Treating `partly` broken as safe.** `partly` means at least one claimed property has fallen. It
is not a passing grade; it is a reason to migrate. See [what broken means](#broken_means).

**Picking an exotic algorithm because "it has no known attacks".** For an unstudied design that
statement carries almost no information. Read the `broken:` text — Jacksum tells you when the
cryptanalysis is thin. See [scrutiny](#scrutiny).

**Hashing file names or metadata instead of content.** A `names-only`, `sizes-and-names` or
`timestamps-and-names` style is useful for a fast inventory, and it is not integrity protection.
The same applies to the size and timestamp columns in a full-style list: helpful context, trivial
to forge.

**Assuming SFV is good enough because a tool wrote it.** CRC-32, uppercase hex, filename only, no
size. See [Tier B](#tier_b).

**Fingerprinting once and never again.** A check list you never verify proves nothing. Schedule
the verification, and make the exit code visible to something that will notice.


<a name="cheatsheet"></a>

# Cheat Sheet

Everything this guide used, in one place.

**Inspect one algorithm**

| Command | Shows |
|---|---|
| `jacksum -a <algo> --info` | width, block size, HMAC capability, `broken:` token, speed rank |
| `jacksum -a <algo> --info -V details` | the same plus the security reasoning and the avalanche measurement |
| `jacksum -a <algo> --info -q <sequence>` | avalanche measured on *your* input instead of `123456789` |
| `jacksum -A -a <algo> --info` | which implementation is active |
| `jacksum -h <algo>` | full documentation: type, year, standard, comment, `broken:`, compatibility list |
| `jacksum -h crc:` | every parameter of the Rocksoft (tm) Model |
| `jacksum -h hmac:` | HMAC syntax and truncation |
| `jacksum -h algorithms` | the documentation of all algorithms |
| `jacksum -h examples` | worked examples from the manpage |

**Enumerate algorithms**

| Command | Result |
|---|---|
| `jacksum -a all -l` | the IDs of all 586 algorithms |
| `jacksum -a all:256 -l` | only 256-bit outputs (55) |
| `jacksum -a all:8 -l` | only 8-bit outputs (9) — the "too narrow" list |
| `jacksum -a all:sha -l` | only IDs containing "sha" |
| `jacksum --hmacs` | the 492 algorithms usable with HMAC |
| `jacksum --hmacs -V info` | per algorithm: output size, block size, recommended key and truncation minimums |
| `jacksum --hmacs -V summary` | just the count |
| `jacksum -a all --info` | the info block for every algorithm |

**Create, protect, verify**

| Command | Purpose |
|---|---|
| `jacksum -a sha-256 --style full -o list.txt -r max /data` | create a check list |
| `jacksum -a sha256+sha3-256 -F "#HASH{0} #HASH{1} #FILESIZE #FILENAME" -r max /data` | two algorithms, one read pass |
| `jacksum -a hmac:sha256 -k password --style full -o list.txt -r max /data` | authenticated check list |
| `jacksum -a sha-256 --style full -c list.txt` | verify |
| `jacksum -a sha-256 --style full --check-strict -c list.txt /data` | strict audit (also detects new files) |
| `jacksum -a sha-256 --style full --list-filter bad -c list.txt` | show only what needs attention |
| `jacksum -a sha-256 -e <hash> -r max /data` | find files matching a hash |
| `jacksum -a sha-256 -w wanted.txt -r max /data` | find files matching any hash in a list |
| `jacksum -a unknown:256 -E hex -q <sequence> -e <hash>` | identify the algorithm behind a hash |
| `jacksum -a sha-256 --threads-reading max -r max /data` | parallel reads |


<a name="footnotes"></a>

# Footnotes and Further Reading

**Jacksum documentation**

- [Algorithms](ALGORITHMS.md) — all 586 algorithms, sorted alphabetically and by provenance;
  also [HMAC](ALGORITHMS.md#hmac) and [customizable CRCs](ALGORITHMS.md#customizable_crcs)
- [Features](FEATURES.md), [Examples](EXAMPLES.md), [Jacksum Hacks](JACKSUM_HACKS.md)
- [Manpage](https://github.com/jonelo/jacksum/wiki/Manpage) and
  [Cheat Sheet](https://github.com/jonelo/jacksum/wiki/Cheat-Sheet) in the wiki

**Primary sources for the security statements**

These are the references Jacksum's own `broken:` texts cite; `--info -V details` prints the
relevant one for each algorithm.

- MD5, HAVAL-128-3 and the original RIPEMD collisions (Wang et al., 2004) —
  https://eprint.iacr.org/2004/199.pdf
- SHA-1, first identical-prefix collision (SHAttered, 2017) — https://shattered.io
- SHA-1, first chosen-prefix collision (Shambles, 2020) — https://sha-mbles.github.io
- SHA-256, best known step-reduced attacks (2024) — https://eprint.iacr.org/2024/349.pdf
- SHA-3 / Keccak cryptanalysis — https://eprint.iacr.org/2019/147
- RIPEMD-128, full compression-function collision (Landelle, Peyrin 2013) —
  https://eprint.iacr.org/2013/607
- Whirlpool, rebound attacks (2010) — https://eprint.iacr.org/2010/198
- Edon-R (2009) — https://eprint.iacr.org/2009/135
- CRC-64/NVMe — https://nvmexpress.org

**Standards and papers referenced in this guide**

- A. Joux, *Multicollisions in Iterated Hash Functions. Application to Cascaded Constructions*,
  CRYPTO 2004 — the result that concatenation does not add security levels
- NIST FIPS 180-4 (SHA-1, SHA-2), FIPS 202 (SHA-3, SHAKE), FIPS 198-1 (HMAC)
- RFC 2104 (HMAC), RFC 2440 (OpenPGP), RFC 7143 (iSCSI, CRC-32c), RFC 7693 (BLAKE2)
- ISO/IEC 10118-3 (Whirlpool), ISO 3309 / ITU-T V.42 (CRC-32), ETSI EN 300 751 (CRC-82/DARC)
- POSIX 1003.2 (`cksum`)

**A closing note on trust.** Every number in this guide came out of Jacksum itself, and every
command is repeatable on your machine. That is deliberate: the point is not to memorise a
recommendation, but to be able to re-derive it when the recommendations change — because they
will. `--info -V details` is the habit worth keeping.
