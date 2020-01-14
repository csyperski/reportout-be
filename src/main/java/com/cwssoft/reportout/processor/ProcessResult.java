/*
 *   2016 Charles Syperski <csyperski@cwssoft.com> - CWS Software LLC
 */
package com.cwssoft.reportout.processor;

import com.cwssoft.reportout.model.VersionedObject;
import com.cwssoft.reportout.model.reports.Job;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Base64;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 *
 * @author csyperski
 */
@Entity
@Table(name = "processResults", indexes = {
    @Index(name="idxPrJobId", columnList="jobId"),
    @Index(name="idxPrExecutor", columnList="executedBy"),
    @Index(name="idxPrSuccess", columnList="successful"),
    @Index(name="idxPrExecutedByScheduler", columnList="executedByScheduler"),
    @Index(name="idxPrDateStarted", columnList="dateStarted")
    
})
public class ProcessResult extends VersionedObject {

    @Getter
    @Setter
    @Column
    private boolean successful = false;

    @Getter
    @Setter
    @Column(length=4000, nullable=true, unique=false)
    private String message;

    @Getter
    @Setter
    @Column
    private long records = 0;

    @Getter
    @Setter
    @Column(length=1, nullable=true, unique=false)
    private String data; // base64; ugly; we'll need to find a better method
                         // we never want to persist this to the database either!

    @Getter
    @Setter
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date dateStarted = null;

    @Getter
    @Setter
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date dateCompleted = null;

    @Getter
    @Setter
    @Column
    private boolean executedByScheduler = false;

    @Getter
    @Setter
    @Column
    private long executedBy = -1l;

    @Getter
    @Setter
    @Column
    private long jobId = -1l;

    @Getter
    @Setter
    @Transient
    private Job job;

    public ProcessResult() {
        dateStarted = new Date();
    }
    
    public ProcessResult(ProcessResult template) {
        this();
        this.data = template.data;
        this.dateCompleted = template.dateCompleted;
        this.dateStarted = template.dateStarted;
        this.executedBy = template.executedBy;
        this.executedByScheduler = template.executedByScheduler;
        this.id = template.id;
        this.jobId = template.jobId;
        this.message = template.message;
        this.records = template.records;
        this.successful = template.successful;
        this.setVersion(template.getVersion());
    }
    
    public ProcessResult(boolean successful, String message) {
        this(successful, message, null);
    }
    
    public ProcessResult(boolean successful, String message, String data) {
        this();
        this.successful = successful;
        this.message = message;
        this.data = data;
    }

    /**
     * @param data the reports to set
     */
    public void setByteData(byte[] data) {
        this.data = Base64.getEncoder().encodeToString(data);
    }
    
}
