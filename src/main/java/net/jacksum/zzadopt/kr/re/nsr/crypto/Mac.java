package net.jacksum.zzadopt.kr.re.nsr.crypto;

import net.jacksum.zzadopt.kr.re.nsr.crypto.mac.HMac;

/**
 * MAC 구현을 위한 인터페이스
 */
public abstract class Mac {

    /**
     * 초기화 함수
     *
     * @param key 비밀키
     */
    public abstract void init(byte[] key);

    /**
     * 새로운 메시지에 대한 MAC 계산을 위한 객체 초기화
     */
    public abstract void reset();

    /**
     * 메시지 추가
     *
     * @param msg 추가할 메시지
     */
    public abstract void update(byte[] msg);

    /**
     * 현재까지 추가된 메시지에 대한 MAC 계산
     *
     * @return MAC 값
     */
    public abstract byte[] doFinal();

    /**
     * 마지막 메시지를 포함하여 MAC 계산
     *
     * @param msg 마지막 메시지
     * @return MAC 값
     */
    public byte[] doFinal(byte[] msg) {
        update(msg);
        return doFinal();
    }

    /**
     * MAC 계산을 위한 객체 생성
     *
     * @param algorithm MessageDigest 알고리즘
     * @return Mac 객체
     */
    public static Mac getInstance(Hash.Algorithm algorithm) {
        Hash md = Hash.getInstance(algorithm);
        return new HMac(md);
    }

}
