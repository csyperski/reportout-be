/*
 *    2015 Charles Syperski <csyperski@cwssoft.com> - CWSSoftware LLC
 *  
 */

package com.cwssoft.reportout.processor;

import java.io.Serializable;

/**
 *
 * @author csyperski
 */
public class CsvBuilder implements Serializable {

    private StringBuilder sb;
    private boolean comma;
    
    public CsvBuilder() {
        sb = new StringBuilder();
        comma = false;
    }

    public void addCellEndLine(final String content ) {
        addCell(content);
        newLine();
    }
    
    public void addCellEndLine(final long l ) {
        addCell(l);
        newLine();
    }
       
    public void addCellEndLine(final int i ) {
        addCell(i);
        newLine();
    }
    
    public void addCellEndLine(final double d ) {
        addCell(d);
        newLine();
    }
    
    public void addCell() {
        addCell("");
    }
    
    public void addCell(final long l) {
        addCell(String.valueOf(l));
    }
    
    public void addCell(final int i) {
        addCell(String.valueOf(i));
    }
    
    public void addCell(final double d) {
        addCell(String.valueOf(d));
    }
    
    public void addCell(final String content) {
        if ( comma ) {
            sb.append(",");
        }
        comma = true;
        
        String realContent = content;
        if ( content == null ) {
            realContent = "";
        }
        
        realContent = realContent.replace('"', '\'');
        
        sb.append("\"");
        sb.append(realContent.trim());
        sb.append("\"");
    }
    
    public void newLine() {
        sb.append("\n");
        comma = false;
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
