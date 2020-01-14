package com.cwssoft.reportout;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.cwssoft.reportout.db.DatabaseManager;
import com.cwssoft.reportout.db.DbUpdateException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author csyperski
 */
@Configuration
@EnableAutoConfiguration
@EnableTransactionManagement
@ComponentScan("com.cwssoft")
@EntityScan("com.cwssoft")
@EnableScheduling
@Slf4j
public class Application {

    @Getter
    @Setter
    @Value("${reportout.secretkey}")
    private String secretKey;

    @Getter
    @Setter
    @Value(value = "${reportout.jwt.aud}")
    private String audience;

    @Getter
    @Setter
    @Value(value = "${reportout.jwt.issuer}")
    private String issuer;

    private static final String KEY_REPORT_OUT_DATA_DIRECTORY = "reportOutDataDirectory";

    private static final String TEMPLATE_SOURCE = "/application.template.properties";

    private static final String[] TEMPLATES = {
            "email.job.body.html",
            "email.job.body.txt",
            "email.job.title",
            "email.confirm.body.html",
            "email.confirm.body.txt",
            "email.confirm.title",};

    private static final String[] DRIVERS = {
            "ojdbc7.jar"
    };

    private static final String APPLICATION_PROPERTIES = "application.properties";

    public static final String REPORT_OUT_DATA = "ReportOutData";

    private static final String DIR_LIB = "lib";

    private static final String DIR_LOGS = "logs";

    public static final String DIR_DB = "db";

    private static final String DIR_DRIVERS = "drivers";

    private static final String DIR_TEMPLATES = "templates";

    public static void main(String[] args) {

        SpringApplication app = new SpringApplication(Application.class);
        Map<String, Object> map = new HashMap<>();

        String fullDataDirectory = System.getProperty("user.home") + File.separator + REPORT_OUT_DATA;

        if (initStorage(fullDataDirectory)) {

            map.put(KEY_REPORT_OUT_DATA_DIRECTORY, fullDataDirectory);

            app.setDefaultProperties(map);
            ApplicationContext ctx = app.run(args);

            try {
                /*
                 * This is ugly, but I don't know a way to inject a bean
                 * into a ServletContextListener
                 */
                DatabaseManager schemaManager = (DatabaseManager) ctx.getBean("defaultDatabaseManager", DatabaseManager.class);
                if (schemaManager != null) {
                    log.info("Size of updates: {}", schemaManager.getPackages().size());
                    schemaManager.update();
                }

            } catch (DbUpdateException ex) {
                log.error("==============================================================================");
                log.error("==============================================================================");
                log.error("An error was thrown while attempting to create/update the database!");
                log.error("Failed to update schema due to: {}", ex.getMessage(), ex);
                log.error("==============================================================================");
                log.error("==============================================================================");
            }
        } else {
            log.warn("Unable to bootstrap ReportOut :(");
        }
    }

    private static boolean initStorage(String root) {
        if (root != null) {
            try (BufferedReader templateReader =  new BufferedReader(new InputStreamReader(Application.class.getResourceAsStream(TEMPLATE_SOURCE)))) {
                log.info("Checking for : {}", root);
                File r = new File(root);
                if (!r.exists()) {
                    r.mkdirs();
                }

                if (r.exists()) {
                    log.info("{} found, checking for child directories...", root);
                    File logDir = new File(r.getAbsolutePath() + File.separator + DIR_LOGS);
                    File dbDir = new File(r.getAbsolutePath() + File.separator + DIR_DB);
                    File libDir = new File(r.getAbsolutePath() + File.separator + DIR_LIB);
                    File templateDir = new File(r.getAbsolutePath() + File.separator + DIR_TEMPLATES);

                    if (!logDir.exists()) {
                        log.info("Creating log directory...{}", logDir);
                        logDir.mkdirs();
                    }

                    if (!dbDir.exists()) {
                        log.info("Creating db directory...{}", dbDir);
                        dbDir.mkdirs();
                    }

                    if (!libDir.exists()) {
                        log.info("Creating lib directory...{}", libDir);
                        libDir.mkdirs();

                        // on installation of the lib directory, copy the default
                        // jar files into the lib directory
                        Arrays.stream(DRIVERS).forEach(tmp -> {
                            if (tmp != null) {
                                File target = new File(libDir.getAbsolutePath() + File.separator + tmp);
                                if (!target.exists()) {
                                    log.info("Missing template {}, we'll create it now.", target.getAbsolutePath());
                                    try (InputStream inputTemplate = Application.class.getResourceAsStream('/' + DIR_DRIVERS + "/" + tmp)) {
                                        Path tar = target.toPath();
                                        Files.copy(inputTemplate, tar);
                                    } catch (Exception e) {
                                        log.warn("Failed to copy: {} - {}", tmp, e.getMessage(), e);
                                    }
                                }
                            }
                        });

                    }

                    if (!templateDir.exists()) {
                        log.info("Creating template directory...{}", templateDir);
                        templateDir.mkdirs();
                    }

                    // check if a template is missing and only copy it if the file isn't present
                    // as the end-user might have made modifications.
                    Arrays.stream(TEMPLATES).forEach(tmp -> {
                        if (tmp != null) {
                            File target = new File(templateDir.getAbsolutePath() + File.separator + tmp);
                            if (!target.exists()) {
                                log.info("Missing template {}, we'll create it now.", target.getAbsolutePath());
                                try (InputStream inputTemplate = Application.class.getResourceAsStream("/" + tmp)) {
                                    Path tar = target.toPath();
                                    Files.copy(inputTemplate, tar);
                                } catch (Exception e) {
                                    log.warn("Failed to copy: {} - {}", tmp, e.getMessage(), e);
                                }
                            }
                        }
                    });

                    File userLandConfig = new File(root + File.separator + APPLICATION_PROPERTIES);
                    if (!userLandConfig.exists()) {
                        String secret = UUID.randomUUID().toString().replace("-", "");
                        String templateValue  = templateReader.lines()
                                .map( line -> line.replace("{{secret}}", secret)).collect(Collectors.joining("\n"));
                        log.info("Creating user land configuration file...{}", userLandConfig.toString());
                        Path config = new File(root + File.separator + APPLICATION_PROPERTIES).toPath();
                        Files.write(config, templateValue.getBytes());

                    }
                } else {
                    log.warn("Unable to create root directory!");
                    return false;
                }
                return true;
            } catch (Exception e) {
                log.warn("Error confinguring storage: {}", e.getMessage(), e);
            }
        }
        return false;
    }

    @Bean
    public StrongPasswordEncryptor getStrongPasswordEncryptor() {
        return new StrongPasswordEncryptor();
    }

    @Bean
    public Algorithm getAlgorithm() {
        return Algorithm.HMAC512(secretKey);
    }

    @Bean
    public JWTVerifier getJwtVerifier() {
        return JWT.require(getAlgorithm())
                .withIssuer(issuer)
                .withAudience(audience)
                .build();
    }
}