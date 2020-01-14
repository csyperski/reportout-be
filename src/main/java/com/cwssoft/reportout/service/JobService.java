package com.cwssoft.reportout.service;

import com.cwssoft.reportout.dao.JobDao;
import com.cwssoft.reportout.model.reports.Job;
import com.cwssoft.reportout.model.reports.JobSchedule;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author csyperski
 */
public interface JobService extends CrudService<Job, JobDao> {
    public boolean saveWithSchedule(Job job, Collection<JobSchedule> schedule);
    public boolean updateSchedule(Job job, Collection<JobSchedule> schedule);
    public List<Job> getAndPrepareAll();
    public List<Job> getAllJobsScheduledFor(int hour, int dow);
}
