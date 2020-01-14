package com.cwssoft.reportout.db;

/**
 *
 * @author csyperski
 */
public interface UpdatePackage {

    int getFromVersion();

    /**
     * @return the toVersion
     */
    int getToVersion();
    
    
    boolean runUpgrade();
}
