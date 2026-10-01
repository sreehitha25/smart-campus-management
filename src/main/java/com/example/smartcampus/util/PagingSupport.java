package com.example.smartcampus.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class PagingSupport {

    private static final int MAX_PAGE_SIZE = 100;

    private PagingSupport() {
    }

    /**
     * Builds a safe Pageable: negative pages and huge sizes are corrected, and only
     * whitelisted fields can be sorted on (so a client cannot sort by an unknown field).
     */
    public static Pageable pageable(int page, int size, String sortBy, String direction,
                                    Set<String> allowedSortFields, String defaultSortField) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        String field = sortBy != null && allowedSortFields.contains(sortBy) ? sortBy : defaultSortField;
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(safePage, safeSize, Sort.by(sortDirection, field).and(Sort.by("id")));
    }
}
