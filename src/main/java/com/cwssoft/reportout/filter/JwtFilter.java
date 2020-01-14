package com.cwssoft.reportout.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cwssoft.reportout.service.JobService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * @author csyperski
 */
@Slf4j
@Component
@WebFilter(filterName = "jwtAuthFilter", urlPatterns = "/api/*")
@Order(Integer.MIN_VALUE)
public class JwtFilter implements Filter {

    private JWTVerifier jwtVerifier;

    @Getter
    @Setter
    @Value("${reportout.jwt.urlpattern}")
    private String pattern;

    @Getter
    @Setter
    @Value("${reportout.jwt.auth}")
    private String excludePattern;

    @Getter
    @Setter
    @Value(value = "${reportout.jwt.issuer:reportout}")
    private String issuer;

    @Getter
    @Setter
    @Value(value = "${reportout.jwt.aud:https://ro.cwssoft.com}")
    private String audience;

    @Setter
    @Getter
    @Inject
    private Algorithm algorithm;

    @Setter
    @Getter
    @Inject
    private JobService jobService;

    @Override
    public void init(FilterConfig fc) throws ServletException {
        log.info("String authentication filter...");

        jwtVerifier = JWT.require(algorithm).withAudience(audience).withIssuer(issuer).build();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain fc) throws IOException, ServletException {
        if (!appliesToMe((HttpServletRequest) servletRequest)) {
            String pi = getPathPart((HttpServletRequest) servletRequest);
            fc.doFilter(servletRequest, servletResponse);
            return;
        }

        log.info("Applies to me: {}", servletRequest.toString());
        String token = getToken((HttpServletRequest) servletRequest);
        try {
            DecodedJWT decodedJWT = jwtVerifier.verify(token);
            fc.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            log.error("Unauthorized: Token validation failed", e);
            ((HttpServletResponse) servletResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @Override
    public void destroy() {
    }

    private String getPathPart(HttpServletRequest request) {
        if (request != null) {
            return request.getRequestURI();
        }
        return null;
    }

    private boolean appliesToMe(HttpServletRequest request) {
        String path = getPathPart(request);
        boolean matches = request == null ||
                path == null ||
                pattern == null ||
                pattern.trim().length() == 0 ||
                path.toLowerCase().trim().startsWith(pattern.toLowerCase().trim());

        if (matches) {
            // check if excluded 
            if (excludePattern != null && excludePattern.trim().length() > 0) {
                String trim = excludePattern.trim().toLowerCase();
                if (path.toLowerCase().trim().startsWith(trim)) {
                    log.info("Path is excluded: {}", trim);
                    return false;
                }

            }
        }

        // if this is an options request - preflight for CORS then ignore
        if (request != null &&
                request.getMethod() != null &&
                request.getMethod().equalsIgnoreCase(RequestMethod.OPTIONS.toString())) {
            log.debug("Ignoring Options query");
            return false;
        }

        return matches;
    }

    private String getToken(HttpServletRequest httpRequest) throws ServletException {
        String token = null;
        final String authorizationHeader = httpRequest.getHeader("authorization");
        if (authorizationHeader == null) {
            throw new ServletException("Unauthorized: No Authorization header was found");
        }

        String[] parts = authorizationHeader.split(" ");
        if (parts.length != 2) {
            throw new ServletException("Unauthorized: Format is Authorization: Bearer [token]");
        }

        String scheme = parts[0];
        String credentials = parts[1];

        Pattern pattern = Pattern.compile("^Bearer$", Pattern.CASE_INSENSITIVE);
        if (pattern.matcher(scheme).matches()) {
            token = credentials;
        }
        return token;
    }

}
