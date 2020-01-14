package com.cwssoft.reportout.model.settings;

import com.cwssoft.reportout.model.VersionedObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "setting", indexes = {
    @Index(name="idxSettingKey", columnList="settingKey")
})
public class Setting extends VersionedObject {

    @Column(unique = true, nullable = false, length = 255)
    private String settingKey;

    private String settingValue;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    public Setting() {
    }

    public Setting(String key, String value) {
        this(key, value, "");
    }

    public Setting(String key, String value, String description) {
        settingKey = key;
        settingValue= value;
        this.description = description;
    }

    /**
     * @return the key
     */
    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public void setSettingValueAsLong(long v) {
        this.settingValue = String.valueOf(v);
    }

    public void setSettingValueAsInt(int v) {
        this.settingValue = String.valueOf(v);
    }

    public void setSettingValueAsBoolean(boolean v) {
        this.settingValue = v ? "1" : "0";
    }

    public long getSettingValueAsLong() throws NumberFormatException {
        return Long.parseLong(settingValue);
    }

    public int getSettingValueAsInt() throws NumberFormatException {
        return Integer.parseInt(settingValue);
    }

    public boolean getSettingValueAsBoolean() throws IllegalArgumentException {
        switch (settingValue.trim()) {
            case "1":
            case "T":
            case "t":
            case "Y":
            case "y":
                return true;
            case "0":
            case "F":
            case "f":
            case "N":
            case "n":
                return false;
            default:
                throw new IllegalArgumentException(settingValue + " can't be converted to a boolean");
        }
    }

    /**
     * @return the value
     */
    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return settingKey;
    }
}