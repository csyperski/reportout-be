package com.cwssoft.reportout.model.version;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by csyperski on 5/18/16.
 */
public class SystemVersionTest {
    @Test
    public void isExpired()  {
        System.out.println("isExpired - New Version");

        SystemVersion instance = new SystemVersion("1.0.0", "1.0.0", "1.0.0");
        boolean result = instance.isExpired();

        assertEquals(false, result);
    }

    @Test
    public void isExpired1()  {
        System.out.println("isExpired - Yesterday Checked");

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1);
        Date date = c.getTime();

        SystemVersion instance = new SystemVersion("1.0.0", "1.0.0", "1.0.0", date);
        boolean result = instance.isExpired();

        assertEquals(true, result);
    }

    @Test
    public void isExpired2()  {
        System.out.println("isExpired - 12 hours + 1 second");

        Calendar c = Calendar.getInstance();
        c.add(Calendar.MILLISECOND, -1 * (SystemVersion.EXPIRE + 1000));
        Date date = c.getTime();

        SystemVersion instance = new SystemVersion("1.0.0", "1.0.0", "1.0.0", date);
        boolean result = instance.isExpired();

        assertEquals(true, result);
    }

    @Test
    public void isExpired3() {
        System.out.println("isExpired - 11:59:58 hours");

        Calendar c = Calendar.getInstance();
        c.add(Calendar.MILLISECOND, -1 * (SystemVersion.EXPIRE - 2000));
        Date date = c.getTime();

        SystemVersion instance = new SystemVersion("1.0.0", "1.0.0", "1.0.0", date);
        boolean result = instance.isExpired();

        assertEquals(false, result);
    }

}