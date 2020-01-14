package com.cwssoft.reportout.db;

import java.util.Optional;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.cwssoft.reportout.dao.SettingsDao;
import com.cwssoft.reportout.model.settings.Setting;
import com.cwssoft.reportout.service.SettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author csyperski
 */
public abstract class BaseUpdatePackage implements UpdatePackage {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private SettingsService settingsService;

    private static final Logger logger = LoggerFactory.getLogger(BaseUpdatePackage.class);

    protected abstract String[] getQueries();

    // used to update schema via objects
    protected boolean runNonQueryBasedUpgrades() {
        return true;
    }


    @Transactional
    public boolean updateSchemaVersion() {
        logger.info("updating schema version");
        Optional<Setting> setting = settingsService.getSetting(SettingsDao.KEY_DATABASE_VERSION);
        if (!setting.isPresent()) {
            logger.info("Inserting new schema version setting");
            settingsService.save(new Setting(SettingsDao.KEY_DATABASE_VERSION, String.valueOf(getToVersion())));
        } else {
            logger.info("Updating schema version setting");
            Setting s = setting.get();
            s.setSettingValueAsInt(getToVersion());
            settingsService.update(s);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean runUpgrade() {
        if (getQueries() != null) {
            try {
                for (String query : getQueries()) {
                    if (query != null) {
                        logger.info("Running: " + query);
                        entityManager.createNativeQuery(query).executeUpdate();
                    }
                }
                return runNonQueryBasedUpgrades() && updateSchemaVersion();
            } catch (Exception hex) {
                logger.error(hex.getMessage(), hex);
                DbUpdateException dbe = new DbUpdateException(hex.getMessage());
                dbe.setStackTrace(hex.getStackTrace());
                throw dbe;
            }
        }
        return false;
    }

    /**
     * @return the toVersion
     */
    @Override
    public int getToVersion() {
        return getFromVersion() + 1;
    }


    /**
     * @return the entityManager
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * @param entityManager the entityManager to set
     */
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * @return the settingsService
     */
    public SettingsService getSettingsService() {
        return settingsService;
    }

    /**
     * @param settingsService the settingsService to set
     */
    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

}
