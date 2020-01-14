/*
 *   2016 Charles Syperski <csyperski@cwssoft.com> - CWS Software LLC
 */
package com.cwssoft.reportout.tasks;

import com.cwssoft.reportout.model.reports.Job;
import com.cwssoft.reportout.processor.JobProcessor;
import com.cwssoft.reportout.processor.ProcessResult;
import com.cwssoft.reportout.service.JobService;
import com.cwssoft.reportout.service.ProcessResultService;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author csyperski
 */
@Component
@Slf4j
public class TaskRunner {

    @Inject
    @Getter
    @Setter
    private JobService jobService;

    @Inject
    @Getter
    @Setter
    private ProcessResultService processResultService;

    @Inject
    @Getter
    @Setter
    private JobProcessor jobProcessor;

    @Scheduled(cron="1 0 * * * *") // hourly
    public void reportCurrentTime() {

        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int dow = c.get(Calendar.DAY_OF_WEEK);

        log.info("Starting execution of scheduled tasks for Hour: {} - DOW: {}", hour, dow);

        List<Job> jobs = jobService.getAllJobsScheduledFor(hour, dow);
        log.info("Found {} jobs to process.", jobs.size());

        List<ProcessResult> results = jobs.stream()
                .filter(j -> j.isFullConfiguredForAction() && 
                             j.isReady() && 
                             ( j.getJobAction() == Job.ACTION_FTP || j.getJobAction() == Job.ACTION_SFTP || j.getJobAction() == Job.ACTION_EMAIL))
                .map(j -> {

                    final Date startTime = new Date();
                    ProcessResult pr = jobProcessor.execute(j);
                    pr.setDateStarted(startTime);
                    pr.setDateCompleted(new Date());
                    pr.setExecutedBy(-1l);
                    pr.setExecutedByScheduler(true);
                    pr.setJobId(j.getId());
                    pr.setData(null);

                    processResultService.save(pr);
                    log.info("Job status for: {} - {} ({})", j, pr.isSuccessful(), pr.getMessage());
                    return pr;
                }).collect(Collectors.toList());
    }
}