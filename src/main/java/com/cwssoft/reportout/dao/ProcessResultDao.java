package com.cwssoft.reportout.dao;

import com.cwssoft.reportout.processor.ProcessResult;
import java.util.Date;
import java.util.List;

/**
 *
 * @author csyperski
 */
public interface ProcessResultDao extends BaseDao<ProcessResult> {
    
    List<ProcessResult> getMostRecentResults(int count);
    
    List<ProcessResult> getResultsBetween(Date date1, Date date2);
    
}
