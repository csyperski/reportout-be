/*
 *  2012 Charles Syperski <csyperski@cwssoft.com>
 *
 */
package com.cwssoft.reportout.model;

import java.io.Serializable;

/**
 * @author csyperski
 */
public interface DomainObject extends Serializable {
    /* This allows us to create a generic JSF converter */
    Long getId();
}
