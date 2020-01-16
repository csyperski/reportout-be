/*
 *   2016 Charles Syperski <csyperski@cwssoft.com> - CWS Software LLC
 */
package com.cwssoft.reportout.processor;

import static com.cwssoft.reportout.util.SqlUtils.getBlackListedMatches;
import static com.cwssoft.reportout.util.StringUtils.*;

import com.cwssoft.reportout.model.reports.DataSource;
import com.cwssoft.reportout.model.reports.Job;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Inject;
import javax.mail.internet.MimeMessage;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 *
 * @author csyperski
 */
@Slf4j
@Service
public class JobProcessor {

    private final ReentrantLock lock = new ReentrantLock(true);

    @Getter
    @Setter
    @Autowired
    private List<JobDeliverer> deliverers = new ArrayList<>();

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
    @Value("${reportout.email.template.confirm.html}")
    private String emailHtml;

    @Getter
    @Setter
    @Value("${reportout.email.template.confirm.text}")
    private String emailText;

    @Getter
    @Setter
    @Value("${reportout.email.template.confirm.title}")
    private String emailTitle;

    @Getter
    @Setter
    @Value("${reportout.db.preview.querytimeout}")
    private int queryTimeout = 30;

    @Getter
    @Setter
    @Value("${reportout.db.preview.enabled}")
    private boolean previewEnabled = true;

    @Getter
    @Setter
    @Value("${reportout.sql.blacklist}")
    private String blackListedSqlWords;

    @Getter
    @Setter
    @Value("${spring.mail.from}")
    private String mailFrom;

    public JobProcessor() {
    }

    public boolean test(DataSource dataSource) throws ClassNotFoundException, SQLException {
        if (dataSource != null && dataSource.isReady() && !isNullOrBlank(dataSource.getTestQuery())) {
            try (Connection c = buildConnection(dataSource);
                    Statement statement = c.createStatement();
                    ResultSet rs = statement.executeQuery(dataSource.getTestQuery())) {
                return true;
            } catch (ClassNotFoundException | SQLException e) {
                log.warn("Unable to successfully test connection: {}", e.getMessage(), e);
                throw e;
            }
        }
        return false;
    }

    public String bdToString(BigDecimal bd) {
        
        DecimalFormat df = new DecimalFormat("#.######");
        if ( bd == null ) {
            log.warn("Big Decimal value was null, we will return an empty string!");
        }
        return bd != null ? df.format(bd) : "";
    }

