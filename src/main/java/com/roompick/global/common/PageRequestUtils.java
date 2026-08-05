package com.roompick.global.common;

public final class PageRequestUtils {

    private static final int MIN_PAGE = 0;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 100;
    private static final int DEFAULT_SIZE = 20;

    private PageRequestUtils() {
    }

    public static int resolvePage(int page) {
        return page < MIN_PAGE ? MIN_PAGE : page;
    }

    public static int resolveSize(int size) {
        return (size < MIN_SIZE || size > MAX_SIZE) ? DEFAULT_SIZE : size;
    }
}
