package com.cwssoft.reportout.dao;

import java.util.List;
import java.util.Optional;
import javax.persistence.TypedQuery;

import com.cwssoft.reportout.model.settings.Setting;
import org.springframework.stereotype.Component;

/**
 *
 * @author csyperski
 */
@Component
public class DefaultSettingsDao extends GenericDaoOrm<Setting> implements SettingsDao {

    public DefaultSettingsDao() {
        super(Setting.class);
    }

    @Override
    public Optional<Setting> getSetting(String key) {
        TypedQuery<Setting> q = getEntityManager()
                .createQuery("select s from " + type.getSimpleName() + " s where s.settingKey = :settingKey", Setting.class);
        q.setParameter("settingKey", key);
        List<Setting> settings = q.getResultList();
        
        if (settings != null && settings.size() == 1) {
            return Optional.of(settings.get(0));
        }
        return Optional.empty();
    }
}
