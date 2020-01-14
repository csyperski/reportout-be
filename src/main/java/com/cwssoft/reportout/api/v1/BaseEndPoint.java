/*
 *   2016 Charles Syperski <csyperski@cwssoft.com> - CWS Software LLC
 */
package com.cwssoft.reportout.api.v1;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cwssoft.reportout.model.DomainObject;
import com.cwssoft.reportout.model.user.User;
import com.cwssoft.reportout.service.CrudService;
import com.cwssoft.reportout.service.UserService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.ws.rs.*;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author csyperski
 */
@Slf4j
public abstract class BaseEndPoint<T extends DomainObject, U extends CrudService<T, ?>> {

    public static final String AUTH_HEADER = "authorization";

    @Getter
    @Setter
    @Inject
    protected JWTVerifier jwtVerifier;

    @Getter
    @Setter
    @Inject
    protected U service;

    @Getter
    @Setter
    @Inject
    private UserService userService;

    protected abstract boolean isAdminRequired();

    protected abstract boolean isValidToAddOrUpdate(T item, boolean isUpdate, User user);
    
    protected abstract boolean isValidToLoadItem(T item, User user);
    
    protected abstract boolean isValidToDeleteItem(long id, User user);
        
    @GET
    @Path("/{param}")
    public T getById(@HeaderParam(AUTH_HEADER) String jwt,
                            @PathParam("param") long id) {
        try {
            if (isAuthorizedToAccess(jwt)) {
                
                final User user = getUser(jwt).orElse(null);
                return service
                        .getAndPrepare(id)
                        .filter( item -> isValidToLoadItem(item, user))
                        .orElseThrow(() -> getNotFoundException("Item not found!"));
            } else {
                throw getUnauthorizedException("Authorization Error!");
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException | IOException | SignatureException | JWTVerificationException | ServletException e) {
            log.warn("Unable to process request due to an authorization error: {}", e.getMessage(), e);
            throw getUnauthorizedException("Authorization Error: " + e.getMessage());
        }
    }

    @GET
    @Path("/all")
    public List<T> getAll(@HeaderParam(AUTH_HEADER) String jwt) {
        try {
            if (isAuthorizedToAccess(jwt)) {
                final User user = getUser(jwt).orElse(null);
                List<T> items = service.getAndPrepareAllItems().stream().filter( item -> isValidToLoadItem(item, user)).collect(Collectors.toList());
                if (items != null) {
                    return items;
                } else {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Items not found!" );
                }
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization Error!" );
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException | IOException | SignatureException | JWTVerificationException | ServletException e) {
            log.warn("Unable to process request due to an authorization error: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization Error: " + e.getMessage() );
        } catch (Exception e) {
            log.warn("Unable to list all items: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage() );
        }
    }

    @DELETE
    @Path("/{param}")
    public void delete(@HeaderParam(AUTH_HEADER) String jwt, @PathParam("param") long id) {
        try {
            final User user = getUser(jwt).orElse(null);
            if (isAuthorizedToAccess(jwt) && 
                isValidToDeleteItem(id, user)) {
                if (service.delete(id)) {
                    return;
                } else {
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Delete failed!" );
                }
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization Error!" );
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException | IOException | SignatureException | JWTVerificationException | ServletException e) {
            log.warn("Unable to process request due to an authorization error: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization Error!" + e.getMessage());
        } catch (Exception e) {
            log.warn("Unable to delete item: {} - {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Error: Unable to delete item: " + id + " - " + e.getMessage() );
        }
    }

    @PUT
    @Path("/")
    public T update(@HeaderParam(AUTH_HEADER) String jwt, T item) {
        if (item != null && item.getId() >= 0) {
            try { 
                if (isAuthorizedToAccess(jwt)) {
                    if (isValidToAddOrUpdate(item, true, getUser(jwt).orElse(null))) {
                        boolean status = service.update(item);
                        if (status) {
                            return service.getAndPrepare(item.getId()).orElse(null);
                        } 
                    } else {
                        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Validation Failed: Failed to update item!" );
                    }
                } else {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization Error!" );
                }
            } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException | IOException | SignatureException | JWTVerificationException | ServletException e) {
                log.warn("Unable to process request due to an authorization error: {}", e.getMessage(), e);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization Error!" + e.getMessage() );
            } catch (Exception e) {
                log.warn("Failed to update job: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Failed to update item!" );
            }
        }
        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Failed to update item!" );
    }

    @POST
    @Path("/")
    public T create(@HeaderParam(AUTH_HEADER) String jwt, T item) {
        if (item != null) {
            try {
                if (isAuthorizedToAccess(jwt)) {
                    if (isValidToAddOrUpdate(item, false, getUser(jwt).orElse(null))) {
                        if (service.save(item)) {
                            return service
                                    .getAndPrepare(item.getId())
                                    .orElseThrow( () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Failed to create item!" ) );
                        } else {
                            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Failed to create item!" );
                        }
                    } else {
                        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Validation Failed: Unable to add!" );
                    }
                } else {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization Error!" );
                }
            } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException | IOException | SignatureException | JWTVerificationException | ServletException e) {
                log.warn("Unable to process request due to an authorization error: {}", e.getMessage(), e);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization Error: " + e.getMessage());
            }
        }
        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Item incomplete or null!");
    }

    protected final boolean isAuthorizedToAccess(String header) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, IOException, SignatureException, JWTVerificationException, ServletException {
        if (isAdminRequired()) {
            return getUser(header)
                    .filter(u -> u != null
                                 && u.isEnabled()
                                 && u.isAdministrator()
                                 && isAuthorizedAdditional(u))
                    .isPresent();
        } else {
            return getUser(header).filter( u -> u.isEnabled() && isAuthorizedAdditional(u)).isPresent();
        }
    }

    // this is a hook we can use to handle additional authorization where needed.
    protected boolean isAuthorizedAdditional(final User user) {
        return true;
    }

    protected final Optional<User> getUser(String header) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, IOException, SignatureException, JWTVerificationException, ServletException {
        DecodedJWT jwt = jwtVerifier.verify(getToken(header));
        Claim userIdClaim = jwt.getClaim("userId");
        if (! userIdClaim.isNull() ) {
            try {
                return userService.getItem(userIdClaim.asLong());
            } catch (Exception e) {
                log.warn("Unable to get user from header information: {}", e.getMessage(), e);
            }
        }
        return Optional.empty();
    }

    protected final String getToken(String header) throws ServletException {
        String token = null;
        if (header == null) {
            throw new ServletException("Unauthorized: No Authorization header was found");
        }

        String[] parts = header.split(" ");
        if (parts.length != 2) {
            throw new ServletException("Unauthorized: Format is Authorization: Bearer [token]");
        }

        String scheme = parts[0];
        String credentials = parts[1];

        Pattern pattern = Pattern.compile("^Bearer$", Pattern.CASE_INSENSITIVE);
        if (pattern.matcher(scheme).matches()) {
            token = credentials;
        }
        return token;
    }

    public static ResponseStatusException getUnauthorizedException() {
        return getUnauthorizedException("Authentication failed!");
    }

    public static ResponseStatusException getUnauthorizedException(String message) {
        return getException(HttpStatus.UNAUTHORIZED, message);
    }

    public static ResponseStatusException getUnprocessableEntityException() {
        return getUnprocessableEntityException("Unable to process item!");
    }

    public static ResponseStatusException getUnprocessableEntityException(String message) {
        return getException(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    public static ResponseStatusException getNotFoundException() {
        return getNotFoundException("Unable to process item!");
    }

    public static ResponseStatusException getNotFoundException(String message) {
        return getException(HttpStatus.NOT_FOUND, message);
    }

    public static ResponseStatusException getForbiddenException() {
        return getForbiddenException("Unable to process item!");
    }

    public static ResponseStatusException getForbiddenException(String message) {
        return getException(HttpStatus.FORBIDDEN, message);
    }

    public static ResponseStatusException getInternalServerException(String message) {
        return getException(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public static ResponseStatusException getException(HttpStatus code, String message) {
        return new ResponseStatusException(code, message);
    }
}
