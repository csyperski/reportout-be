/*
 *   2016 Charles Syperski <csyperski@cwssoft.com> - CWS Software LLC
 */
package com.cwssoft.reportout.processor;

import com.cwssoft.reportout.model.reports.Job;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.activation.DataSource;
import javax.inject.Inject;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.cwssoft.reportout.util.StringUtils.*;

/**
 * @author csyperski
 */
@Component
@Slf4j
public class EmailJobDeliverer implements JobDeliverer {

    @Getter
    @Setter
    @Value("${reportout.job.email.maxsize}")
    private int maxSize = 10485760;  // 10mb

    @Inject
    @Getter
    @Setter
    private JavaMailSender javaMailSender;

    @Inject
    @Getter
    @Setter
    private List<StringReplacer> replacers;

    @Getter
    @Setter
    @Value("${reportout.email.template.job.html}")
    private String emailHtml;

    @Getter
    @Setter
    @Value("${reportout.email.template.job.text}")
    private String emailText;

    @Getter
    @Setter
    @Value("${reportout.email.template.job.title}")
    private String emailTitle;

    @Getter
    @Setter
    @Value("${spring.mail.from}")
    private String mailFrom;

    @Override
    public boolean canHandleJob(Job job) {
        return job != null && job.getJobAction() == Job.ACTION_EMAIL;
    }

    @Override
    public ProcessResult deliver(Job job, String result, long rowCount) {
        if (job != null && result != null) {
            if (!job.isFullConfiguredForAction()) {
                return new ProcessResult(false, "Invalid email address: " + job.getUsername());
            }

            try {
                final byte[] data = result.getBytes("UTF-8");
                if (data != null) {
                    if (data.length > maxSize) {
                        return new ProcessResult(false, "File too large to safely email: file size: " + data.length + " max size: " + maxSize);
                    }

                    ProcessResult res = new ProcessResult(true, "Task completed via Email!", null);
                    res.setRecords(rowCount);

                    List<Boolean> results = getEmailsAsSet(job.getUsername()).stream().map(email -> {
                        try {
                            MimeMessage message = javaMailSender.createMimeMessage();

                            // use the true flag to indicate you need a multipart message
                            MimeMessageHelper helper = new MimeMessageHelper(message, true);
                            helper.setTo(email);

                            String html = loadTemplate(emailHtml).map(t -> getProcessedEmail(t, job, res)).orElse(null);
                            String text = loadTemplate(emailText).map(t -> getProcessedEmail(t, job, res)).orElse(null);
                            String title = loadTemplate(emailTitle).map(t -> getProcessedEmail(t, job, res)).orElse(null);

                            helper.setText(text, html);
                            helper.setSubject(title);
                            if ( mailFrom != null && isValidEmail(mailFrom)) {
                                helper.setFrom(mailFrom);
                            }

                            DataSource source = new ByteArrayDataSource(data, "text/csv");
                            helper.addAttachment(toSafeFileName(job.getName()).trim() + ".csv", source);

                            javaMailSender.send(message);
                            return true;
                        } catch (Exception e) {
                            log.warn("Error during Job Deliverer: {}", e.getMessage(), e);
                            return false;
                        }
                    }).collect(Collectors.toList());

                    long suc = results.stream().filter( b -> b).count();
                    long failures = results.stream().filter( b -> !b).count();

                    res.setMessage(res.getMessage() + " (" + suc + " Successful / " + failures + " Failures)");

                    return res;
                }
            } catch (UnsupportedEncodingException e) {
                log.warn("Unable to encode results due to: {}", e.getMessage(), e);
            }
        }
        return new ProcessResult(false, "Failed to process job!");
    }

    private Optional<String> loadTemplate(String path) {
        if (path != null) {
            try {
                return Optional.ofNullable(
                        Files.readAllLines(
                                new File(path).toPath())
                                .stream()
                                .collect(Collectors.joining("\n")
                                )
                );
            } catch (IOException ex) {
                log.warn("Unable to load template: {} - due to: {}", path, ex.getMessage(), ex);
            }
        }
        return Optional.empty();
    }

    private String getProcessedEmail(String template, Job job, ProcessResult processResult) {
        if (template != null && job != null && processResult != null) {
            Map<String, Object> elements = new HashMap<>();
            elements.put("job", job);
            elements.put("processresult", processResult);
            String ret = template;
            if ( replacers != null ) {
                for (StringReplacer r : replacers) {
                    if (r != null) {
                        if (elements.containsKey(r.requires())) {
                            ret = r.process(ret, elements.get(r.requires()), false);
                        }
                    }
                }
            }
            return ret;
        }
        return null;
    }
}
