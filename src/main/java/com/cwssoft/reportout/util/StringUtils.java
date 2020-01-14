package com.cwssoft.reportout.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class StringUtils {

    public static String toSafeFileName(String s) {
        if (s != null) {
            return s.replaceAll("[^A-Za-z0-9_\\-\\(\\) \\.,]", "").trim();
        }
        return null;
    }

    public static Set<String> getEmailsAsSet(String input) {
        if (!StringUtils.isNullOrBlank(input)) {
            return StreamSupport.stream(Splitter.on(CharMatcher.anyOf(";|, "))
                    .trimResults()
                    .omitEmptyStrings()
                    .split(input).spliterator(), false)
                    .map( s -> s.toLowerCase(Locale.ENGLISH) )
                    .filter( StringUtils::isValidEmail )
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    public static boolean isValidEmail(String email) {
        if ( email != null ) {
            Pattern p = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-\\+]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$");
            return p.matcher(email).matches();
        }
        return false;
    }

    public static boolean isNullOrBlank(String s) {
        return s == null || s.trim().length() == 0;
    }


}
