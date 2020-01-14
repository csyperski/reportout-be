package com.cwssoft.reportout.service;

import com.cwssoft.reportout.dao.UserDao;
import com.cwssoft.reportout.model.user.User;
import java.util.Optional;

/**
 *
 * @author csyperski
 */
public interface UserService extends CrudService<User, UserDao> {
    boolean saveWithPassword(User user, String plainText);
    Optional<User> attemptLogin(String email, String password);
    boolean mergeUser(User user);
    boolean mergeUser(User user, String plainTextPassword);
}
