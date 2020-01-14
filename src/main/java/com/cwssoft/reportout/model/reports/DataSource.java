package com.cwssoft.reportout.model.reports;

import com.cwssoft.reportout.model.VersionedObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import static com.cwssoft.reportout.util.StringUtils.isNullOrBlank;

/**
 *
 * @author csyperski
 */
@Entity
@Table(name = "ds", indexes = {
    @Index(name="idxName", columnList="name"),
    @Index(name="idxDsLimitToAdmin", columnList="limitToAdmin")
})
@JsonIgnoreProperties({"ready"})
public class DataSource extends VersionedObject {

    @Getter
    @Setter
    @Column(length=150, nullable=false, unique=true)
    private String name;

    @Getter
    @Setter
    @Column(length=255, nullable=false, unique=false)
    private String driverClass;

    @Getter
    @Setter
    @Column(length=4000, nullable=false, unique=false)
    private String connectionString;

    @Getter
    @Setter
    @Column(length=255, nullable=true, unique=false)
    private String userName;

    @Getter
    @Setter
    @Column(length=255, nullable=true, unique=false)
    private String password;

    @Getter
    @Setter
    @Column(length=255, nullable=true, unique=false)
    private String testQuery;

    @Getter
    @Setter
    @Column
    private boolean limitToAdmin = false;

    @JsonIgnore
    public boolean isReady() {
        return  ! isNullOrBlank(name) &&
                ! isNullOrBlank(driverClass) &&
                ! isNullOrBlank(connectionString);
    }

    @Override
    public String toString() {
        return "DataSource{" + "name=" + name + ", driverClass=" + driverClass + '}';
    }
}
