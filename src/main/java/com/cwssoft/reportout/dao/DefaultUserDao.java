package com.cwssoft.reportout.dao;

import com.cwssoft.reportout.model.user.User;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 *
 * @author csyperski
 */
@Repository
public class DefaultUserDao extends GenericDaoOrm<User> implements UserDao {
    public DefaultUserDao() {
        super(User.class);
    }

    @Override
    public Optional<User> getByEmail(String email) {
        return getEntityManager()
                .createQuery("from " + type.getName() + " where email = :email and enabled = :true ")
                .setParameter("email", email)
                .setParameter("true", true)
                .getResultList()
                .stream()
                .findFirst();
    }
}
