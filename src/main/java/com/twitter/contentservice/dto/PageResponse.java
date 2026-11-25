package com.twitter.contentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.function.Function;

@Data
@AllArgsConstructor
public class PageResponse<T> {
    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 50;

    private List<T> items;
    private int limit;
    private long offset;
    private boolean hasMore;

    public static int normalizeLimit(int limit) {
        return Math.min(Math.max(limit, 1), MAX_LIMIT);
    }

    public static long normalizeOffset(long offset) {
        return Math.max(offset, 0);
    }

    public static <S, T> PageResponse<T> of(List<S> rows, int limit, long offset,
                                            Function<List<S>, List<T>> mapper) {
        boolean hasMore = rows.size() > limit;
        List<S> page = hasMore ? rows.subList(0, limit) : rows;
        return new PageResponse<>(mapper.apply(page), limit, offset, hasMore);
    }
}
