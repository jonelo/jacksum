package net.jacksum.zzadopt.kr.re.nsr.crypto.hash;

import java.util.Arrays;

import net.jacksum.zzadopt.kr.re.nsr.crypto.Hash;
import net.jacksum.zzadopt.kr.re.nsr.crypto.util.PackLE;

/**
 * LSH256 알고리즘 구현
 *
 * 워드 길이: 32-bit (4-byte) 연쇄 변수 길이: 512-bit (64-byte) 메시지 블록 길이: 1024-bit
 * (128-byte)
 */
public class LSH256 extends Hash {

    //@formatter:off
    private static final int BLOCKSIZE = 128;

    private static final int NUMSTEP = 26;

    /// 사전 계산된 224비트 출력용 IV
    private static final int IV224[] = {
        0x068608D3, 0x62D8F7A7, 0xD76652AB, 0x4C600A43, 0xBDC40AA8, 0x1ECA0B68, 0xDA1A89BE, 0x3147D354,
        0x707EB4F9, 0xF65B3862, 0x6B0B2ABE, 0x56B8EC0A, 0xCF237286, 0xEE0D1727, 0x33636595, 0x8BB8D05F,};

    /// 사전 계산된 256비트 출력용 IV
    private static final int IV256[] = {
        0x46a10f1f, 0xfddce486, 0xb41443a8, 0x198e6b9d, 0x3304388d, 0xb0f5a3c7, 0xb36061c4, 0x7adbd553,
        0x105d5378, 0x2f74de54, 0x5c2f2d95, 0xf2553fbe, 0x8051357a, 0x138668c8, 0x47aa4484, 0xe01afb41
    };

