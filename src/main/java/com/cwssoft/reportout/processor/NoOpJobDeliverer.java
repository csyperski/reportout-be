/*
 *   2016 Charles Syperski <csyperski@cwssoft.com> - CWS Software LLC
 */
package com.cwssoft.reportout.processor;

import com.cwssoft.reportout.model.reports.Job;

import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author csyperski
 */
@Component
public class NoOpJobDeliverer implements JobDeliverer {
    
    private static final Logger logger = LoggerFactory.getLogger(NoOpJobDeliverer.class);
    
    private int maxSize = 31457280;  // 30mb
    
    @Override
    public boolean canHandleJob(Job job) {
        return job != null && job.getJobAction() == Job.ACTION_NONE;
    }
    
    @Override
    public ProcessResult deliver(Job job, String result, long rowCount) {
        if (job != null && result != null) {
            try {
                byte[] data = result.getBytes("UTF-8");
                if (data.length > maxSize) {
                    return new ProcessResult(false, "File too large for download: file size: " + data.length + " max size: " + maxSize);
                }
                
                ProcessResult pr = new ProcessResult(true, "Task complete!", Base64.getEncoder().encodeToString(data));
                pr.setRecords(rowCount);
                return pr;
            } catch (Exception e) {
                logger.warn("Error during Job Deliverer: {}", e.getMessage(), e);
                return new ProcessResult(false, "Unable to process job: " + e.getMessage());
            }
        } else {
            return new ProcessResult(false, "Either job or result was null!");
        }
    }
}