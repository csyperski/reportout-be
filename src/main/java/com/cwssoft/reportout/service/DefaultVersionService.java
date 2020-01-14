package com.cwssoft.reportout.service;

import com.cwssoft.reportout.model.version.SystemVersion;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Optional;
import java.util.Scanner;

/**
 * @author csyperski
 */
@Service
@Slf4j
public class DefaultVersionService implements VersionService {

    @Getter
    @Setter
    @Value("${reportout.version.url}")
    private String checkerUrl;

    @Override
    public Optional<SystemVersion> getLatestVersion(String current) {
        if (checkerUrl != null) {
            try {
                URL url = new URL(checkerUrl);

                StringBuilder sb = new StringBuilder();
                Scanner s = new Scanner(url.openStream());
                while (s.hasNext()) {
                    sb.append(s.next());
                    sb.append(" ");
                }
                String version = sb.toString();
                return parse(version, current);
            } catch (Exception e) {
                log.warn("Unable to get version from remote server: {}", e.getMessage(), e);
            }
        }
        return Optional.empty();
    }

    private Optional<SystemVersion> parse(String version, String current) {
        if (version != null && version.contains("|")) {
            String[] parts = version.split("\\|");
            if (parts != null && parts.length == 2 &&
                    parts[0] != null && parts[0].length() > 0 &&
                    parts[1] != null && parts[1].length() > 0) {
                return Optional.of(new SystemVersion(parts[0].trim(), parts[1].trim(), current));
            }
        }
        return Optional.empty();
    }
}