    /// STEP 상수
    private static final int STEP[] = {
        0x917caf90, 0x6c1b10a2, 0x6f352943, 0xcf778243, 0x2ceb7472, 0x29e96ff2, 0x8a9ba428, 0x2eeb2642,
        0x0e2c4021, 0x872bb30e, 0xa45e6cb2, 0x46f9c612, 0x185fe69e, 0x1359621b, 0x263fccb2, 0x1a116870,
        0x3a6c612f, 0xb2dec195, 0x02cb1f56, 0x40bfd858, 0x784684b6, 0x6cbb7d2e, 0x660c7ed8, 0x2b79d88a,
        0xa6cd9069, 0x91a05747, 0xcdea7558, 0x00983098, 0xbecb3b2e, 0x2838ab9a, 0x728b573e, 0xa55262b5,
        0x745dfa0f, 0x31f79ed8, 0xb85fce25, 0x98c8c898, 0x8a0669ec, 0x60e445c2, 0xfde295b0, 0xf7b5185a,
        0xd2580983, 0x29967709, 0x182df3dd, 0x61916130, 0x90705676, 0x452a0822, 0xe07846ad, 0xaccd7351,
        0x2a618d55, 0xc00d8032, 0x4621d0f5, 0xf2f29191, 0x00c6cd06, 0x6f322a67, 0x58bef48d, 0x7a40c4fd,
        0x8beee27f, 0xcd8db2f2, 0x67f2c63b, 0xe5842383, 0xc793d306, 0xa15c91d6, 0x17b381e5, 0xbb05c277,
        0x7ad1620a, 0x5b40a5bf, 0x5ab901a2, 0x69a7a768, 0x5b66d9cd, 0xfdee6877, 0xcb3566fc, 0xc0c83a32,
        0x4c336c84, 0x9be6651a, 0x13baa3fc, 0x114f0fd1, 0xc240a728, 0xec56e074, 0x009c63c7, 0x89026cf2,
        0x7f9ff0d0, 0x824b7fb5, 0xce5ea00f, 0x605ee0e2, 0x02e7cfea, 0x43375560, 0x9d002ac7, 0x8b6f5f7b,
        0x1f90c14f, 0xcdcb3537, 0x2cfeafdd, 0xbf3fc342, 0xeab7b9ec, 0x7a8cb5a3, 0x9d2af264, 0xfacedb06,
        0xb052106e, 0x99006d04, 0x2bae8d09, 0xff030601, 0xa271a6d6, 0x0742591d, 0xc81d5701, 0xc9a9e200,
        0x02627f1e, 0x996d719d, 0xda3b9634, 0x02090800, 0x14187d78, 0x499b7624, 0xe57458c9, 0x738be2c9,
        0x64e19d20, 0x06df0f36, 0x15d1cb0e, 0x0b110802, 0x2c95f58c, 0xe5119a6d, 0x59cd22ae, 0xff6eac3c,
        0x467ebd84, 0xe5ee453c, 0xe79cd923, 0x1c190a0d, 0xc28b81b8, 0xf6ac0852, 0x26efd107, 0x6e1ae93b,
        0xc53c41ca, 0xd4338221, 0x8475fd0a, 0x35231729, 0x4e0d3a7a, 0xa2b45b48, 0x16c0d82d, 0x890424a9,
        0x017e0c8f, 0x07b5a3f5, 0xfa73078e, 0x583a405e, 0x5b47b4c8, 0x570fa3ea, 0xd7990543, 0x8d28ce32,
        0x7f8a9b90, 0xbd5998fc, 0x6d7a9688, 0x927a9eb6, 0xa2fc7d23, 0x66b38e41, 0x709e491a, 0xb5f700bf,
        0x0a262c0f, 0x16f295b9, 0xe8111ef5, 0x0d195548, 0x9f79a0c5, 0x1a41cfa7, 0x0ee7638a, 0xacf7c074,
        0x30523b19, 0x09884ecf, 0xf93014dd, 0x266e9d55, 0x191a6664, 0x5c1176c1, 0xf64aed98, 0xa4b83520,
        0x828d5449, 0x91d71dd8, 0x2944f2d6, 0x950bf27b, 0x3380ca7d, 0x6d88381d, 0x4138868e, 0x5ced55c4,
        0x0fe19dcb, 0x68f4f669, 0x6e37c8ff, 0xa0fe6e10, 0xb44b47b0, 0xf5c0558a, 0x79bf14cf, 0x4a431a20,
        0xf17f68da, 0x5deb5fd1, 0xa600c86d, 0x9f6c7eb0, 0xff92f864, 0xb615e07f, 0x38d3e448, 0x8d5d3a6a,
        0x70e843cb, 0x494b312e, 0xa6c93613, 0x0beb2f4f, 0x928b5d63, 0xcbf66035, 0x0cb82c80, 0xea97a4f7,
        0x592c0f3b, 0x947c5f77, 0x6fff49b9, 0xf71a7e5a, 0x1de8c0f5, 0xc2569600, 0xc4e4ac8c, 0x823c9ce1
    };

    private static final int ALPHA_EVEN = 29;
    private static final int ALPHA_ODD = 5;

    private static final int BETA_EVEN = 1;
    private static final int BETA_ODD = 17;

    private static final int GAMMA[] = {0, 8, 16, 24, 24, 16, 8, 0};
    //@formatter:on

    private int[] cv;
    private int[] tcv;
    private int[] msg;
    private byte[] block;

    private int boff;
    private int outlenbits;

    /**
     * LSH256 기본 생성자, 256비트 출력 설정
     */
    public LSH256() {
        this(256);
    }

    /**
     * LSH256 생성자
     *
     * @param outlenbits 출력 길이, 비트 단위
     */
    public LSH256(int outlenbits) {
        if (outlenbits < 0 || outlenbits > 256) {
            throw new RuntimeException("invalid hash length");
        }

        cv = new int[16];
        tcv = new int[16];
        msg = new int[16 * (NUMSTEP + 1)];
        block = new byte[BLOCKSIZE];
        this.outlenbits = outlenbits;

        init();
    }

    /**
     * 같은 출력길이를 가지는 객체를 만들어 리턴한다.
     *
     * @return LSH256객체
     */
    @Override
    public Hash newInstance() {
        return new LSH256(this.outlenbits);
    }

