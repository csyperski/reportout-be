package com.cwssoft.reportout.api.v1;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.cwssoft.reportout.model.user.User;
import com.cwssoft.reportout.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author csyperski
 */
@Slf4j
@Component
@Path("/1/user")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class UserEndPoint extends BaseEndPoint<User, UserService> {

    @PUT
    @Path("/")
    @Override
    public User update(@HeaderParam(AUTH_HEADER) String jwt, User item) {
        if (item != null && item.getId() >= 0) {
            try {
                if (isAuthorizedToAccess(jwt)) {
                    if (isValidToAddOrUpdate(item, true, getUser(jwt).orElse(null))) {
                        String userPassword = item.getPassword();
                        boolean status = service.mergeUser(item, userPassword != null && userPassword.trim().length() > 0 ? userPassword : null);
                        if (status) {
                            return service.getAndPrepare(item.getId()).orElse(null);
                        }
                    } else {
                        throw getForbiddenException("Validation Failed: Unable to update!");
                    }
                } else {
                    throw getUnauthorizedException();
                }
            } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException | IOException | SignatureException | JWTVerificationException | ServletException e) {
                log.warn("Unable to process request due to an authorization error: {}", e.getMessage(), e);
                throw getUnauthorizedException("Authorization Error: " + e.getMessage());
            } catch (Exception e) {
                log.warn("Failed to update job: {}", e.getLocalizedMessage());
                throw getUnprocessableEntityException("Failed to update item!");
            }
        }
        throw getUnprocessableEntityException("Failed to update item!");
    }

    @GET
    @Path("/{param}")
    public User getById(@HeaderParam(AUTH_HEADER) String jwt,
                     @PathParam("param") long id) {
        final User user = super.getById(jwt, id);
        if ( user != null ) {
            user.setPassword(null);
        }
        return user;
    }

    @GET
    @Path("/self")
    public User getSelf(@HeaderParam(AUTH_HEADER) String jwt) {
        try {
            return getUser(jwt).filter(u -> u.isEnabled()).map( u -> {
                u.setPassword(null);
                return u;
            }).orElseThrow(() -> getNotFoundException());
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException | IOException | SignatureException | JWTVerificationException | ServletException e) {
            log.warn("Unable to process request due to an authorization error: {}", e.getMessage(), e);
            throw getUnauthorizedException("Authorization Error: " + e.getMessage());
        }
    }

    @GET
    @Path("/all")
    public List<User> getAll(@HeaderParam(AUTH_HEADER) String jwt) {
        return super.getAll(jwt).stream().peek( u -> u.setPassword(null) ).collect(Collectors.toList());
    }

    @POST
    @Path("/")
    public User create(@HeaderParam(AUTH_HEADER) String jwt, User item) {
        if (item != null) {
            try {
                if (isAuthorizedToAccess(jwt)) {
                    if (isValidToAddOrUpdate(item, false, getUser(jwt).orElse(null))) {
                        if (service.saveWithPassword(item, item.getPassword())) {
                            return service
                                    .getAndPrepare(item.getId())
                                    .orElseThrow(() -> getNotFoundException("Failed to create item!"));
                        } else {
                            throw getNotFoundException("Failed to create item!");
                        }
                    } else {
                        throw getUnprocessableEntityException("Validation Failed: Unable to add!");
                    }
                } else {
                    throw getUnauthorizedException();
                }
            } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException | IOException | SignatureException | JWTVerificationException | ServletException e) {
                log.warn("Unable to process request due to an authorization error: {}", e.getMessage(), e);
                throw getUnauthorizedException("Authorization Error: " + e.getMessage());
            }
        }
        throw getUnprocessableEntityException("Item incomplete or null!");
    }

    @Override
    protected boolean isAdminRequired() {
        return true;
    }

    @Override
    protected boolean isValidToAddOrUpdate(User item, boolean isUpdate, User user) {
        return (user.isEnabled() && user.isAdministrator());
    }

    @Override
    protected boolean isValidToLoadItem(User item, User user) {
        return (user.isEnabled() && user.isAdministrator());
    }

    @Override
    protected boolean isValidToDeleteItem(long id, User user) {
        return (user.isEnabled() && user.isAdministrator());
    }

}
