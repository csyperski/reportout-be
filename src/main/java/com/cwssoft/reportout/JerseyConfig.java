package com.cwssoft.reportout;

import com.cwssoft.reportout.api.v1.AuthEndPoint;
import com.cwssoft.reportout.api.v1.DataSourceEndPoint;
import com.cwssoft.reportout.api.v1.JobEndPoint;
import com.cwssoft.reportout.api.v1.ProcessResultEndPoint;
import com.cwssoft.reportout.api.v1.UserEndPoint;
import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

@Component
@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(DataSourceEndPoint.class);
        register(UserEndPoint.class);
        register(AuthEndPoint.class);
        register(JobEndPoint.class);
        register(ProcessResultEndPoint.class);
    }
}
