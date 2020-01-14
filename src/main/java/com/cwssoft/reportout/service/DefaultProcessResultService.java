package com.cwssoft.reportout.service;

import com.cwssoft.reportout.dao.JobDao;
import com.cwssoft.reportout.dao.ProcessResultDao;
import com.cwssoft.reportout.model.user.User;
import com.cwssoft.reportout.processor.ProcessResult;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author csyperski
 */
@Slf4j
@Service
public class DefaultProcessResultService extends BaseCrudService<ProcessResult, ProcessResultDao> implements ProcessResultService {

    @Getter
    @Setter
    @Inject
    private JobDao jobDao;
    
    @Override
    @Inject
    public void setDao(ProcessResultDao dao) {
        super.setDao(dao);
    }

    @Override
    @Transactional(readOnly = false)
    public boolean update(ProcessResult item) {
        return false;
    }

    @Override
    @Transactional(readOnly = false)
    public boolean save(ProcessResult item) {
        if (item != null) {
            item.setData(null);                 // we don't want to persist the data set
            return super.save(item);
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessResult> getMostRecentResults(User user, int count) {
        return dao.getMostRecentResults(count * 5)
                .stream()
                .map(pr -> {
                    if (pr.getJobId() >= 0) {
                        pr.setJob(jobDao.get(pr.getJobId()).orElse(null));
                    }
                    return pr;
                })
                .filter(pr -> user != null && user.isEnabled() && pr.getJob() != null && (user.isAdministrator() || pr.getJob().getCreatorId() == user.getId() || pr.getJob().isPublicJob()))
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessResult> getResultsBetween(User user, Date date1, Date date2) {
        return dao.getResultsBetween(date1, date2)
                .stream()
                .map(pr -> {
                    if (pr.getJobId() >= 0) {
                        pr.setJob(jobDao.get(pr.getJobId()).orElse(null));
                    }
                    return pr;
                })
                .filter(pr -> user != null && user.isEnabled() && pr.getJob() != null && (user.isAdministrator() ||  pr.getJob().getCreatorId() == user.getId() || pr.getJob().isPublicJob()))
                .collect(Collectors.toList());
    }


}
