package net.jacksum.zzadopt.kr.re.nsr.crypto.hash;

import java.util.Arrays;

import net.jacksum.zzadopt.kr.re.nsr.crypto.Hash;
import net.jacksum.zzadopt.kr.re.nsr.crypto.util.PackLE;

/**
 * LSH512 Algorithm implementation
 *
 * Word length: 64-bit (8-byte)
 * Chain variable length: 1024-bit (128-byte)
 * Message block length: 2048-bit (256-byte)
 */
public class LSH512 extends Hash {

    //@formatter:off
    private static final int BLOCKSIZE = 256;

    private static final int NUMSTEP = 28;

    /// 사전 계산된 224비트 출력용 IV
    private static final long IV224[] = {
        0x0c401e9fe8813a55l, 0x4a5f446268fd3d35l, 0xff13e452334f612al, 0xf8227661037e354al,
        0xa5f223723c9ca29dl, 0x95d965a11aed3979l, 0x01e23835b9ab02ccl, 0x52d49cbad5b30616l,
        0x9e5c2027773f4ed3l, 0x66a5c8801925b701l, 0x22bbc85b4c6779d9l, 0xc13171a42c559c23l,
        0x31e2b67d25be3813l, 0xd522c4deed8e4d83l, 0xa79f5509b43fbafel, 0xe00d2cd88b4b6c6al,};

    /// 사전 계산된 256비트 출력용 IV
    private static final long IV256[] = {
        0x6dc57c33df989423l, 0xd8ea7f6e8342c199l, 0x76df8356f8603ac4l, 0x40f1b44de838223al,
        0x39ffe7cfc31484cdl, 0x39c4326cc5281548l, 0x8a2ff85a346045d8l, 0xff202aa46dbdd61el,
        0xcf785b3cd5fcdb8bl, 0x1f0323b64a8150bfl, 0xff75d972f29ea355l, 0x2e567f30bf1ca9e1l,
        0xb596875bf8ff6dbal, 0xfcca39b089ef4615l, 0xecff4017d020b4b6l, 0x7e77384c772ed802l,};

    /// 사전 계산된 384비트 출력용 IV
    private static final long IV384[] = {
        0x53156a66292808f6l, 0xb2c4f362b204c2bcl, 0xb84b7213bfa05c4el, 0x976ceb7c1b299f73l,
        0xdf0cc63c0570ae97l, 0xda4441baa486ce3fl, 0x6559f5d9b5f2acc2l, 0x22dacf19b4b52a16l,
        0xbbcdacefde80953al, 0xc9891a2879725b3el, 0x7c9fe6330237e440l, 0xa30ba550553f7431l,
        0xbb08043fb34e3e30l, 0xa0dec48d54618eadl, 0x150317267464bc57l, 0x32d1501fde63dc93l,};

    /// 사전 계산된 512비트 출력용 IV
    private static final long IV512[] = {
        0xadd50f3c7f07094eL, 0xe3f3cee8f9418a4fL, 0xb527ecde5b3d0ae9L, 0x2ef6dec68076f501L,
        0x8cb994cae5aca216L, 0xfbb9eae4bba48cc7L, 0x650a526174725feaL, 0x1f9a61a73f8d8085L,
        0xb6607378173b539bL, 0x1bc99853b0c0b9edL, 0xdf727fc19b182d47L, 0xdbef360cf893a457L,
        0x4981f5e570147e80L, 0xd00c4490ca7d3e30L, 0x5d73940c0e4ae1ecL, 0x894085e2edb2d819L,};

