package com.cwssoft.reportout.api.v1;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.cwssoft.reportout.model.reports.Job;
import com.cwssoft.reportout.model.reports.JobSchedule;
import com.cwssoft.reportout.model.user.User;
import com.cwssoft.reportout.processor.JobProcessor;
import com.cwssoft.reportout.processor.PreviewResult;
import com.cwssoft.reportout.processor.ProcessResult;
import com.cwssoft.reportout.service.DataSourceService;
import com.cwssoft.reportout.service.JobService;
import com.cwssoft.reportout.service.ProcessResultService;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.cwssoft.reportout.util.StringUtils.isNullOrBlank;


/**
 * @author csyperski
 */
@Slf4j
@Component
@Path("/1/job")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class JobEndPoint extends BaseEndPoint<Job, JobService> {

    @Inject
    @Getter
    @Setter
    private JobProcessor jobProcessor;

    @Inject
    @Getter
    @Setter
    private ProcessResultService processResultService;

    @Inject
    @Getter
    @Setter
    private DataSourceService dataSourceService;

    @POST
    @Path("/execute/{param}")
    public ProcessResult execute(@HeaderParam(AUTH_HEADER) String jwt, @PathParam("param") long id) {
        return execute(jwt, id, null);
    }

    @POST
    @Path("/execute/{param}/{asdownload}")
    public ProcessResult execute(@HeaderParam(AUTH_HEADER) String jwt, @PathParam("param") long id, @PathParam("asdownload") String asDownload) {
        try {
            if (isAuthorizedToAccess(jwt)) {
                final boolean forceDownload = asDownload != null && asDownload.equalsIgnoreCase("download");
                final User user = this.getUser(jwt).orElse(null);
                Optional<Job> maybeJob = service.getAndPrepare(id)
                        .filter(j -> isValidToLoadItem(j, user));
                final Date startDate = new Date();
                Optional<ProcessResult> maybeResult = maybeJob.map(j -> jobProcessor.execute(j, forceDownload));
                if (maybeResult.isPresent()) {
                    maybeResult.ifPresent(pr -> {
                        ProcessResult copy = new ProcessResult(pr);
                        copy.setData(null);
                        copy.setDateStarted(startDate);
                        copy.setDateCompleted(new Date());
                        copy.setExecutedBy(user.getId());
                        copy.setExecutedByScheduler(false);
                        copy.setJobId(id);
                        processResultService.save(copy);
                    });
                    return maybeResult.orElse(null);
                } else {
                    throw getUnprocessableEntityException("Unable to execute job!");
                }
            } else {
                throw getUnauthorizedException();
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException | IOException | SignatureException | JWTVerificationException | ServletException e) {
            log.warn("Unable to process request due to an authorization error: {}", e.getMessage(), e);
            throw getUnauthorizedException("Authorization Error: " + e.getMessage());
        } catch (Exception e) {
            log.warn("Unable to execute job: {} - {}", id, e.getMessage(), e);
            throw getInternalServerException("Error: Unable to execute job: " + id + " - " + e.getMessage());
        }
    }


    @PUT
    @Path("/")
    public Job update(@HeaderParam(AUTH_HEADER) String jwt, Job item) {
        if (item != null) {
            // reset the original owner
            service.getItem(item.getId()).ifPresent(j -> item.setCreatorId(j.getCreatorId()));
        }
        return super.update(jwt, item);
    }

    @POST
    @Path("/preview")
    public PreviewResult preview(@HeaderParam(AUTH_HEADER) String jwt, Job job) {
        try {
            if (isAuthorizedToAccess(jwt)) {

                // we need to re-populate the datasource because the one
                // that will be passed with the request will not have the correct username/password
                log.info("Reloading reports source...");
                dataSourceService.getItem(job.getDataSource() != null ? job.getDataSource().getId() : -1)
                        .ifPresent(ds -> job.setDataSource(ds));
                log.info("Data source reloaded.");


                final User user = this.getUser(jwt).orElse(null);
                log.info("Processing live preview for {}", user);

                if (isValidToLoadItem(job, user)) {
                    return jobProcessor.liveResult(job);
                } else {
                    throw getUnauthorizedException("Authorization Error (1)!");
                }
            } else {
                throw getUnauthorizedException();
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException | IOException | SignatureException | JWTVerificationException | ServletException e) {
            log.warn("Unable to process request due to an authorization error: {}", e.getMessage(), e);
            throw getUnauthorizedException("Authorization Error: " + e.getMessage());
        } catch (Exception e) {
            log.warn("Unable to preview reports: {} - {}", job, e.getMessage(), e);
            throw getInternalServerException("Error: Unable to preview job: " + job.toString() + " - " + e.getMessage());
        }
    }

    @Override
    protected boolean isValidToAddOrUpdate(Job item, boolean isUpdate, User user) {
        if (item != null && !isNullOrBlank(item.getName()) && user != null) {
            return isValidToLoadItem(item, user);
        }
        return false;
    }

    @PUT
    @Path("/jobschedules")
    public Job updateJobSchedule(@HeaderParam(AUTH_HEADER) String jwt, Job job) {
        if (job != null && job.getId() >= 0) {
            try {
                if (isAuthorizedToAccess(jwt)) {
                    final List<JobSchedule> schedules = job.getJobSchedules();
                    boolean status = service.updateSchedule(job, schedules);
                    if (status) {
                        return service.getAndPrepare(job.getId()).orElse(null);
                    }
                } else {
                    throw getUnauthorizedException();
                }
            } catch (Exception e) {
                log.warn("Failed to update job: {}", e.getLocalizedMessage());
                throw getUnprocessableEntityException("Failed to update job!");
            }
        }
        throw getUnprocessableEntityException("Failed to update job!");
    }

    @Override
    protected boolean isAdminRequired() {
        return false;
    }

    @Override
    protected boolean isValidToLoadItem(Job item, User user) {
        return item != null &&
                user != null &&
                (item.isPublicJob() || user.isAdministrator() || item.getCreatorId() == user.getId()) &&
                (user.isAdministrator() || item.getDataSource() == null || !item.getDataSource().isLimitToAdmin());
    }

    @Override
    protected boolean isValidToDeleteItem(long id, User user) {
        return service
                .getAndPrepare(id)
                .map(item -> item != null &&
                        user != null &&
                        (item.isPublicJob() || user.isAdministrator() || item.getCreatorId() == user.getId()))
                .orElse(Boolean.FALSE);
    }
}
