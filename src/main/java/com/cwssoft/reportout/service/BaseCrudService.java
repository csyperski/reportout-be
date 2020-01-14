/*
 *  2012 Charles Syperski <csyperski@cwssoft.com>
 *  
 */
package com.cwssoft.reportout.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.cwssoft.reportout.dao.BaseDao;
import com.cwssoft.reportout.model.DomainObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author csyperski
 */
public abstract class BaseCrudService<T extends DomainObject, U extends BaseDao> implements CrudService<T, U> {

    private static final Logger logger = LoggerFactory.getLogger(BaseCrudService.class);

    protected U dao;

    @Override
    public void setDao(U dao) {
        this.dao = dao;
    }

    @Override
    public U getDao() {
        return dao;
    }

    @Override
    @Transactional(readOnly = false)
    public void evict(T item) {
        getDao().evict(item);
    }

    @Transactional(readOnly = true)
    @Override
    public List<T> getAllItems() {
        return getDao().getAll();
    }
    
    @Transactional(readOnly = true)
    @Override
    public List<T> getAndPrepareAllItems() {
        List<T> allItems = getAllItems();
        allItems.stream().forEach( item -> prepare(item));
        return allItems;
    }
    
    @Transactional(readOnly = true)
    @Override
    public List<T> getAndPrepareAllItems(Predicate<T> predicate) {
        return getAllItems()
                .stream()
                .filter(predicate)
                .map(item -> prepare(item))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<T> getAllFilter(Predicate<T> predicate) {
        return getAllItems(predicate);
    }
    
    @Transactional(readOnly = true)
    @Override
    public List<T> getAllItems(Predicate<T> predicate) {
        return getAllItems()
                .stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = false)
    @Override
    public T merge(T item) {
        return (T) getDao().merge(item);
    }

    @Transactional(readOnly = false)
    @Override
    public void refresh(T item) {
        getDao().refresh(item);
    }

    @Transactional(readOnly = false)
    @Override
    public boolean save(T item) {
        getDao().save(item);
        return true;
    }

    /**
     * This function has side effects, T is returned only as a convenience for
     * chaining of calls. This method could also return void
     *
     * @param item
     * @return The same item passed in
     */
    @Transactional(readOnly = false)
    @Override
    public T prepare(T item) {
        return item;
    }

    @Transactional(readOnly = false)
    @Override
    public boolean update(T item) {
        getDao().update(item);
        return true;
    }

    @Transactional(readOnly = false)
    @Override
    public boolean delete(long id) {
        return getDao().delete(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<T> find(String query) {
        return getDao().find(query);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<T> getItem(Long id) {
        return getDao().get(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<T> getAndPrepare(Long id) {
        return getItem(id).map(item -> prepare(item));
    }

}
