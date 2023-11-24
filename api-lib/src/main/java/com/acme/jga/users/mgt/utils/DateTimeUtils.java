package com.acme.jga.users.mgt.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeUtils {

    public static LocalDateTime nowIso() {
        return Instant.now().atZone(ZoneOffset.UTC).toLocalDateTime();
    }

}
