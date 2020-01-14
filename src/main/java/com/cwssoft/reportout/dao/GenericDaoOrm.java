/*
 *  Copyright (C)  2014 Charles Syperski <csyperski@cwssoft.com>
 *  
 */
package com.cwssoft.reportout.dao;

import com.cwssoft.reportout.model.DomainObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author csyperski
 */
public class GenericDaoOrm<T extends DomainObject> implements BaseDao<T> {

    protected Class<T> type;

    @PersistenceContext
    private EntityManager entityManager;

    private String defaultOrderBy;

    public GenericDaoOrm(Class<T> type) {
        super();
        this.type = type;
    }

    @Override
    public Optional<T> get(Long id) {
        return Optional.ofNullable(getEntityManager().find(type, id));
    }

    @Override
    public List<T> find(String search) {
        return new ArrayList<>();
    }

    @Override
    public List<T> getAll() {
        return getEntityManager()
                .createQuery("from " + type.getName() + " " + (getDefaultOrderBy() == null || getDefaultOrderBy().trim().length() == 0 ? "" : " order by " + getDefaultOrderBy())).getResultList();
    }

    @Override
    public T merge(T item) {
        return item;
    }

    @Override
    public void refresh(T item) {
    }

    @Override
    public void save(T item) {
        if (item != null) {
            getEntityManager().persist(item);
        }
    }

    @Override
    public void update(T item) {
        if (item != null) {
            getEntityManager().merge(item);
        }
    }

    @Override
    public boolean delete(long id) {
        return get(id).map( item -> {
            getEntityManager().remove(item);
            return true;
        }).orElse(Boolean.FALSE);
    }

    @Override
    public String getDefaultOrderBy() {
        return this.defaultOrderBy;
    }

    /**
     * @param defaultOrderBy the defaultOrderBy to set
     */
    public void setDefaultOrderBy(String defaultOrderBy) {
        this.defaultOrderBy = defaultOrderBy;
    }

    @Override
    public void initialize(Object item) {
    }

    @Override
    public void evict(T item) {
        if ( item != null ) {
            getEntityManager().detach(item);
        }
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


}
