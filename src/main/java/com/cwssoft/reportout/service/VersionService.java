package com.cwssoft.reportout.service;

import com.cwssoft.reportout.model.version.SystemVersion;
import java.io.Serializable;
import java.util.Optional;

/**
 *
 * @author csyperski
 */
public interface VersionService extends Serializable {
    Optional<SystemVersion> getLatestVersion(String current);
}
