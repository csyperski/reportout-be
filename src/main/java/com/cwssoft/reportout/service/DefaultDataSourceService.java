package com.cwssoft.reportout.service;

import com.cwssoft.reportout.dao.DataSourceDao;
import com.cwssoft.reportout.model.reports.DataSource;
import javax.inject.Inject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.cwssoft.reportout.util.StringUtils.isNullOrBlank;

/**
 *
 * @author csyperski
 */
@Service
public class DefaultDataSourceService extends BaseCrudService<DataSource, DataSourceDao> implements DataSourceService {

    @Override
    @Inject
    public void setDao(DataSourceDao dao) {
        super.setDao(dao);
    }

    @Override
    @Transactional(readOnly = false)
    public boolean update(DataSource item) {
        if (isValid(item)) {
            return super.update(item);
        }
        return false;
    }

    @Override
    @Transactional(readOnly = false)
    public boolean save(DataSource item) {
        if (isValid(item)) {
            return super.save(item);
        }
        return false;
    }

    protected boolean isValid(DataSource item) {
        if (item == null) {
            throw new NullPointerException("DataSource is null!");
        }

        if (isNullOrBlank(item.getDriverClass())) {
            throw new IllegalArgumentException("Invalid driver class");
        }

        if (isNullOrBlank(item.getConnectionString())) {
            throw new IllegalArgumentException("Invalid connection string");
        }

        return true;
    }

}
