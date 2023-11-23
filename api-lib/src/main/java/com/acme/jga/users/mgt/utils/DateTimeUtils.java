package com.acme.jga.users.mgt.utils;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeUtils {

    public static String nowIso() {
        return DateTimeFormatter.ISO_DATE_TIME.format(Instant.now());
    }

}
