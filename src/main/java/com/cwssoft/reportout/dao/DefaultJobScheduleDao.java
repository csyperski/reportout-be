package com.cwssoft.reportout.dao;

import com.cwssoft.reportout.model.reports.JobSchedule;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 *
 * @author csyperski
 */
@Repository
public class DefaultJobScheduleDao extends GenericDaoOrm<JobSchedule> implements JobScheduleDao {
    public DefaultJobScheduleDao() {
        super(JobSchedule.class);
    }

    @Override
    public List<JobSchedule> getByJobId(long jobId) {
        return getEntityManager()
                .createQuery("from " + type.getName() + " js where js.job.id = :jobid")
                .setParameter("jobid", jobId)
                .getResultList();
    }

}
