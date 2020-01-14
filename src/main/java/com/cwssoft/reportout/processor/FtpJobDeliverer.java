/*
 *   2016 Charles Syperski <csyperski@cwssoft.com> - CWS Software LLC
 */
package com.cwssoft.reportout.processor;

import com.cwssoft.reportout.model.reports.Job;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author csyperski
 */
@Slf4j
@Component
public class FtpJobDeliverer implements JobDeliverer {

    private final int BUFFER_SIZE = 8 * 1024;

    @Getter
    @Setter
    @Value("${reportout.ftp.template}")
    private String ftpUrl = "ftp://%s:%s@%s%s/%s;type=i";

    @Override
    public boolean canHandleJob(Job job) {
        return job != null && job.getJobAction() == Job.ACTION_FTP;
    }

    @Override
    public ProcessResult deliver(Job job, String result, long rowCount) {
        if (job != null && result != null && job.isFullConfiguredForAction()) {
            try {
                byte[] data = result.getBytes("UTF-8");

                if ( ftpData(job, data) ) {
                    ProcessResult res = new ProcessResult(true, "Task complete via FTP!", null);
                    res.setRecords(rowCount);
                    return res;
                } else {
                    return new ProcessResult(false, "FTP Task failed without exception!", null);
                }

            } catch (Exception e) {
                log.warn("Error during Job Deliverer: {}", e.getMessage(), e);
                return new ProcessResult(false, "Unable to process job: " + e.getMessage());
            }
        } else {
            return new ProcessResult(false, "Either job or result was null!");
        }
    }

    private boolean ftpData(Job job, byte[] data) throws IOException {
        if (job != null && data != null) {
            String host = job.getJobHost();
            String user = job.getUsername();
            String pass = job.getPassword();
            String port = job.getPort() != 21 && job.getPort() > 0 ? ":" + job.getPort() : "";
            String uploadPath = job.getJobPath();

            if (uploadPath != null && uploadPath.endsWith("/")) {
                uploadPath = uploadPath + "reportoutfile";
            }

            try {
                String localFtpUrl = String.format(ftpUrl, URLEncoder.encode(user, "UTF-8"), URLEncoder.encode(pass, "UTF-8"), host, port, uploadPath);
                String localFtpUrlLog = String.format(ftpUrl, URLEncoder.encode(user, "UTF-8"), "**********", host, port, uploadPath);
                log.info("Uploading using URL: {}", localFtpUrlLog);
                URL url = new URL(localFtpUrl);
                URLConnection conn = url.openConnection();

                try (OutputStream outputStream = conn.getOutputStream();
                     InputStream inputStream = new ByteArrayInputStream(data);) {

                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead = -1;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
                log.info("File upload!");
                return true;
            } catch (IOException ex) {
                log.warn("Error during FTP process: {}", ex.getMessage(), ex);
                throw ex;
            }
        }
        return false;
    }
}
