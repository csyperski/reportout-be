package com.cwssoft.reportout.dao;

import com.cwssoft.reportout.model.user.User;
import java.util.Optional;

/**
 *
 * @author csyperski
 */
public interface UserDao extends BaseDao<User> {
   Optional<User> getByEmail(String email);
}
