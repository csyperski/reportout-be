package com.cwssoft.reportout.dao;

import com.cwssoft.reportout.model.reports.JobSchedule;
import java.util.List;

/**
 *
 * @author csyperski
 */
public interface JobScheduleDao extends BaseDao<JobSchedule> {
    
    List<JobSchedule> getByJobId(long jobId);
    
}
