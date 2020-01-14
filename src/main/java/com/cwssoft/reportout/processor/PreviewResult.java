package com.cwssoft.reportout.processor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by csyperski on 5/13/16.
 */
public class PreviewResult implements Serializable {

    private final List<String> headerTitles = new ArrayList<>();
    private final List<List<String>> data  = new ArrayList<>();

    private boolean successful = false;

    private String message;

    private String sql;

    public PreviewResult() {
    }

    public PreviewResult(boolean successful, String message) {
        this.successful = successful;
        this.message = message;
    }

    public List<String> getHeaderTitles() {
        return headerTitles;
    }

    public List<List<String>> getData() {
        return data;
    }

    public int getRowCount() {
        return data.size();
    }

    public int getColumnCount() {
        return headerTitles.size();
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
