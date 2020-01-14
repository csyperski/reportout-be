/*
 *   2016 Charles Syperski <csyperski@cwssoft.com> - CWS Software LLC
 */
package com.cwssoft.reportout.model.reports;

import com.cwssoft.reportout.model.VersionedObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author csyperski
 */
@Entity
@Table(name = "jobSchedule", indexes = {
    @Index(name="idxHour", columnList="hour"),
    @Index(name="idxDow", columnList="dow")
})
@JsonIgnoreProperties({"dayMap", "selected"})
public class JobSchedule extends VersionedObject {

    @Getter
    @Setter
    @Column
    private int hour;  //0 = 12am
                       // 1 = 1am
                       // ...
                       // 23 = 11pm

    @Getter
    @Setter
    @Column
    private int dow;  // Calendar.SUNDAY

    @Getter
    @Setter
    @JsonIgnore
    @ManyToOne
    private Job job;

    public JobSchedule() {
    }
    
    public JobSchedule(JobSchedule jobSchedule, Job job) {
        if ( jobSchedule != null && job != null) {
            this.hour = jobSchedule.hour;
            this.dow = jobSchedule.dow;
            this.job = job;
        }
    }
    
    public JobSchedule(int hour, int dow, Job job) {
        this.hour = hour;
        this.dow = dow;
        this.job = job;
    }
}