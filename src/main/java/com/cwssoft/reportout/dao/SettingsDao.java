/*
 *  2012 Charles Syperski <csyperski@cwssoft.com> - CWS Software LLC
 *
 */
package com.cwssoft.reportout.dao;

import com.cwssoft.reportout.model.settings.Setting;

import java.util.Optional;

/**
 * @author csyperski
 */
public interface SettingsDao extends BaseDao<Setting> {
    public static final String KEY_DATABASE_VERSION = "com.cwssoft.dbmanager.db.version";

    public Optional<Setting> getSetting(String key);
}
