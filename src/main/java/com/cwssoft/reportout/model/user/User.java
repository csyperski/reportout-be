package com.cwssoft.reportout.model.user;

import com.cwssoft.reportout.model.VersionedObject;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 *
 * @author csyperski
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idxEmail", columnList = "email"),
    @Index(name = "idxEnabled", columnList = "enabled"),
    })
@Getter
@Setter
public class User extends VersionedObject {

    @Column(length = 150, nullable = false, unique=true)
    private String email; // username
    
    @Column(length = 250, nullable = false, unique=false)
    private String password;
    
    @Column(length = 50, nullable = false)
    private String firstName;
    
    @Column(length = 50, nullable = false)
    private String lastName;
    
    private boolean administrator;
    
    private boolean enabled;
    
    private boolean passwordChangeRequested;
}
