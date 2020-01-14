/*
 *   2014 Charles Syperski <csyperski@cwssoft.com> - CWS Software LLC
 */

package com.cwssoft.reportout.processor;

import org.springframework.stereotype.Component;

/**
 *
 * @author csyperski
 */
@Component
public class ProcessResultReplacer extends BaseReplacer implements StringReplacer {

    @Override
    public String requires() {
        return "processresult";
    }

    @Override
    public String process(String message, Object requiredObject, boolean escape) {
        if ( message != null && requiredObject != null && requiredObject instanceof ProcessResult ) {
            ProcessResult r = (ProcessResult)requiredObject;
            return message
                    .replace("${processResult.successful}", r.isSuccessful() ? "Yes" : "No")
                    .replace("${processResult.message}", escape(r.getMessage(), escape) )
                    .replace("${processResult.records}", String.valueOf(r.getRecords()) );
        }
        return message;
    }
    
}
