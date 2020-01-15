package com.cwssoft.reportout.db;

import com.cwssoft.reportout.dao.SettingsDao;
import com.cwssoft.reportout.service.SettingsService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
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
                .mapToInt(up -> up.getToVersion())
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
        if (packages != null) {
            final List<UpdatePackage> sortedPackages = packages.stream()
                    .filter(p -> p != null)
                    .sorted(Comparator.comparing(UpdatePackage::getFromVersion))
                    .collect(Collectors.toList());

            int current = from;
            for (UpdatePackage p : sortedPackages) {
                if (current == p.getFromVersion()) {
                    if (!p.runUpgrade()) {
                        throw new DbUpdateException("Failed to update schema from " + current + " to " + p.getToVersion());
                    } else {
                        current = getCurrentSchemaVersion();
                        log.info(" ** Current database version = " + current);
                    }
                }
            }
        }
        return true;
    }
}
