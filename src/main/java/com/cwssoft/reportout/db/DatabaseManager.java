/*
 *  2012 Charles Syperski <csyperski@cwssoft.com>
 *  
 */
package com.cwssoft.reportout.db;

import com.cwssoft.reportout.service.SettingsService;

import java.util.List;

/**
 *
 * @author csyperski
 */
public interface DatabaseManager
{
    int NOT_INSTALLED = 0;

    int getCurrentSchemaVersion();

    List<UpdatePackage> getPackages();

    /**
     * @return the settingsDao
     */
    SettingsService getSettingsService();

    int getTargetVersion();

    void setPackages(List<UpdatePackage> packages);

    void setSettingsService(SettingsService settingsService);

    boolean update() throws DbUpdateException;

    boolean updateInternal(int from, int to) throws DbUpdateException;
    
}
