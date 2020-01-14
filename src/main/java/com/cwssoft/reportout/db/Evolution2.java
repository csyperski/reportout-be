package com.cwssoft.reportout.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Evolution2 extends BaseUpdatePackage {

    @Override
    protected String[] getQueries() {

        return new String[]{
                // Add limitedAccess field to DS table
                "alter table ds add limit_to_admin boolean;",
                "create index idxDsLimitToAdmin on ds (limit_to_admin);",
                "update ds set limit_to_admin = false;"
        };
    }

    @Override
    public int getFromVersion() {
        return 1;
    }
}
