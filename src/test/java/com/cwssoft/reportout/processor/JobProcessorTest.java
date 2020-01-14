/*
 *  Copyright (C) 2016 Charles Syperski <csyperski@gmail.com> - CWS Software LLC
 */
package com.cwssoft.reportout.processor;

import java.math.BigDecimal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author csyperski
 */
public class JobProcessorTest {
    
    public JobProcessorTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }


    /**
     * Test of bdToString method, of class JobProcessor.
     */
    @Test
    public void testBdToStringBigNumber() {
        System.out.println("bdToString - Big Number");
        BigDecimal bd = new BigDecimal("88888888888.88888888822228338383833");
        JobProcessor instance = new JobProcessor();
        String expResult = "88888888888.888889";
        String result = instance.bdToString(bd);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testBdToStringOneThird() {
        System.out.println("bdToString - 1/3 ");
        double d = 1d/3d;
        BigDecimal bd = new BigDecimal(d);
        JobProcessor instance = new JobProcessor();
        String expResult = "0.333333";
        String result = instance.bdToString(bd);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testBdToStringMaxInt() {
        System.out.println("bdToString - Max Int");
        BigDecimal bd = new BigDecimal(Integer.MAX_VALUE);
        JobProcessor instance = new JobProcessor();
        String expResult = Integer.MAX_VALUE + "";
        String result = instance.bdToString(bd);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testBdToStringMinInt() {
        System.out.println("bdToString - MIN Int");
        BigDecimal bd = new BigDecimal(Integer.MIN_VALUE);
        JobProcessor instance = new JobProcessor();
        String expResult = Integer.MIN_VALUE + "";
        String result = instance.bdToString(bd);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testBdToStringZero() {
        System.out.println("bdToString - 0");
        BigDecimal bd = new BigDecimal(0);
        JobProcessor instance = new JobProcessor();
        String expResult = "0";
        String result = instance.bdToString(bd);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testBdToStringSmall() {
        System.out.println("bdToString - 0.000000000000000000000000001");
        BigDecimal bd = new BigDecimal(0.000000000000000000000000001);
        JobProcessor instance = new JobProcessor();
        String expResult = "0";
        String result = instance.bdToString(bd);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testBdToStringTwoPointFive() {
        System.out.println("bdToString - 2.5");
        BigDecimal bd = new BigDecimal(2.5);
        JobProcessor instance = new JobProcessor();
        String expResult = "2.5";
        String result = instance.bdToString(bd);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testBdToStringThreePointZero() {
        System.out.println("bdToString - 3.0");
        BigDecimal bd = new BigDecimal(3.0);
        JobProcessor instance = new JobProcessor();
        String expResult = "3";
        String result = instance.bdToString(bd);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testBdToStringThreeLotsOfZeros() {
        System.out.println("bdToString - 3.0000001");
        BigDecimal bd = new BigDecimal(3.0000001);
        JobProcessor instance = new JobProcessor();
        String expResult = "3";
        String result = instance.bdToString(bd);
        assertEquals(expResult, result);
    }
}
