package com.cwssoft.reportout.db;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import com.cwssoft.reportout.dao.SettingsDao;
import com.cwssoft.reportout.service.SettingsService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author csyperski
 */
@Slf4j
@Component
@Service
public class DefaultDatabaseManager implements DatabaseManager {

    @Getter
    @Setter
    @Inject
    private SettingsService settingsService;

    @Getter
    @Setter
    @Inject
    protected List<UpdatePackage> packages;

    @Override
    public int getCurrentSchemaVersion() {
        try {
            return settingsService.getSetting(SettingsDao.KEY_DATABASE_VERSION)
                    .map(s -> s.getSettingValueAsInt())
                    .orElse(DatabaseManager.NOT_INSTALLED);
        } catch (Exception e) {
            log.warn("Unable to get schema version, is the database installed?");
            return DatabaseManager.NOT_INSTALLED;
        }
    }

    @Override
    public int getTargetVersion() {
        return getPackages()
                .stream()
                .mapToInt(up -> up.getToVersion() )
                .max()
                .orElse(NOT_INSTALLED);
    }

    @Override
    public boolean update() throws DbUpdateException {
        log.info("Starting database update process.");
        int from = getCurrentSchemaVersion();
        int to = getTargetVersion();
        if (from != to) {
            updateInternal(from, to);
        } else {
            log.info("Database is up to date, no update was needed!");
        }
        return getCurrentSchemaVersion() == getTargetVersion();
    }

    @Transactional
    @Override
    public boolean updateInternal(int from, int to) throws DbUpdateException {
        log.info("Upgrading from version " + from + " to " + to + ".");
        for (UpdatePackage p : (packages != null ? packages : new ArrayList<UpdatePackage>() ) ) {
            if (p == null) {
                continue;
            }

            if (from == p.getFromVersion()) {
                if (!p.runUpgrade()) {
                    throw new DbUpdateException("Failed to update schema from " + from + " to " + p.getToVersion());
                } else {
                    from = getCurrentSchemaVersion();
                    log.info("Current database version = " + from);
                }
            }
        }
        return true;
    }
}
