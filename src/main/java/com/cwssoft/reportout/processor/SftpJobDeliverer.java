/*
 *   2016 Charles Syperski <csyperski@cwssoft.com> - CWS Software LLC
 */
package com.cwssoft.reportout.processor;

import com.cwssoft.reportout.model.reports.Job;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *
 * @author csyperski
 */
@Slf4j
@Component
public class SftpJobDeliverer implements JobDeliverer {

    @Override
    public boolean canHandleJob(Job job) {
        return job != null && job.getJobAction() == Job.ACTION_SFTP;
    }

    @Override
    public ProcessResult deliver(Job job, String result, long rowCount) {
        if (job != null && result != null && job.isFullConfiguredForAction()) {
            try {
                byte[] data = result.getBytes("UTF-8");

                if (sftpData(job, data)) {
                    ProcessResult res = new ProcessResult(true, "Task complete via SFTP!", null);
                    res.setRecords(rowCount);
                    return res;
                } else {
                    return new ProcessResult(false, "SFTP Task failed without exception!", null);
                }

            } catch (Exception e) {
                log.warn("Error during Job Deliverer: {}", e.getMessage(), e);
                return new ProcessResult(false, "Unable to process job: " + e.getMessage());
            }
        } else {
            return new ProcessResult(false, "Either job or result was null!");
        }
    }

    private boolean sftpData(Job job, byte[] data) throws IOException, JSchException, SftpException {

        if (job != null && data != null) {

            String host = job.getJobHost();
            String user = job.getUsername();
            String pass = job.getPassword();
            int port = job.getPort() != 22 && job.getPort() > 0 ? job.getPort() : 22;
            String uploadPath = job.getJobPath();

            if (uploadPath != null && uploadPath.endsWith("/")) {
                uploadPath = uploadPath + "reportoutfile";
            }

            Session session = null;
            ChannelSftp c  = null;
            try (InputStream is = new ByteArrayInputStream(data)) {

                JSch.setConfig("StrictHostKeyChecking", "no");
                
                JSch jsch = new JSch();
                
                session = jsch.getSession(user, host, port);
                session.setPassword(pass);
                session.connect(60000);

                Channel channel = session.openChannel("sftp");
                channel.connect();
                c = (ChannelSftp) channel;

                int mode = ChannelSftp.OVERWRITE;
                c.put(is, uploadPath, mode);
                return true;
            } catch (IOException | JSchException | SftpException ex) {
                log.warn("Error during SFTP process: {}", ex.getMessage(), ex);
                throw ex;
            } finally {
                if ( c != null ) {
                    try {
                        c.disconnect();
                    } catch( Exception e) {
                    }
                }
                if ( session != null ) {
                    try {
                        session.disconnect();
                    } catch( Exception e) {
                    }
                }
            }
        }
        return false;
    }

}
