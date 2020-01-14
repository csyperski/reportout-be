package com.cwssoft.reportout.service;

import com.cwssoft.reportout.dao.ProcessResultDao;
import com.cwssoft.reportout.model.user.User;
import com.cwssoft.reportout.processor.ProcessResult;
import java.util.Date;
import java.util.List;

/**
 *
 * @author csyperski
 */
public interface ProcessResultService extends CrudService<ProcessResult, ProcessResultDao> {
    
    List<ProcessResult> getMostRecentResults(User user, int count);
    
    List<ProcessResult> getResultsBetween(User user, Date date1, Date date2);

}
