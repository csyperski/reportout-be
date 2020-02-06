package com.cwssoft.reportout.db;

import org.springframework.stereotype.Component;

@Component
public class Evolution3 extends BaseUpdatePackage {

    @Override
    protected String[] getQueries() {

        return new String[]{
                // Add limitedAccess field to DS table
                "alter table jobs add only_send_if_results boolean;",
                "update jobs set only_send_if_results = false;"
        };
    }

    @Override
    public int getFromVersion() {
        return 2;
    }
}
