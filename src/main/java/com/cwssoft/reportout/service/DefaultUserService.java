package com.cwssoft.reportout.service;

import com.cwssoft.reportout.dao.UserDao;
import com.cwssoft.reportout.model.user.User;
import java.util.Locale;
import java.util.Optional;
import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.cwssoft.reportout.util.StringUtils.isNullOrBlank;
import static com.cwssoft.reportout.util.StringUtils.isValidEmail;

/**
 *
 * @author csyperski
 */
@Service
@Slf4j
public class DefaultUserService extends BaseCrudService<User, UserDao> implements UserService {

    @Setter
    @Getter
    @Inject
    private StrongPasswordEncryptor passwordEncryptor;

    @Override
    @Inject
    public void setDao(UserDao dao) {
        super.setDao(dao);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> attemptLogin(String email, String password) {
        if (email != null && password != null) {
            return dao.getByEmail(email.trim().toLowerCase())
                    .filter(u -> u.isEnabled() && passwordEncryptor.checkPassword(password, u.getPassword()));
        }
        return Optional.empty();
    }

    private String cleanUpEmailAddress(String email) {
        if ( email != null ) {
            return email.trim().toLowerCase(Locale.US);
        }
        return email;
    }
    
    @Override
    @Transactional(readOnly = false)
    public boolean saveWithPassword(User user, String plainText) {

        if (user == null) {
            throw new IllegalArgumentException("Invalid user!");
        }
        
        if ( ! isValidEmail(user.getEmail()) ) {
            throw new IllegalArgumentException("Invalid email address!");
        }

        // let's clean up the email address...
        user.setEmail(cleanUpEmailAddress(user.getEmail()));
        
        // let's check if an account exists with this email address...
        Optional<User> maybeUser = this.dao.getByEmail(user.getEmail());
        
        if ( maybeUser.isPresent() ) {
            throw new IllegalArgumentException("User already exists! (ID: " + maybeUser.get().getId() + ")");
        }
        
        if ( isNullOrBlank(user.getFirstName()) || isNullOrBlank(user.getLastName()) ) {
            throw new IllegalArgumentException("Invalid first or last name!");
        }
        
        if (plainText == null || plainText.trim().length() < 6) {
            throw new IllegalArgumentException("Invalid password!");
        }

        String pw = passwordEncryptor.encryptPassword(plainText.trim());
        user.setPassword(pw);
        dao.save(user);
        return true;
    }

    @Override
    @Transactional(readOnly = false)
    public boolean mergeUser(User user) {
        return mergeUser(user, null);
    }

    @Override
    @Transactional(readOnly = false)
    public boolean mergeUser(User user, String plainTextPassword) {
        if (user != null && user.getId() >= 0) {
            return getItem(user.getId()).map(u -> {
                boolean passwordRequested = plainTextPassword != null;
                if (passwordRequested && plainTextPassword.trim().length() < 6) {
                    throw new IllegalArgumentException("Invalid password!");
                }
                u.setAdministrator(user.isAdministrator());
                u.setEmail(user.getEmail());
                u.setEnabled(user.isEnabled());
                u.setFirstName(user.getFirstName());
                u.setLastName(user.getLastName());
                u.setPasswordChangeRequested(user.isPasswordChangeRequested());
                if (passwordRequested && plainTextPassword != null) {
                    String pw = passwordEncryptor.encryptPassword(plainTextPassword.trim());
                    u.setPassword(pw);
                }
                return true;
            }).orElse(Boolean.FALSE);
        }
        return false;
    }
}