    private void init() {
        boff = 0;

        switch (outlenbits) {
            case 224:
                System.arraycopy(IV224, 0, cv, 0, cv.length);
                break;

            case 256:
                System.arraycopy(IV256, 0, cv, 0, cv.length);
                break;

            default:
                generateIV();
                break;
        }
    }

    /**
     * 내부 블록 크기를 반환한다.
     *
     * @return 내부 블록 크기
     */
    @Override
    public int getBlockSize() {
        return BLOCKSIZE;
    }

    /**
     * 출력 길이를 반환한다.
     *
     * @return 출력 길이, 비트 단위
     */
    @Override
    public int getOutlenbits() {
        return outlenbits;
    }

    /**
     * 상태 변수 초기화
     */
    @Override
    public void reset() {
        Arrays.fill(tcv, 0);
        Arrays.fill(msg, 0);
        Arrays.fill(block, (byte) 0);

        init();
    }

    /**
     * 온라인 동작을 위한 메시지 처리 함수
     *
     * @param data 데이터
     * @param offset 데이터 시작 오프셋
     * @param lenbits 데이터 길이 (바이트)
     */
    @Override
    public void update(byte[] data, int offset, int lenbits) {

        if (data == null || data.length == 0) {
            return;
        }

        int rbytes = lenbits >> 3;
        int rbits = lenbits & 0x7;
        int idx = boff >> 3;

        if ((boff & 0x7) > 0) {
            throw new IllegalArgumentException("bit level update is not allowed");
        }

        int gap = BLOCKSIZE - rbytes;
        if (idx > 0 && rbytes >= gap) {
            System.arraycopy(data, offset, block, idx, gap);
            compress(block, 0);
            boff = 0;
            rbytes -= gap;
            offset += gap;
        }

        while (rbytes >= block.length) {
            compress(data, offset);
            boff = 0;
            offset += BLOCKSIZE;
            rbytes -= BLOCKSIZE;
        }

        if (rbytes > 0) {
            idx = boff >> 3;
            System.arraycopy(data, offset, block, idx, rbytes);
            boff += rbytes << 3;
            offset += rbytes;
        }

        if (rbits > 0) {
            idx = boff >> 3;
            block[idx] = (byte) (data[offset] & ((0xff >> rbits) ^ 0xff));
            boff += rbits;
        }
    }

    /**
     * 최종 내부 상태를 업데이트 하고, 해시값을 리턴한다.
     *
     * @return 해시값
     */
    @Override
    public byte[] doFinal() {

        int rbytes = boff >> 3;
        int rbits = boff & 0x7;

        if (rbits > 0) {
            block[rbytes] |= (byte) (0x1 << (7 - rbits));
        } else {
            block[rbytes] = (byte) 0x80;
        }

        Arrays.fill(block, rbytes + 1, block.length, (byte) 0);
        compress(block, 0);

        int[] temp = new int[8];
        for (int i = 0; i < temp.length; ++i) {
            temp[i] = cv[i] ^ cv[i + 8];
        }

        reset();

        rbytes = outlenbits >> 3;
        rbits = outlenbits & 0x7;
        byte[] result = new byte[rbits > 0 ? rbytes + 1 : rbytes];
        for (int i = 0; i < result.length; ++i) {
            result[i] = (byte) (temp[i >> 2] >> ((i << 3) & 0x1f));
        }

        if (rbits > 0) {
            result[rbytes] &= 0xff << (8 - rbits);
        }

        return result;
    }

    /**
     * IV 생성
     */
    private void generateIV() {
        Arrays.fill(cv, (byte) 0);
        Arrays.fill(block, (byte) 0);

        cv[0] = 32;
        cv[1] = outlenbits;

        compress(block, 0);
    }

