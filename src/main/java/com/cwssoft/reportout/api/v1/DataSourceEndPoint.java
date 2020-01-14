package com.cwssoft.reportout.api.v1;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.cwssoft.reportout.model.reports.DataSource;
import com.cwssoft.reportout.model.user.User;
import com.cwssoft.reportout.processor.JobProcessor;
import com.cwssoft.reportout.service.DataSourceService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author csyperski
 */
@Slf4j
@Component
@Path("/1/ds")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class DataSourceEndPoint extends BaseEndPoint<DataSource, DataSourceService> {
    
    @Inject
    @Getter
    @Setter
    private JobProcessor jobProcessor;

    /**
     * We override this method because we need regular users to be able to list
     * then datasource, but we probably don't want them to have access to the PW
     * field.
     *
     * @param jwt
     * @return
     */
    @GET
    @Path("/all")
    @Override
    public List<DataSource> getAll(@HeaderParam(AUTH_HEADER) String jwt) {
        try {
            User u = getUser(jwt).orElse(null);
            if (isAuthorizedToAccess(jwt)) {

                List<DataSource> items = service.getAndPrepareAllItems()
                        .stream()
                        .filter(ds -> ds != null && (!ds.isLimitToAdmin() || u.isAdministrator()))
                        .map(ds -> {
                            ds.setPassword("");
                            return ds;
                        }).collect(Collectors.toList());

                if (items != null) {
                    return items;
                } else {
                    throw getNotFoundException("Items not found!");
                }
            } else {
                throw getUnauthorizedException();
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException | IOException | SignatureException | JWTVerificationException | ServletException e) {
            log.warn("Unable to process request due to an authorization error: {}", e.getMessage(), e);
            throw getUnauthorizedException("Authorization Error: " + e.getMessage());
        } catch (Exception e) {
            log.warn("Unable to list all items: {}", e.getMessage(), e);
            throw getInternalServerException("Error: " + e.getMessage());
        }
    }

    @POST
    @Path("/test/{param}")
    public DataSource execute(@HeaderParam(AUTH_HEADER) String jwt, @PathParam("param") long id) {
        try {
            User u = getUser(jwt).orElse(null);
            if (isAuthorizedToAccess(jwt) && u.isAdministrator()) {

                Optional<DataSource> maybeDataSource = service.getAndPrepare(id);
                if (maybeDataSource.isPresent() && jobProcessor.test(maybeDataSource.get())) {
                    return maybeDataSource.get();
                } else {
                    throw getUnprocessableEntityException("Test execution failed!");
                }
            } else {
                throw getUnauthorizedException();
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException | IOException | SignatureException | JWTVerificationException | ServletException e) {
            log.warn("Unable to process request due to an authorization error: {}", e.getMessage(), e);
            throw getUnprocessableEntityException("Error: " + e.getMessage());
        } catch (Exception e) {
            log.warn("Unable to test reports source: {} - {}", id, e.getMessage(), e);
            throw getInternalServerException("Error: Unable to test reports source: " + id + " - " + e.getMessage());
        }
    }

    @Override
    protected boolean isAdminRequired() {
        return false;  // we need regular users to list the reports sources
    }

    @Override
    protected boolean isValidToAddOrUpdate(DataSource item, boolean isUpdate, User user) {
        return user.isEnabled() && user.isAdministrator();
    }

    @Override
    protected boolean isValidToLoadItem(DataSource item, User user) {
        return user != null && item != null && user.isAdministrator() && user.isEnabled();
    }

    @Override
    protected boolean isValidToDeleteItem(long id, User user) {
        return user != null && user.isEnabled() && user.isAdministrator();
    }
}