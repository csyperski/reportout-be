package com.cwssoft.reportout.dao;

import com.cwssoft.reportout.model.reports.Job;
import org.springframework.stereotype.Repository;

/**
 *
 * @author csyperski
 */
@Repository
public class DefaultJobDao extends GenericDaoOrm<Job> implements JobDao {
    public DefaultJobDao() {
        super(Job.class);
        this.setDefaultOrderBy("name");
    }
}