    /// STEP 상수
    private static final long STEP[] = {
        0x97884283c938982aL, 0xba1fca93533e2355L, 0xc519a2e87aeb1c03L, 0x9a0fc95462af17b1L,
        0xfc3dda8ab019a82bL, 0x02825d079a895407L, 0x79f2d0a7ee06a6f7L, 0xd76d15eed9fdf5feL,
        0x1fcac64d01d0c2c1L, 0xd9ea5de69161790fL, 0xdebc8b6366071fc8L, 0xa9d91db711c6c94bL,
        0x3a18653ac9c1d427L, 0x84df64a223dd5b09L, 0x6cc37895f4ad9e70L, 0x448304c8d7f3f4d5L,
        0xea91134ed29383e0L, 0xc4484477f2da88e8L, 0x9b47eec96d26e8a6L, 0x82f6d4c8d89014f4L,
        0x527da0048b95fb61L, 0x644406c60138648dL, 0x303c0e8aa24c0edcL, 0xc787cda0cbe8ca19L,
        0x7ba46221661764caL, 0x0c8cbc6acd6371acL, 0xe336b836940f8f41L, 0x79cb9da168a50976L,
        0xd01da49021915cb3L, 0xa84accc7399cf1f1L, 0x6c4a992cee5aeb0cL, 0x4f556e6cb4b2e3e0L,
        0x200683877d7c2f45L, 0x9949273830d51db8L, 0x19eeeecaa39ed124L, 0x45693f0a0dae7fefL,
        0xedc234b1b2ee1083L, 0xf3179400d68ee399L, 0xb6e3c61b4945f778L, 0xa4c3db216796c42fL,
        0x268a0b04f9ab7465L, 0xe2705f6905f2d651L, 0x08ddb96e426ff53dL, 0xaea84917bc2e6f34L,
        0xaff6e664a0fe9470L, 0x0aab94d765727d8cL, 0x9aa9e1648f3d702eL, 0x689efc88fe5af3d3L,
        0xb0950ffea51fd98bL, 0x52cfc86ef8c92833L, 0xe69727b0b2653245L, 0x56f160d3ea9da3e2L,
        0xa6dd4b059f93051fL, 0xb6406c3cd7f00996L, 0x448b45f3ccad9ec8L, 0x079b8587594ec73bL,
        0x45a50ea3c4f9653bL, 0x22983767c1f15b85L, 0x7dbed8631797782bL, 0x485234be88418638L,
        0x842850a5329824c5L, 0xf6aca914c7f9a04cL, 0xcfd139c07a4c670cL, 0xa3210ce0a8160242L,
        0xeab3b268be5ea080L, 0xbacf9f29b34ce0a7L, 0x3c973b7aaf0fa3a8L, 0x9a86f346c9c7be80L,
        0xac78f5d7cabcea49L, 0xa355bddcc199ed42L, 0xa10afa3ac6b373dbL, 0xc42ded88be1844e5L,
        0x9e661b271cff216aL, 0x8a6ec8dd002d8861L, 0xd3d2b629beb34be4L, 0x217a3a1091863f1aL,
        0x256ecda287a733f5L, 0xf9139a9e5b872fe5L, 0xac0535017a274f7cL, 0xf21b7646d65d2aa9L,
        0x048142441c208c08L, 0xf937a5dd2db5e9ebL, 0xa688dfe871ff30b7L, 0x9bb44aa217c5593bL,
        0x943c702a2edb291aL, 0x0cae38f9e2b715deL, 0xb13a367ba176cc28L, 0x0d91bd1d3387d49bL,
        0x85c386603cac940cL, 0x30dd830ae39fd5e4L, 0x2f68c85a712fe85dL, 0x4ffeecb9dd1e94d6L,
        0xd0ac9a590a0443aeL, 0xbae732dc99ccf3eaL, 0xeb70b21d1842f4d9L, 0x9f4eda50bb5c6fa8L,
        0x4949e69ce940a091L, 0x0e608dee8375ba14L, 0x983122cba118458cL, 0x4eeba696fbb36b25L,
        0x7d46f3630e47f27eL, 0xa21a0f7666c0dea4L, 0x5c22cf355b37cec4L, 0xee292b0c17cc1847L,
        0x9330838629e131daL, 0x6eee7c71f92fce22L, 0xc953ee6cb95dd224L, 0x3a923d92af1e9073L,
        0xc43a5671563a70fbL, 0xbc2985dd279f8346L, 0x7ef2049093069320L, 0x17543723e3e46035L,
        0xc3b409b00b130c6dL, 0x5d6aee6b28fdf090L, 0x1d425b26172ff6edL, 0xcccfd041cdaf03adL,
        0xfe90c7c790ab6cbfL, 0xe5af6304c722ca02L, 0x70f695239999b39eL, 0x6b8b5b07c844954cL,
        0x77bdb9bb1e1f7a30L, 0xc859599426ee80edL, 0x5f9d813d4726e40aL, 0x9ca0120f7cb2b179L,
        0x8f588f583c182cbdL, 0x951267cbe9eccce7L, 0x678bb8bd334d520eL, 0xf6e662d00cd9e1b7L,
        0x357774d93d99aaa7L, 0x21b2edbb156f6eb5L, 0xfd1ebe846e0aee69L, 0x3cb2218c2f642b15L,
        0xe7e7e7945444ea4cL, 0xa77a33b5d6b9b47cL, 0xf34475f0809f6075L, 0xdd4932dce6bb99adL,
        0xacec4e16d74451dcL, 0xd4a0a8d084de23d6L, 0x1bdd42f278f95866L, 0xeed3adbb938f4051L,
        0xcfcf7be8992f3733L, 0x21ade98c906e3123L, 0x37ba66711fffd668L, 0x267c0fc3a255478aL,
        0x993a64ee1b962e88L, 0x754979556301faaaL, 0xf920356b7251be81L, 0xc281694f22cf923fL,
        0x9f4b6481c8666b02L, 0xcf97761cfe9f5444L, 0xf220d7911fd63e9fL, 0xa28bd365f79cd1b0L,
        0xd39f5309b1c4b721L, 0xbec2ceb864fca51fL, 0x1955a0ddc410407aL, 0x43eab871f261d201L,
        0xeaafe64a2ed16da1L, 0x670d931b9df39913L, 0x12f868b0f614de91L, 0x2e5f395d946e8252L,
        0x72f25cbb767bd8f4L, 0x8191871d61a1c4ddL, 0x6ef67ea1d450ba93L, 0x2ea32a645433d344L,
        0x9a963079003f0f8bL, 0x74a0aeb9918cac7aL, 0x0b6119a70af36fa3L, 0x8d9896f202f0d480L,
        0x654f1831f254cd66L, 0x1318a47f0366a25eL, 0x65752076250b4e01L, 0xd1cd8eb888071772L,
        0x30c6a9793f4e9b25L, 0x154f684b1e3926eeL, 0x6c7ac0b1fe6312aeL, 0x262f88f4f3c5550dL,
        0xb4674a24472233cbL, 0x2bbd23826a090071L, 0xda95969b30594f66L, 0x9f5c47408f1e8a43L,
        0xf77022b88de9c055L, 0x64b7b36957601503L, 0xe73b72b06175c11aL, 0x55b87de8b91a6233L,
        0x1bb16e6b6955ff7fL, 0xe8e0a5ec7309719cL, 0x702c31cb89a8b640L, 0xfba387cfada8cde2L,
        0x6792db4677aa164cL, 0x1c6b1cc0b7751867L, 0x22ae2311d736dc01L, 0x0e3666a1d37c9588L,
        0xcd1fd9d4bf557e9aL, 0xc986925f7c7b0e84L, 0x9c5dfd55325ef6b0L, 0x9f2b577d5676b0ddL,
        0xfa6e21be21c062b3L, 0x8787dd782c8d7f83L, 0xd0d134e90e12dd23L, 0x449d087550121d96L,
        0xecf9ae9414d41967L, 0x5018f1dbf789934dL, 0xfa5b52879155a74cL, 0xca82d4d3cd278e7cL,
        0x688fdfdfe22316adL, 0x0f6555a4ba0d030aL, 0xa2061df720f000f3L, 0xe1a57dc5622fb3daL,
        0xe6a842a8e8ed8153L, 0x690acdd3811ce09dL, 0x55adda18e6fcf446L, 0x4d57a8a0f4b60b46L,
        0xf86fbfc20539c415L, 0x74bafa5ec7100d19L, 0xa824151810f0f495L, 0x8723432791e38ebbL,
        0x8eeaeb91d66ed539L, 0x73d8a1549dfd7e06L, 0x0387f2ffe3f13a9bL, 0xa5004995aac15193L,
        0x682f81c73efdda0dL, 0x2fb55925d71d268dL, 0xcc392d2901e58a3dL, 0xaa666ab975724a42L
    };