    /**
     * LSH 알고리즘의 compress 연산
     *
     * @param data 데이터
     * @param offset 데이터 시작 오프셋
     */
    private void compress(byte[] data, int offset) {
        msgExpansion(data, offset);

        for (int i = 0; i < NUMSTEP / 2; ++i) {
            step(2 * i, ALPHA_EVEN, BETA_EVEN);
            step(2 * i + 1, ALPHA_ODD, BETA_ODD);
        }

        // msg add
        for (int i = 0; i < 16; ++i) {
            cv[i] ^= msg[16 * NUMSTEP + i];
        }
    }

    /**
     * Compress 함수에서 사용되는 메시지 확장 연산, BLOCKSIZE 만큼씩 처리함
     *
     * @param in 데이터
     * @param offset 데이터 시작 오프셋 (바이트)
     */
    private void msgExpansion(byte[] in, int offset) {
        PackLE.toU32(in, offset, msg, 0, 32);

        for (int i = 2; i <= NUMSTEP; ++i) {
            int idx = 16 * i;
            msg[idx] = msg[idx - 16] + msg[idx - 29];
            msg[idx + 1] = msg[idx - 15] + msg[idx - 30];
            msg[idx + 2] = msg[idx - 14] + msg[idx - 32];
            msg[idx + 3] = msg[idx - 13] + msg[idx - 31];
            msg[idx + 4] = msg[idx - 12] + msg[idx - 25];
            msg[idx + 5] = msg[idx - 11] + msg[idx - 28];
            msg[idx + 6] = msg[idx - 10] + msg[idx - 27];
            msg[idx + 7] = msg[idx - 9] + msg[idx - 26];
            msg[idx + 8] = msg[idx - 8] + msg[idx - 21];
            msg[idx + 9] = msg[idx - 7] + msg[idx - 22];
            msg[idx + 10] = msg[idx - 6] + msg[idx - 24];
            msg[idx + 11] = msg[idx - 5] + msg[idx - 23];
            msg[idx + 12] = msg[idx - 4] + msg[idx - 17];
            msg[idx + 13] = msg[idx - 3] + msg[idx - 20];
            msg[idx + 14] = msg[idx - 2] + msg[idx - 19];
            msg[idx + 15] = msg[idx - 1] + msg[idx - 18];
        }
    }

    /**
     * Compress 함수에서 사용되는 message add & mix 연산
     *
     * @param stepidx 스텝 인덱스
     * @param alpha 상위 8워드에 적용할 왼쪽 회전값
     * @param beta 하위 8워드에 적용할 왼쪽 회전값
     */
    private void step(int stepidx, int alpha, int beta) {
        int vl, vr;
        for (int colidx = 0; colidx < 8; ++colidx) {
            vl = cv[colidx] ^ msg[16 * stepidx + colidx];
            vr = cv[colidx + 8] ^ msg[16 * stepidx + colidx + 8];
            vl = rol32(vl + vr, alpha) ^ STEP[8 * stepidx + colidx];
            vr = rol32(vl + vr, beta);
            tcv[colidx] = vr + vl;
            tcv[colidx + 8] = rol32(vr, GAMMA[colidx]);
        }

        wordPermutation();
    }

    /**
     * LSH의 word permutation 연산
     */
    private void wordPermutation() {
        cv[0] = tcv[6];
        cv[1] = tcv[4];
        cv[2] = tcv[5];
        cv[3] = tcv[7];
        cv[4] = tcv[12];
        cv[5] = tcv[15];
        cv[6] = tcv[14];
        cv[7] = tcv[13];
        cv[8] = tcv[2];
        cv[9] = tcv[0];
        cv[10] = tcv[1];
        cv[11] = tcv[3];
        cv[12] = tcv[8];
        cv[13] = tcv[11];
        cv[14] = tcv[10];
        cv[15] = tcv[9];
    }

    /**
     * 32비트 단위 왼쪽 회전 연산
     *
     * @param value 피연산자
     * @param rot 회전값
     * @return value를 rot만큼 왼쪽 회전한 값
     */
    private static int rol32(int value, int rot) {
        return (value << rot) | (value >>> (32 - rot));
    }
}
