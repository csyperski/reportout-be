package com.cwssoft.reportout.model.reports;

import com.cwssoft.reportout.model.VersionedObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import static com.cwssoft.reportout.util.StringUtils.isNullOrBlank;

/**
 *
 * @author csyperski
 */
@Entity
@Table(name = "jobs", indexes = {
    @Index(name="idxJobName", columnList="name")
})
public class Job extends VersionedObject {
    
    public static final int ACTION_NONE = 0;
    public static final int ACTION_EMAIL = 1;
    public static final int ACTION_FTP = 2;
    public static final int ACTION_SFTP = 3;

    @Getter
    @Setter
    @Column(length=150, nullable=false, unique=false)
    private String name;

    @Getter
    @Setter
    @Column(length=255, nullable=true)
    private String description;

    @Getter
    @Setter
    @Column(length=100000, nullable=true, unique=false)
    private String query;

    @Getter
    @Setter
    @Column
    private boolean publicJob;

    @Getter
    @Setter
    @Column
    private int jobAction = ACTION_NONE;

    @Getter
    @Setter
    @Column
    private int creatorId = -1;

    @Getter
    @Setter
    @Column(length=255, nullable=true, unique=false)
    private String username; // or email in event of email job

    @Getter
    @Setter
    @Column(length=255, nullable=true, unique=false)
    private String password;

    @Getter
    @Setter
    @Column(length=255, nullable=true, unique=false)
    private String jobHost; // ftp/sftp

    @Getter
    @Setter
    @Column(length=255, nullable=true, unique=false)
    private String jobPath;

    @Getter
    @Setter
    @Column
    private int port;

    @Getter
    @Setter
    @Column(length=255, nullable=true, unique=false)
    private String confirmationEmail;

    @Getter
    @Setter
    @Column
    private boolean includeHeaders = false;

    @Getter
    @Setter
    @OneToMany(mappedBy = "job", 
               cascade = {CascadeType.REMOVE}, 
               fetch = FetchType.EAGER, 
               orphanRemoval = true)
    private List<JobSchedule> jobSchedules = new ArrayList<>();

    @Getter
    @Setter
    @ManyToOne(cascade = {})
    private DataSource dataSource;

    @JsonIgnore
    public boolean isReady() {
        return dataSource != null && 
               dataSource.isReady() &&
               !isNullOrBlank(name) && 
               !isNullOrBlank(query);
               
    }
    
    public boolean isFullConfiguredForAction() {
        switch(jobAction) {
            case ACTION_SFTP:
            case ACTION_FTP:
                return ! isNullOrBlank(jobHost) && 
                       ! isNullOrBlank(jobPath); 
            case ACTION_EMAIL:
                return ! isNullOrBlank(username); 
            default:
                return true;
        }
    }
    
    public boolean isScheduledAt(int hour, int dow) {
        if ( isReady() && jobAction != ACTION_NONE && isFullConfiguredForAction() ) {
            return jobSchedules.stream().anyMatch( js -> js.getDow() == dow && js.getHour() == hour);
        }
        return false;
    }

    @Override
    public String toString() {
        return  name + " (" + id + ")";
    }
    
}
