package com.jiubuntu.wms.biz.user.application;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 계정 발급 시 서버가 생성하는 임시 비밀번호. UserIssueRequest의 비밀번호 정책
 * (영문/숫자/특수문자 각 1자 이상 포함, 8자 이상)을 항상 만족하도록 각 종류를 최소 1자씩 채운 뒤 섞는다.
 */
@Component
public class InitialPasswordGenerator {

    private static final int LENGTH = 10;
    private static final String LETTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String SPECIALS = "!@#$%^&*";
    private static final String ALL = LETTERS + DIGITS + SPECIALS;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        List<Character> characters = new ArrayList<>();
        characters.add(pick(LETTERS));
        characters.add(pick(DIGITS));
        characters.add(pick(SPECIALS));
        for (int i = characters.size(); i < LENGTH; i++) {
            characters.add(pick(ALL));
        }
        Collections.shuffle(characters, random);

        StringBuilder result = new StringBuilder(LENGTH);
        characters.forEach(result::append);
        return result.toString();
    }

    private char pick(String pool) {
        return pool.charAt(random.nextInt(pool.length()));
    }

}
