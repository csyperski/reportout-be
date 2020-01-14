package com.cwssoft.reportout.db;

import static com.cwssoft.reportout.Application.DIR_DB;
import static com.cwssoft.reportout.Application.REPORT_OUT_DATA;
import java.io.File;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.springframework.stereotype.Component;

@Component
public class Evolution1 extends BaseUpdatePackage {

    @Override
    protected String[] getQueries() {
        final String password = new StrongPasswordEncryptor().encryptPassword("password");
        final String home = System.getProperty("user.home") + File.separator + REPORT_OUT_DATA + File.separator + DIR_DB + File.separator + "reportout";
        return new String[]{
            "create table setting (id bigint generated by default as identity (start with 1), version integer, description VARCHAR(1000), setting_key varchar(255) not null, setting_value VARCHAR(1000000), primary key (id))",
            "alter table setting add constraint UK_rc0e6lvptyruet9kmv3ymjqlw unique (setting_key);",
            "create index idxSettingKey on setting (setting_key);",
            // ds
            "create table ds (id bigint generated by default as identity (start with 1), version integer, connection_string varchar(4000) not null, driver_class varchar(255) not null, name varchar(150) not null, password varchar(255), test_query varchar(255), user_name varchar(255), primary key (id))",
            "alter table ds add constraint UK_13i27dreaix0i4rxunufx349e  unique (name)",
            "create index idxName on ds (name)",
            // user
            "create table users (id bigint generated by default as identity (start with 1), version integer, administrator boolean not null, email varchar(150) not null, enabled boolean not null, first_name varchar(50) not null, last_name varchar(50) not null, password varchar(250) not null, password_change_requested boolean not null, primary key (id))",
            "alter table users add constraint UK_6dotkott2kjsp8vw4d0m25fb7  unique (email)",
            "create index idxEmail on users (email)",
            "create index idxEnabled on users (enabled)",
            // create default admin user
            "insert into users (version, administrator, email, enabled, first_name, last_name, password, password_change_requested) values ('1', true, 'admin@localhost.loc', true, 'admin', 'admin', '"+password+"', true);",
            // Jobs
            "create table jobs (id bigint generated by default as identity (start with 1), version integer, confirmation_email varchar(255), creator_id integer, description varchar(255), job_host varchar(255), include_headers boolean, job_action integer, name varchar(150) not null, password varchar(255), job_path varchar(255), port integer, public_job boolean, query varchar(100000), username varchar(255), data_source_id bigint, primary key (id))",
            "create index idxJobName on jobs (name)",
            "alter table jobs add constraint FK_nfo6j1g6sug3s0km19i2q4vd7 foreign key (data_source_id) references ds",
            // Job Schedule
            "create table job_schedule (id bigint generated by default as identity (start with 1), version integer, dow integer, hour integer, job_id bigint)",
            "create index idxHour on job_schedule (hour)",
            "create index idxDow on job_schedule (dow)",
            "alter table job_schedule add constraint FK_aq3vt1e6pi4sutbj1wagsvga4 foreign key (job_id) references jobs",
            // Process Result Table
            "create table process_results (id bigint generated by default as identity (start with 1), version integer, data varchar(1), date_completed timestamp, date_started timestamp, executed_by bigint, executed_by_scheduler boolean, job_id bigint, message varchar(4000), records bigint, successful boolean, primary key (id))",
            "create index idxPrJobId on process_results (job_id);",
            "create index idxPrExecutor on process_results (executed_by)",
            "create index idxPrSuccess on process_results (successful)",
            "create index idxPrExecutedByScheduler on process_results (executed_by_scheduler)",
            "create index idxPrDateStarted on process_results (date_started)",
            // sample data sources
            "insert into ds ( version, connection_string, driver_class, name, password, test_query, user_name) "
                  + "values (1, 'jdbc:hsqldb:file:" + home + "', 'org.hsqldb.jdbc.JDBCDriver', 'Internal Sample Data Source', '', 'select 1 from  INFORMATION_SCHEMA.SYSTEM_USERS', 'sa' );",
            "insert into ds ( version, connection_string, driver_class, name, password, test_query, user_name) "
                  + "values (1, 'jdbc:oracle:thin:@powerschool.domainname.loc:1521:PSPRODDB', 'oracle.jdbc.driver.OracleDriver', 'PowerSchool Data Source', '', 'select 1 from dual', 'psnavigator' );",
            // Create a sample job for sample data and one for PS
            "insert into jobs (version, creator_id, description, include_headers, job_action, name, public_job, query, data_source_id, port) values (1, 1, 'Sample Query from Sample Data Table.  This job can be safely removed, it is provided to aid with system acclimation.', true, 0, 'Sample Query from User Table', true, 'select * from  INFORMATION_SCHEMA.SYSTEM_USERS', 1, 21);",
            "insert into jobs (version, creator_id, description, include_headers, job_action, name, public_job, query, data_source_id, port) values (1, 1, 'This query will list all active students from PowerSchool.', true, 0, 'PowerSchool - Active Students', true, 'SELECT LASTFIRST, STUDENT_NUMBER, GENDER FROM students where ENROLL_STATUS = 0 ORDER BY LASTFIRST', 2, 21);",
        };
    }

    @Override
    public int getFromVersion() {
        return 0;
    }
}