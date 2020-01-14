/*
 *  Copyright (C)  2014 Charles Syperski <csyperski@cwssoft.com>
 *  
 */
package com.cwssoft.reportout.dao;

import com.cwssoft.reportout.model.DomainObject;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author csyperski
 */
public interface BaseDao<T extends DomainObject> extends Serializable {

    public Optional<T> get(Long id);

    public List<T> find(String search);

    public List<T> getAll();

    public void save(T item);

    public void update(T item);

    public T merge(T item);

    public void refresh(T item);

    public void evict(T item);

    public void initialize(Object item);        // used to force initialize a proxy

    public boolean delete(long id);

    public String getDefaultOrderBy();
}
