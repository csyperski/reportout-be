package com.cwssoft.reportout.api.v1;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.cwssoft.reportout.model.user.User;
import com.cwssoft.reportout.model.version.SystemVersion;
import com.cwssoft.reportout.service.UserService;
import com.cwssoft.reportout.service.VersionService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.password.PasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author csyperski
 */
@Slf4j
@Component
@Path("/1/auth")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class AuthEndPoint {

    @Inject
    @Getter
    @Setter
    private UserService userService;

    @Inject
    @Getter
    @Setter
    private VersionService versionService;

    @Getter
    @Setter
    @Value("${reportout.backend.version}")
    private String version;

    @Getter
    @Setter
    @Value(value = "${reportout.jwt.issuer:reportout}")
    private String issuer;

    @Getter
    @Setter
    @Value(value = "${reportout.jwt.aud:https://ro.cwssoft.com}")
    private String audience;

    @Setter
    @Getter
    @Inject
    private Algorithm algorithm;

    @Inject
    @Setter
    @Getter
    private PasswordEncryptor testInject;

    private volatile SystemVersion systemVersion;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @GET
    @Path("/ping")
    public Map<String, String> ping() {
        Map<String, String> result = new HashMap<>();
        result.put("ping", "1");
        return result;
    }

    @GET
    @Path("/latest")
    public SystemVersion getLatestVersion() {
        log.debug("Getting read lock");
        lock.readLock().lock();
        log.debug("Got read lock");
        try {
            if (systemVersion == null || systemVersion.isExpired()) {
                SystemVersion latestVersion = versionService.getLatestVersion(version).orElse(null);
                log.info("System version: {}", latestVersion);
                if (latestVersion != null) {
                    // we need a write lock
                    log.debug("Releasing read lock.");
                    lock.readLock().unlock();
                    log.debug("Requesting write lock.");
                    lock.writeLock().lock();
                    log.debug("Got write lock.");
                    try {
                        // recheck state
                        if (systemVersion == null || systemVersion.isExpired()) {
                            log.debug("Updating cached version to {}", systemVersion);
                            systemVersion = latestVersion;
                        }
                        // downgrade to read lock before unlocking write
                        log.debug("Downgrading to read lock.");
                        lock.readLock().lock();
                    } finally {
                        log.debug("releasing write lock.");
                        lock.writeLock().unlock();
                    }
                }

                if (systemVersion != null) {
                    return systemVersion;
                } else {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to determine version! (1)");
                }
            } else {
                log.info("Returning cached version of systemVersion: {}", systemVersion);
                return systemVersion;
            }
        } finally {
            log.debug("releasing read lock.");
            lock.readLock().unlock();
        }
    }

    @GET
    @Path("/version")
    public Map<String, String> getBackEndVersion() {
        Map<String, String> result = new HashMap<>();
        result.put("version", version);
        return result;
    }

    @POST
    @Path("/")
    public Map<String, String> authenticate(User user) {
        if (user != null && userService != null && algorithm != null) {
            return userService.attemptLogin(user.getEmail(), user.getPassword())
                    .filter(u -> u != null && u.isEnabled())
                    .map(u -> {
                        JWTCreator.Builder builder = JWT.create()
                                .withIssuer(issuer)
                                .withClaim("userEmail", u.getEmail())
                                .withClaim("userId", u.getId())
                                .withClaim("userFirstName", u.getFirstName())
                                .withClaim("userLastName", u.getLastName())
                                .withClaim("userIsAdmin", u.isAdministrator());

                        if (getAudience() != null) {
                            builder.withAudience(getAudience());
                        }

                        // set expiration information
                        Calendar c = Calendar.getInstance();
                        c.add(Calendar.HOUR_OF_DAY, 12);

                        builder.withExpiresAt(c.getTime());
                        builder.withIssuedAt(new Date());

                        final String jwt = builder.sign(algorithm);

                        return Collections.singletonMap("token", jwt);
                    })
                    .orElseThrow(() -> getUnauthorizedException());
        } else {
            throw getUnauthorizedException();
        }
    }

    public static ResponseStatusException getUnauthorizedException() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication failed!");
    }
}