    private static final int ALPHA_EVEN = 23;
    private static final int ALPHA_ODD = 7;

    private static final int BETA_EVEN = 59;
    private static final int BETA_ODD = 3;

    private static final int GAMMA[] = {0, 16, 32, 48, 8, 24, 40, 56};
    //@formatter:on

    private long[] cv;
    private long[] tcv;
    private long[] msg;
    private byte[] block;

    private int boff;
    private int outlenbits;

    /**
     * LSH512 기본 생성자, 512비트 출력으로 설정
     */
    public LSH512() {
        this(512);
    }

    /**
     * LSH512 생성자
     *
     * @param outlenbits 출력 길이, 비트 단위
     */
    public LSH512(int outlenbits) {
        if (outlenbits < 0 || outlenbits > 512) {
            throw new RuntimeException("invalid hash length");
        }

        cv = new long[16];
        tcv = new long[16];
        msg = new long[16 * (NUMSTEP + 1)];
        block = new byte[BLOCKSIZE];
        this.outlenbits = outlenbits;

        init();
    }

    /**
     * 같은 출력길이를 가지는 객체를 만들어 리턴한다.
     *
     * @return LSH512객체
     */
    @Override
    public Hash newInstance() {
        return new LSH512(this.outlenbits);
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

            case 384:
                System.arraycopy(IV384, 0, cv, 0, cv.length);
                break;

            case 512:
                System.arraycopy(IV512, 0, cv, 0, cv.length);
                break;

            default:
                generateIV();
                break;
        }
    }

    /**
     * 내부 블록 크기 리턴
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
     * @param lenbits 데이터 길이 (비트)
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

        long[] temp = new long[8];
        for (int i = 0; i < temp.length; ++i) {
            temp[i] = cv[i] ^ cv[i + 8];
        }

        reset();

        rbytes = outlenbits >> 3;
        rbits = outlenbits & 0x7;
        byte[] result = new byte[rbits > 0 ? rbytes + 1 : rbytes];
        for (int i = 0; i < result.length; ++i) {
            result[i] = (byte) (temp[i >> 3] >> ((i << 3) & 0x3f));
        }

        if (rbits > 0) {
            result[rbytes] = (byte) (result[rbytes] & (0xff << (8 - rbits)));
        }

        return result;
    }

    /**
     * IV 생성
     */
    private void generateIV() {
        Arrays.fill(cv, (byte) 0);
        Arrays.fill(block, (byte) 0);

        cv[0] = 64;
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
        PackLE.toU64(in, offset, msg, 0, 32);

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

        long vl, vr;
        for (int colidx = 0; colidx < 8; ++colidx) {
            vl = cv[colidx] ^ msg[16 * stepidx + colidx];
            vr = cv[colidx + 8] ^ msg[16 * stepidx + colidx + 8];
            vl = rol64(vl + vr, alpha) ^ STEP[8 * stepidx + colidx];
            vr = rol64(vl + vr, beta);
            tcv[colidx] = vr + vl;
            tcv[colidx + 8] = rol64(vr, GAMMA[colidx]);
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
     * 64-bit unit left rotation operation
     *
     * @param value operand
     * @param rot Rotation value
     * @return Value rotated left by rot
     */
    private static long rol64(long value, int rot) {
        return (value << rot) | (value >>> (64 - rot));
    }

}