    public Set<String> getBlackListedSqlWordsAsSet() {
        if ( ! isNullOrBlank(blackListedSqlWords) ) {
                return StreamSupport.stream(Splitter.on(CharMatcher.anyOf(","))
                        .trimResults()
                        .omitEmptyStrings()
                        .split(blackListedSqlWords).spliterator(), false)
                        .map( s -> s.toLowerCase(Locale.ENGLISH) )
                        .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    public PreviewResult liveResult(Job job) {
        log.info("Executing live result for {}", job);

        if ( ! previewEnabled ) {
            return new PreviewResult(true, "SQL Preview Results is disabled");
        }

        if (job == null) {
            return new PreviewResult(false, "Job is null!");
        }

        if (!job.isReady()) {
            return new PreviewResult(false, "Job is not ready!");
        }

        final List<String> blackListedMatches = getBlackListedMatches(getBlackListedSqlWordsAsSet(), job.getQuery());

        if ( ! blackListedMatches.isEmpty() ) {
            final String matchesAsString = blackListedMatches.stream().collect(Collectors.joining(", "));
            return new PreviewResult(false, "A query was found which may modify reports in the database, so the preview was cancelled! (" + matchesAsString + ")");
        }

        try {
            lock.tryLock(30, TimeUnit.SECONDS);
            log.debug("Connecting to database for job: {} - {}", job, job.getDataSource());
            try (Connection c = buildConnection(job.getDataSource());
                 Statement statement = c.createStatement();) {

                // this is unique to the preview as we want to ensure that
                // we don't allow a query to run to too long
                statement.setQueryTimeout(queryTimeout > 0 ? queryTimeout : 30);

                try (ResultSet rs = statement.executeQuery(job.getQuery());) {

                    log.info("Attempting to execute query...{}", job.getQuery());

                    log.info("Extracting meta reports...");
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columns = metaData.getColumnCount();

                    PreviewResult previewResult = new PreviewResult();
                    previewResult.setSql(job.getQuery());

                    final int columnsToGet = Math.min(columns, 50);
                    for (int i = 0; i < columnsToGet; i++) {
                        String label = metaData.getColumnLabel(i + 1);
                        previewResult.getHeaderTitles().add(label);
                    }

                    long rowCount = 0;
                    while (rs.next() && rowCount < 6) {
                        List<String> row = new ArrayList<>();
                        for (int i = 1; i <= columnsToGet; i++) {
                            int fieldType = metaData.getColumnType(i);
                            DateFormat df = DateFormat.getDateTimeInstance();
                            String fieldValue = null;
                            switch (fieldType) {
                                case Types.BIT:
                                    fieldValue = rs.getBoolean(i) ? "1" : "0";
                                    break;
                                case Types.DATE:
                                    fieldValue = rs.getDate(i) != null ? df.format(rs.getDate(i)) : "";
                                    break;
                                case Types.DECIMAL:
                                case Types.NUMERIC:
                                    fieldValue = bdToString(rs.getBigDecimal(i));
                                    break;
                                case Types.FLOAT:
                                case Types.REAL:
                                    fieldValue = String.valueOf(rs.getDouble(i));
                                    break;
                                case Types.BIGINT:
                                    fieldValue = String.valueOf(rs.getLong(i));
                                    break;
                                case Types.INTEGER:
                                    fieldValue = String.valueOf(rs.getInt(i));
                                    break;
                                case Types.SMALLINT:
                                    fieldValue = String.valueOf(rs.getShort(i));
                                    break;
                                case Types.TIME:
                                    fieldValue = rs.getTime(i) != null ? df.format(rs.getTime(i)) : "";
                                    break;
                                case Types.TIMESTAMP:
                                    fieldValue = rs.getTimestamp(i) != null ? df.format(rs.getTimestamp(i)) : "";
                                    break;
                                case Types.VARCHAR:
                                case Types.CHAR:
                                case Types.LONGVARCHAR:
                                case Types.NVARCHAR:
                                case Types.NCHAR:
                                default:
                                    fieldValue = rs.getString(i);
                                    break;
                            }
                            row.add(fieldValue);
                        }
                        previewResult.getData().add(row);
                        rowCount++;
                    }
                    rs.close();

                    previewResult.setSuccessful(true);
                    previewResult.setMessage("Query Successful");
                    return previewResult;
                } catch (Exception e) {
                    log.warn("Unable to preview job! {}", e.getMessage(), e);
                    return new PreviewResult(false, "Failed to Preview: " + e.getMessage());
                }
            } catch (Exception e) {
                log.warn("Unable to preview job! {}", e.getMessage(), e);
                return new PreviewResult(false, "Failed to Preview: " + e.getMessage());
            }
        }catch (Exception e) {
            log.warn("Unable to acquire lock to preview job! {}", e.getMessage(), e);
            return new PreviewResult(false, "Failed to acquire lock: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public ProcessResult execute(Job job) {
        return execute(job, false);
    }

    public ProcessResult execute(Job job, boolean forceDownloadTask) {
        log.info("Starting execution of job: {}", job);
        if (job == null) {
            return new ProcessResult(false, "Job is null!");
        }

        if (!job.isReady()) {
            return new ProcessResult(false, "Job is not ready!");
        }

        if (!job.isFullConfiguredForAction()) {
            return new ProcessResult(false, "Job not fully ready for selected action!");
        }

        if (deliverers != null) {
            log.info("Number of deliverers: {}", deliverers.size());
        }

        log.debug("Connecting to database for job: {} - {}", job, job.getDataSource());
        try (Connection c = buildConnection(job.getDataSource());
                Statement statement = c.createStatement();
                ResultSet rs = statement.executeQuery(job.getQuery())) {
            log.info("Attempting to execute query...{}", job.getQuery());

            log.info("Extracting meta reports...");
            ResultSetMetaData metaData = rs.getMetaData();
            int columns = metaData.getColumnCount();
            CsvBuilder csv = new CsvBuilder();

            if (job.isIncludeHeaders()) {
                for (int i = 0; i < columns; i++) {
                    log.debug("Extracting meta reports for column: {}", i + 1);
                    String label = metaData.getColumnLabel(i + 1);
                    csv.addCell(label);
                }
                csv.newLine();
            }
            long rowCount = 0;
            while (rs.next()) {
                for (int i = 1; i <= columns; i++) {
                    int fieldType = metaData.getColumnType(i);
                    DateFormat df = DateFormat.getDateTimeInstance();
                    switch (fieldType) {
                        case Types.BIT:
                            csv.addCell(rs.getBoolean(i) ? "1" : "0");
                            break;
                        case Types.DATE:
                            csv.addCell(rs.getDate(i) != null ? df.format(rs.getDate(i)) : "");
                            break;
                        case Types.DECIMAL:
                        case Types.NUMERIC:
                            csv.addCell(bdToString(rs.getBigDecimal(i)));
                            break;
                        case Types.FLOAT:
                        case Types.REAL:
                            csv.addCell(rs.getDouble(i));
                            break;
                        case Types.BIGINT:
                            csv.addCell(rs.getLong(i));
                            break;
                        case Types.INTEGER:
                            csv.addCell(rs.getInt(i));
                            break;
                        case Types.SMALLINT:
                            csv.addCell(rs.getShort(i));
                            break;
                        case Types.TIME:
                            csv.addCell(rs.getTime(i) != null ? df.format(rs.getTime(i)) : "");
                            break;
                        case Types.TIMESTAMP:
                            csv.addCell(rs.getTimestamp(i) != null ? df.format(rs.getTimestamp(i)) : "");
                            break;
                        case Types.VARCHAR:
                        case Types.CHAR:
                        case Types.LONGVARCHAR:
                        case Types.NVARCHAR:
                        case Types.NCHAR:
                        default:
                            csv.addCell(rs.getString(i));
                            break;
                    }
                }
                csv.newLine();
                rowCount++;
            }
            rs.close();
            final long finalRowCount = rowCount;

            if ( forceDownloadTask ) {
                log.info("Resetting job action to none...");
                job.setJobAction(Job.ACTION_NONE);
            }

            ProcessResult result = deliverers.stream().filter(jd
                    -> jd.canHandleJob(job)
            ).findAny().map(processor
                    -> processor.deliver(job, csv.toString(), finalRowCount)
            ).orElse(null);

            if (result == null) {
                return new ProcessResult(false, "No Result was returned from the Job Deliverers");
            }

            sendEmail(job, result);

            return result;

        } catch (Exception e) {
            log.warn("Unable to process job! {}", e.getMessage(), e);
            return new ProcessResult(false, "Failed to execute job: " + e.getMessage());
        }
    }

    public void sendEmail(Job job, ProcessResult processResult) {
        if (job != null && processResult != null && ! isNullOrBlank(job.getConfirmationEmail())) {
            getEmailsAsSet(job.getConfirmationEmail()).stream().forEachOrdered( email -> {
                try {
                    log.info("Sending confirmation email to {}...", email);

                    MimeMessage message = javaMailSender.createMimeMessage();

                    // use the true flag to indicate you need a multipart message
                    MimeMessageHelper helper = new MimeMessageHelper(message, true);
                    helper.setTo(email);

                    String html = loadTemplate(emailHtml).map(t -> getProcessedEmail(t, job, processResult)).orElse(null);
                    String text = loadTemplate(emailText).map(t -> getProcessedEmail(t, job, processResult)).orElse(null);
                    String title = loadTemplate(emailTitle).map(t -> getProcessedEmail(t, job, processResult)).orElse(null);

                    if ( mailFrom != null && isValidEmail(mailFrom)) {
                        helper.setFrom(mailFrom);
                    }

                    helper.setText(text, html);
                    helper.setSubject(title);

                    javaMailSender.send(message);
                } catch (Exception e) {
                    log.warn("Failed to send confirmation email: {}", e.getMessage(), e);
                }
            });
        }
    }

    private Optional<String> loadTemplate(String path) {
        if (path != null) {
            try {
                return Optional.of(
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

    public Connection buildConnection(DataSource dataSource) throws ClassNotFoundException, SQLException {

        if (dataSource == null) {
            throw new NullPointerException("Data Source is null!");
        }

        if (!dataSource.isReady()) {
            throw new IllegalArgumentException("Data Source is reporting that it is not ready!");
        }

        log.debug("Loading JDBC Driver: {}", dataSource.getDriverClass());
        Class.forName(dataSource.getDriverClass());
        log.debug("Loaded Driver: {}", dataSource.getDriverClass());

        log.debug("Attempting to build connection using: {}", dataSource.getConnectionString());

        DriverManager.setLoginTimeout(10);
        Connection c = DriverManager.getConnection(dataSource.getConnectionString(), dataSource.getUserName(), dataSource.getPassword());
        if (c != null) {
            c.setAutoCommit(true);
            c.setReadOnly(true);
            return c;
        }
        throw new NullPointerException("Unable to create connection!");
    }
}