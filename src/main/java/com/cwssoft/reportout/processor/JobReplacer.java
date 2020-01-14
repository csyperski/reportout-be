/*
 *   2014 Charles Syperski <csyperski@cwssoft.com> - CWS Software LLC
 */

package com.cwssoft.reportout.processor;

import com.cwssoft.reportout.model.reports.Job;
import org.springframework.stereotype.Component;

/**
 *
 * @author csyperski
 */
@Component
public class JobReplacer extends BaseReplacer implements StringReplacer {

    @Override
    public String requires() {
        return "job";
    }

    @Override
    public String process(String message, Object requiredObject, boolean escape) {
        if ( message != null && requiredObject != null && requiredObject instanceof Job ) {
            Job j = (Job)requiredObject;
            return message
                    .replace("${job.title}", escape(j.getName(), escape) )
                    .replace("${job.description}", escape(j.getDescription(), escape) );
        }
        return message;
    }
    
}
