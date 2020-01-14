package com.cwssoft.reportout.dao;

import com.cwssoft.reportout.model.reports.DataSource;
import org.springframework.stereotype.Repository;

/**
 *
 * @author csyperski
 */
@Repository
public class DefaultDataSourceDao extends GenericDaoOrm<DataSource> implements DataSourceDao {
    public DefaultDataSourceDao() {
        super(DataSource.class);
        setDefaultOrderBy("name");
    }
}
