/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cwssoft.reportout.processor;

import java.io.Serializable;

/**
 *
 * @author csyperski
 */
public interface StringReplacer extends Serializable {
    String requires();
    String process(String message, Object requiredObject, boolean escape);
}
