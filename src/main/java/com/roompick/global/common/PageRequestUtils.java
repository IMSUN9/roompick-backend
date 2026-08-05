package com.roompick.global.common;

/**
 * 페이지네이션 파라미터(page, size)를 안전한 범위로 보정하는 유틸리티입니다.
 */
public final class PageRequestUtils {

    private static final int MIN_PAGE = 0;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 100;
    private static final int DEFAULT_SIZE = 20;

    private PageRequestUtils() {
    }

    /**
     * page가 0보다 작으면 0으로 보정합니다.
     */
    public static int resolvePage(int page) {
        return page < MIN_PAGE ? MIN_PAGE : page;
    }

    /**
     * size가 1~100 범위를 벗어나면 기본값(20)으로 보정합니다.
     */
    public static int resolveSize(int size) {
        return (size < MIN_SIZE || size > MAX_SIZE) ? DEFAULT_SIZE : size;
    }
}
