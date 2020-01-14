package com.cwssoft.reportout.service;

import com.cwssoft.reportout.dao.JobDao;
import com.cwssoft.reportout.dao.JobScheduleDao;
import com.cwssoft.reportout.model.reports.Job;
import com.cwssoft.reportout.model.reports.JobSchedule;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.cwssoft.reportout.util.StringUtils.isNullOrBlank;

/**
 *
 * @author csyperski
 */
@Slf4j
@Service
public class DefaultJobService extends BaseCrudService<Job, JobDao> implements JobService {

    @Getter
    @Setter
    @Inject
    private JobScheduleDao jobScheduleDao;

    @Override
    @Inject
    public void setDao(JobDao dao) {
        super.setDao(dao);
    }

    @Override
    @Transactional(readOnly = false)
    public boolean update(Job item) {
        if (isValid(item)) {
            return super.update(item);
        }
        return false;
    }

    @Override
    @Transactional(readOnly = false)
    public boolean save(Job item) {
        if (isValid(item)) {
            return super.save(item);
        }
        return false;
    }

    protected boolean isValid(Job item) {
        if (item == null) {
            throw new NullPointerException("Job is null!");
        }

        if (isNullOrBlank(item.getName())) {
            throw new IllegalArgumentException("Invalid Name");
        }

        return true;
    }

    @Override
    @Transactional(readOnly = false)
    public boolean saveWithSchedule(Job job, Collection<JobSchedule> schedule) {
        if (job != null) {
            job.getJobSchedules().clear();
            if (save(job)) {
                if (schedule != null && schedule.size() > 0) {
                    return this.getItem(job.getId()).map(j -> {
                        return schedule.stream().map(s -> {
                            jobScheduleDao.save(new JobSchedule(s.getHour(), s.getDow(), j));
                            return true;
                        }).allMatch(b -> b);
                    }).orElse(Boolean.FALSE);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(readOnly = false)
    public boolean updateSchedule(Job job, Collection<JobSchedule> schedule) {
        if (isValid(job) && job.getId() >= 0) {

            // delete existing schedules
            List<JobSchedule> schedules = jobScheduleDao.getByJobId(job.getId());

            int sum = schedules.stream().mapToInt(s -> jobScheduleDao.delete(s.getId()) ? 1 : 0).sum();
            log.info("Removed {} job schedules for job: {}", sum, job.toString());
            schedule.stream().forEach(js -> jobScheduleDao.save(new JobSchedule(js, job)));
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> getAndPrepareAll() {
        List<Job> jobs = this.getAllItems();
        jobs.stream().forEachOrdered(j -> prepare(j));

        return jobs;
    }

    @Override
    @Transactional(readOnly = false)
    public Job prepare(Job item) {
        super.prepare(item);
        dao.initialize(item.getJobSchedules());
        return item;

    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> getAllJobsScheduledFor(int hour, int dow) {
        return this.getAllFilter(j -> j != null && j.isScheduledAt(hour, dow));
    }

}
