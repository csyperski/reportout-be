package com.cwssoft.reportout.dao;

import com.cwssoft.reportout.processor.ProcessResult;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 *
 * @author csyperski
 */
@Repository
public class DefaultProcessResultDao extends GenericDaoOrm<ProcessResult> implements ProcessResultDao {
    public DefaultProcessResultDao() {
        super(ProcessResult.class);
    }

    @Override
    public List<ProcessResult> getMostRecentResults(int count) {
        if ( count > 0 ) {
            return getEntityManager()
                    .createQuery("from " + type.getName() + " order by dateStarted desc")
                    .setMaxResults(count)
                    .getResultList();
        }
        return Collections.emptyList();
    }
    
    @Override
    public List<ProcessResult> getResultsBetween(Date date1, Date date2) {
        if ( date1 != null && date2 != null ) {
            return getEntityManager()
                    .createQuery("from " + type.getName() + " where dateStarted between :date1 and :date2 order by dateStarted desc")
                    .setParameter("date1", date1)
                    .setParameter("date2", date2)
                    .getResultList();
        }
        return Collections.emptyList();
    }
}
