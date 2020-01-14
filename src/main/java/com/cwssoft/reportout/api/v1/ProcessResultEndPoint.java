package com.cwssoft.reportout.api.v1;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.cwssoft.reportout.model.user.User;
import com.cwssoft.reportout.processor.ProcessResult;
import com.cwssoft.reportout.service.ProcessResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author csyperski
 */
@Slf4j
@Component
@Path("/1/results")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class ProcessResultEndPoint extends BaseEndPoint<ProcessResult, ProcessResultService> {


    @GET
    @Path("/recent/{param}")
    public List<ProcessResult> getRecent(@HeaderParam(AUTH_HEADER) String jwt, @PathParam("param") int count) {
        try {
            if (isAuthorizedToAccess(jwt)) {

                if (count > 200) {
                    count = 200;
                }
                if (count <= 0) {
                    count = 10;
                }
                final User user = this.getUser(jwt).orElse(null);
                return service.getMostRecentResults(user, count);
            } else {
                throw getUnauthorizedException();
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException | IOException | SignatureException | JWTVerificationException | ServletException e) {
            log.warn("Unable to process request due to an authorization error: {}", e.getMessage(), e);
            throw getUnauthorizedException("Authorization Error: " + e.getMessage());
        } catch (Exception e) {
            log.warn("Unable to pull recent results: {}", e.getMessage(), e);
            throw getInternalServerException("Error: Unable to pull recent results - " + e.getMessage());
        }
    }

    @GET
    @Path("/lastmonth")
    public List<ProcessResult> getLast30Days(@HeaderParam(AUTH_HEADER) String jwt) {
        try {
            if (isAuthorizedToAccess(jwt)) {
                final User user = this.getUser(jwt).orElse(null);
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, -30);
                return service.getResultsBetween(user, c.getTime(), new Date());
            } else {
                throw getUnauthorizedException();
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException | IOException | SignatureException | JWTVerificationException | ServletException e) {
            log.warn("Unable to process request due to an authorization error: {}", e.getMessage(), e);
            throw getUnauthorizedException("Authorization Error: " + e.getMessage());
        } catch (Exception e) {
            log.warn("Unable to pull recent results: {}", e.getMessage(), e);
            throw getInternalServerException("Error: Unable to pull recent results - " + e.getMessage());
        }
    }

    @Override
    protected boolean isValidToAddOrUpdate(ProcessResult item, boolean isUpdate, User user) {
        return false;
    }

    @Override
    protected boolean isAdminRequired() {
        return false;
    }

    @Override
    protected boolean isValidToLoadItem(ProcessResult item, User user) {
        return true; // this is already filtered in the service
    }

    @Override
    protected boolean isValidToDeleteItem(long id, User user) {
        return false;
    }

}
