/*
 *   2016 Charles Syperski <csyperski@cwssoft.com> - CWS Software LLC
 */
package com.cwssoft.reportout.model.version;

import lombok.Getter;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author csyperski
 */
public class SystemVersion implements Serializable {

    public static final int EXPIRE = 2 * 60 * 60 * 1000; // 2 HOURS

    @Getter
    private final String latestFrontendVersion;

    @Getter
    private final String latestBackendVersion;

    @Getter
    private final String currentBackendVersion;

    @Getter
    private final Date checkTime;

    public SystemVersion(String latestFrontendVersion, String latestBackendVersion, String currentBackendVersion, Date baseDate) {
        this.latestFrontendVersion = latestFrontendVersion;
        this.latestBackendVersion = latestBackendVersion;
        this.currentBackendVersion = currentBackendVersion;
        this.checkTime = baseDate;
    }

    public SystemVersion(String latestFrontendVersion, String latestBackendVersion, String currentBackendVersion) {
        this(latestFrontendVersion, latestBackendVersion, currentBackendVersion, new Date());
    }

    public boolean isExpired() {
        return checkTime == null || checkTime.getTime() + EXPIRE < System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "SystemVersion{" +
                "latestFrontendVersion='" + latestFrontendVersion + '\'' +
                ", latestBackendVersion='" + latestBackendVersion + '\'' +
                ", currentBackendVersion='" + currentBackendVersion + '\'' +
                ", checkTime=" + checkTime +
                '}';
    }
}
