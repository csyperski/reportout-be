package com.cwssoft.reportout.util;

import java.util.*;
import java.util.stream.Collectors;

public class SqlUtils {

    /**
     *
     * @param naughtyWords
     * @param sql
     * @return the word that matched the blacklist, Optional.empty() flags that the query is safe to execute
     */
    public static List<String> getBlackListedMatches(final Collection<String> naughtyWords, final String sql) {
        if ( sql != null && naughtyWords != null ) {
            String paddedSql = " " + sql.toLowerCase(Locale.ENGLISH);
            return naughtyWords.stream()
                    .map( word -> word.toLowerCase(Locale.ENGLISH))
                    .map( word -> " " + word.trim() + " ")
                    .filter( bl -> paddedSql.indexOf(bl) > -1)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
