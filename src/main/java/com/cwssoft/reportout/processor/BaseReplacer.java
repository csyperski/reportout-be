/*
 *   2014 Charles Syperski <csyperski@cwssoft.com> - CWS Software LLC
 */

package com.cwssoft.reportout.processor;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author csyperski
 */
public abstract class BaseReplacer {
    protected String escape(String s, boolean escape) {
        if ( s != null && escape ) {
            return StringEscapeUtils.escapeHtml4(s);
        }
        return s;
    }
}
