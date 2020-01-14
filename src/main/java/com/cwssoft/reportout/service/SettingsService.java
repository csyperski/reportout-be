package com.cwssoft.reportout.service;


import com.cwssoft.reportout.dao.SettingsDao;
import com.cwssoft.reportout.model.settings.Setting;

import java.util.Optional;

/**
 * @author csyperski
 */
public interface SettingsService extends CrudService<Setting, SettingsDao> {
    public Optional<Setting> getSetting(String key);
}
