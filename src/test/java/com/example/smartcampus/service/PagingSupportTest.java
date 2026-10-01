package com.example.smartcampus.service;

import com.example.smartcampus.util.PagingSupport;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PagingSupportTest {

    private static final Set<String> ALLOWED = Set.of("name", "studentId");

    @Test
    void unknownSortField_fallsBackToDefault() {
        Pageable pageable = PagingSupport.pageable(0, 10, "password", "asc", ALLOWED, "studentId");
        assertEquals("studentId", pageable.getSort().iterator().next().getProperty());
    }

    @Test
    void invalidPageAndSize_areCorrected() {
        Pageable pageable = PagingSupport.pageable(-5, 5000, "name", "desc", ALLOWED, "studentId");
        assertEquals(0, pageable.getPageNumber());
        assertEquals(100, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("name").getDirection());
    }
}
