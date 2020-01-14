/*
 *  2014 Charles Syperski <csyperski@cwssoft.com>
 *  
 */
package com.cwssoft.reportout.service;

import com.cwssoft.reportout.dao.BaseDao;
import com.cwssoft.reportout.model.DomainObject;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 *
 * @author csyperski
 */
public interface CrudService<T extends DomainObject, U extends BaseDao> extends Serializable {

    public void setDao(U dao);

    public U getDao();

    @Deprecated
    public List<T> getAllFilter(Predicate<T> predicate);

    public List<T> getAllItems();
    
    public List<T> getAllItems(Predicate<T> predicate);
    
    public List<T> getAndPrepareAllItems();
    
    public List<T> getAndPrepareAllItems(Predicate<T> predicate);

    public boolean save(T item);

    public boolean update(T item);

    public void refresh(T item);

    public void evict(T item);

    public T merge(T item);

    public boolean delete(long id);

    public List<T> find(String query);

    public Optional<T> getItem(Long id);

    public Optional<T> getAndPrepare(Long id);

    public T prepare(T item);

}
