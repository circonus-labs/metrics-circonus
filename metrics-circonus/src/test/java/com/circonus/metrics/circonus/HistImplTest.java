package com.circonus.metrics.circonus;

import com.circonus.metrics.circonus.HistImpl;

import java.lang.Math;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertArrayEquals;

public class HistImplTest {

  @Test
  public void testHistBin() {
    HistImpl.HistBin hb;

    hb = new HistImpl.HistBin(0.0);
    assertEquals(0, hb.getVal());
    assertEquals(0, hb.getExp());

    hb = new HistImpl.HistBin(9.9999e-129);
    assertEquals(0, hb.getVal());
    assertEquals(0, hb.getExp());

    hb = new HistImpl.HistBin(1e-128);
    assertEquals(10, hb.getVal());
    assertEquals(-128, hb.getExp());

    hb = new HistImpl.HistBin(1.00001e-128);
    assertEquals(10, hb.getVal());
    assertEquals(-128, hb.getExp());
    
    hb = new HistImpl.HistBin(1.09999e-128);
    assertEquals(10, hb.getVal());
    assertEquals(-128, hb.getExp());
    
    hb = new HistImpl.HistBin(1.1e-128);
    assertEquals(11, hb.getVal());
    assertEquals(-128, hb.getExp());
    
    hb = new HistImpl.HistBin(1e127);
    assertEquals(10, hb.getVal());
    assertEquals(127, hb.getExp());
    
    hb = new HistImpl.HistBin(9.999e127);
    assertEquals(99, hb.getVal());
    assertEquals(127, hb.getExp());
    
    hb = new HistImpl.HistBin(1e128);
    assertEquals(-1, hb.getVal());
    assertEquals(0, hb.getExp());

    hb = new HistImpl.HistBin(-9.9999e-129);
    assertEquals(0, hb.getVal());
    assertEquals(0, hb.getExp());

    hb = new HistImpl.HistBin(-1e-128);
    assertEquals(-10, hb.getVal());
    assertEquals(-128, hb.getExp());

    hb = new HistImpl.HistBin(-1.00001e-128);
    assertEquals(-10, hb.getVal());
    assertEquals(-128, hb.getExp());
    
    hb = new HistImpl.HistBin(-1.09999e-128);
    assertEquals(-10, hb.getVal());
    assertEquals(-128, hb.getExp());
    
    hb = new HistImpl.HistBin(-1.1e-128);
    assertEquals(-11, hb.getVal());
    assertEquals(-128, hb.getExp());
    
    hb = new HistImpl.HistBin(-1e127);
    assertEquals(-10, hb.getVal());
    assertEquals(127, hb.getExp());
    
    hb = new HistImpl.HistBin(-9.999e127);
    assertEquals(-99, hb.getVal());
    assertEquals(127, hb.getExp());
    
    hb = new HistImpl.HistBin(9.999e127);
    assertEquals(99, hb.getVal());
    assertEquals(127, hb.getExp());
  }

  public void test1(Double val, Double b, Double w) {
    HistImpl.HistBin in = new HistImpl.HistBin(val);
    Double out = in.getDouble();
    Double interval = in.getBinWidth();
    if(out < 0) interval *= -1.0;
    assertEquals(b,out,Math.abs(b/100000.0));
    assertEquals(w,interval,Math.abs(interval/100000.0));
  }

  @Test
  public void testHistBinSizes() {
    test1(43.3, 43.0, 1.0);
    test1(99.9, 99.0, 1.0);
    test1(10.0, 10.0, 1.0);
    test1(1.0, 1.0, 0.1);
    test1(0.0002, 0.0002, 0.00001);
    test1(0.003, 0.003, 0.0001);
    test1(0.3201, 0.32, 0.01);
    test1(0.0035, 0.0035, 0.0001);
    test1(-1.0, -1.0, -0.1);
    test1(-0.00123, -0.0012, -0.0001);
    test1(-987324.0, -980000.0, -10000.0);
  }

  public final Double[] s1 = new Double[]
      { 0.123, 0.0, 0.43, 0.41, 0.415, 0.2201, 0.3201, 0.125, 0.13 };

  public void mean_test(Double[] vals, Double expected) {
    int i;
    HistImpl hist = new HistImpl();
    for(i=0; i<vals.length; i++) hist.insert(vals[i]);
    assertEquals(hist.getApproxMean(), expected, Math.abs(expected/100000.0));
  }

  @Test
  public void testMean() {
    mean_test(s1, 0.244444444);
  }

  public void q_test(Double[] vals, Double[] in, Double[] expected) {
    int i;
    HistImpl hist = new HistImpl();
    for(i=0; i<vals.length; i++) hist.insert(vals[i]);
    Double[] out = hist.getApproxQuantiles(in);
    assertNotNull(out);
    for(i=0;i<out.length;i++) {
      assertEquals(expected[i], out[i], Math.abs(expected[i]/100000.0));
    }
  }

  @Test
  public void testQuantiles() {
    q_test(new Double[] { 1.0 },
           new Double[] { 0.0, 0.25, 0.5, 1.0 },
           new Double[] { 1.0, 1.025, 1.05, 1.1 });
    q_test(new Double[] { 1.0 },
           new Double[] { 0.0, 0.25, 0.5, 1.0 },
           new Double[] { 1.0, 1.025, 1.05, 1.1 });
    q_test(s1,
           new Double[] { 0.0, 0.95, 0.99, 1.0 },
           new Double[] { 0.0, 0.4355, 0.4391, 0.44 });
    q_test(new Double[] { 1.0, 2.0 },
           new Double[] { 0.5 },
           new Double[] { 1.1 });
  }
}
