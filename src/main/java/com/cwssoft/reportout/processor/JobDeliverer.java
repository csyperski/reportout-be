/*
 *   2016 Charles Syperski <csyperski@cwssoft.com> - CWS Software LLC
 */
package com.cwssoft.reportout.processor;

import com.cwssoft.reportout.model.reports.Job;

/**
 *
 * @author csyperski
 */
public interface JobDeliverer {
    boolean canHandleJob(Job job);
    ProcessResult deliver(Job job, String result, long rows);
}
