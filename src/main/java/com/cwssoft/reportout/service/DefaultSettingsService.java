package com.cwssoft.reportout.service;

import com.cwssoft.reportout.dao.SettingsDao;
import com.cwssoft.reportout.model.settings.Setting;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Optional;

/**
 * @author csyperski
 */
@Component
@Service
public class DefaultSettingsService extends BaseCrudService<Setting, SettingsDao> implements SettingsService {


    @Override
    @Inject
    public void setDao(SettingsDao dao) {
        this.dao = dao;
    }

    @Override
    public SettingsDao getDao() {
        return dao;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Setting> getSetting(String key) {
        return dao.getSetting(key);
    }

}
