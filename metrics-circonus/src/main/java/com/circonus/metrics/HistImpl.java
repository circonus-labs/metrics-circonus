package com.circonus.metrics;

/*
 * Copyright (c) 2012-2016, Circonus, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name Circonus, Inc. nor the names of its contributors
 *       may be used to endorse or promote products derived from this
 *       software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.lang.Math;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class HistImpl {
  static final short DEFAULT_HIST_SIZE = 100;
  static final double[] power_of_ten = {
    1, 10, 100, 1000, 10000, 100000, 1e+06, 1e+07, 1e+08, 1e+09, 1e+10,
    1e+11, 1e+12, 1e+13, 1e+14, 1e+15, 1e+16, 1e+17, 1e+18, 1e+19, 1e+20,
    1e+21, 1e+22, 1e+23, 1e+24, 1e+25, 1e+26, 1e+27, 1e+28, 1e+29, 1e+30,
    1e+31, 1e+32, 1e+33, 1e+34, 1e+35, 1e+36, 1e+37, 1e+38, 1e+39, 1e+40,
    1e+41, 1e+42, 1e+43, 1e+44, 1e+45, 1e+46, 1e+47, 1e+48, 1e+49, 1e+50,
    1e+51, 1e+52, 1e+53, 1e+54, 1e+55, 1e+56, 1e+57, 1e+58, 1e+59, 1e+60,
    1e+61, 1e+62, 1e+63, 1e+64, 1e+65, 1e+66, 1e+67, 1e+68, 1e+69, 1e+70,
    1e+71, 1e+72, 1e+73, 1e+74, 1e+75, 1e+76, 1e+77, 1e+78, 1e+79, 1e+80,
    1e+81, 1e+82, 1e+83, 1e+84, 1e+85, 1e+86, 1e+87, 1e+88, 1e+89, 1e+90,
    1e+91, 1e+92, 1e+93, 1e+94, 1e+95, 1e+96, 1e+97, 1e+98, 1e+99, 1e+100,
    1e+101, 1e+102, 1e+103, 1e+104, 1e+105, 1e+106, 1e+107, 1e+108, 1e+109,
    1e+110, 1e+111, 1e+112, 1e+113, 1e+114, 1e+115, 1e+116, 1e+117, 1e+118,
    1e+119, 1e+120, 1e+121, 1e+122, 1e+123, 1e+124, 1e+125, 1e+126, 1e+127,
    1e-128, 1e-127, 1e-126, 1e-125, 1e-124, 1e-123, 1e-122, 1e-121, 1e-120,
    1e-119, 1e-118, 1e-117, 1e-116, 1e-115, 1e-114, 1e-113, 1e-112, 1e-111,
    1e-110, 1e-109, 1e-108, 1e-107, 1e-106, 1e-105, 1e-104, 1e-103, 1e-102,
    1e-101, 1e-100, 1e-99, 1e-98, 1e-97, 1e-96,
    1e-95, 1e-94, 1e-93, 1e-92, 1e-91, 1e-90, 1e-89, 1e-88, 1e-87, 1e-86,
    1e-85, 1e-84, 1e-83, 1e-82, 1e-81, 1e-80, 1e-79, 1e-78, 1e-77, 1e-76,
    1e-75, 1e-74, 1e-73, 1e-72, 1e-71, 1e-70, 1e-69, 1e-68, 1e-67, 1e-66,
    1e-65, 1e-64, 1e-63, 1e-62, 1e-61, 1e-60, 1e-59, 1e-58, 1e-57, 1e-56,
    1e-55, 1e-54, 1e-53, 1e-52, 1e-51, 1e-50, 1e-49, 1e-48, 1e-47, 1e-46,
    1e-45, 1e-44, 1e-43, 1e-42, 1e-41, 1e-40, 1e-39, 1e-38, 1e-37, 1e-36,
    1e-35, 1e-34, 1e-33, 1e-32, 1e-31, 1e-30, 1e-29, 1e-28, 1e-27, 1e-26,
    1e-25, 1e-24, 1e-23, 1e-22, 1e-21, 1e-20, 1e-19, 1e-18, 1e-17, 1e-16,
    1e-15, 1e-14, 1e-13, 1e-12, 1e-11, 1e-10, 1e-09, 1e-08, 1e-07, 1e-06,
    1e-05, 0.0001, 0.001, 0.01, 0.1
  };

  public static class HistBin implements Comparable<HistBin> {
    private byte val;
    private byte exp;
    private long count;

    public HistBin(Integer d) { this(d.doubleValue()); }
    public HistBin(Integer d, long c) { this(d.doubleValue(), c); }
    public HistBin(Long d) { this(d.doubleValue(), 1); }
    public HistBin(Long d, long c) { this(d.doubleValue(), c); }
    public HistBin(Double d) { this(d,1); }
    public HistBin(Double d, long c) {
      val = (byte)0xff;
      exp = 0;
      count = c;
      if(d.isNaN() || d.isInfinite()) return;
      else if(d == 0.0) val = 0;
      else {
        int big_exp, pidx, sign;
        sign = (d < 0) ? -1 : 1;
        d = Math.abs(d);
        big_exp = (int)Math.floor(Math.log10(d));
        exp = (byte)big_exp;
        if(exp != big_exp) { // rolled
          if(big_exp < 0) {
            val = exp = 0;
          } else {
            val = (byte)0xff;
            exp = 0;
          }
          return;
        }
        pidx = (int)exp & 0x00ff;
        d /= power_of_ten[pidx];
        d *= 10;
        val = (byte)(sign * (int)(Math.floor(d + 1e-13)));
        if(val == 100 || val == -100) {
          if(val < 127) {
            val /= 10;
            exp++;
          } else {
            val = exp = 0;
          }
        }
        if(val == 0) {
          exp = 0;
          return;
        }
        if(!((val >= 10 && val < 100) ||
             (val <= -10 && val > -100))) {
          val = (byte)0xff;
          exp = 0;
        }
      }
    }
    public HistBin(byte v, byte e, long c) { val = v; exp = e; count = c; }
    public HistBin(byte v, byte e) { this(v,e,1); }
    public byte getVal() { return val; }
    public byte getExp() { return exp; }
    public void setVal(byte v) { val = v; }
    public void setExp(byte e) { exp = e; }
    public long getCount() { return count; }
    public void setCount(long nc) { count = nc; }

    public Double getDouble() {
      int pidx = ((int)exp & 0x00ff);
      if(val > 99 || val < -99) return Double.NaN;
      if(val < 10 && val > -10) return 0.0;
      return (((double)val)/10.0) * power_of_ten[pidx];
    }
    public Double getBinWidth() {
      int pidx = ((int)exp & 0x00ff);
      if(val > 99 || val < -99) return Double.NaN;
      if(val < 10 && val > -10) return 0.0;
      return power_of_ten[pidx]/10.0;
    }
    public Double getMidpoint() {
      if(val > 99 || val < -99) return Double.NaN;
      Double out = getDouble();
      if(out == 0.0) return 0.0;
      Double interval = getBinWidth();
      if(out < 0.0) return out - interval/2.0;
      return out + interval/2.0;
    }
    public Double getLeft() {
      if(val > 99 || val < -99) return Double.NaN;
      Double out = getDouble();
      if(out >= 0.0) return out;
      return out - getBinWidth();
    }
    @Override
    public int compareTo(HistBin that) {
      if(this.val == that.val && this.exp == that.exp) return 0;
      if(this.val == (byte)0xff) return 1;
      if(that.val == (byte)0xff) return 1;
      if(this.val == 0) return (that.val > 0) ? 1 : -1;
      if(that.val == 0) return (this.val < 0) ? 1 : -1;
      if(this.val < 0 && that.val > 0) return 1;
      if(this.val > 0 && that.val < 0) return -1;
      if(this.exp == that.exp) return (this.val < that.val) ? 1 : -1;
      if(this.exp > that.exp) return (this.val < 0) ? 1 : -1;
      if(this.exp < that.exp) return (this.val < 0) ? -1 : 1;
      return 0;
    }
  }

  private short allocd;
  private short used;
  private HistBin[] bvs;
  private Object lock = new Object();

  // Returns the idx if found or the -1-idx of where it should inserted
  public HistImpl(short size) {
    bvs = new HistBin[size];
    used = 0;
    allocd = size;
  }
  public HistImpl() { this(DEFAULT_HIST_SIZE); }
  protected HistImpl(HistBin[] init) {
    bvs = init;
    used = allocd = (short)bvs.length;
  }

  private int internalFind(HistBin hb) {
    int rv = -1, l = 0, r = used - 1, idx = 0;
    if(used == 0) return -1;
    while(l < r) {
      int check = (r+l)/2;
      rv = bvs[check].compareTo(hb);
      if(rv == 0) l = r = check;
      else if(rv > 0) l = check + 1;
      else r = check - 1;
    }
    if(rv != 0) rv = bvs[l].compareTo(hb);
    idx = l;
    if(rv == 0) return idx;
    if(rv < 0) return -1-idx;
    return -2-idx;
  }

  public synchronized void clear() { used = 0; }
  public Short numBins() { return used; }
  public synchronized long insert(HistBin hb) {
    long count = hb.getCount();
    if(hb.getCount() == 0) return 0;
    if(bvs == null) {
      bvs = new HistBin[DEFAULT_HIST_SIZE];
      allocd = DEFAULT_HIST_SIZE;
    }
    int idx = internalFind(hb);
    if(idx < 0) {
      /* Not found */
      idx = -1-idx;
      if(used == allocd) {
        HistBin[] new_bvs = new HistBin[allocd + DEFAULT_HIST_SIZE];
        if(idx > 0)
          System.arraycopy(bvs, 0, new_bvs, 0, idx);
        new_bvs[idx] = hb;
        if(idx < used)
          System.arraycopy(bvs, idx, new_bvs, idx + 1, used - idx);
        bvs = new_bvs;
        allocd = (short)(allocd + DEFAULT_HIST_SIZE);
      }
      else {
        System.arraycopy(bvs, idx, bvs, idx + 1, used - idx);
        bvs[idx] = hb;
      }
      used++;
    } else {
      long newval = bvs[idx].getCount() + count;
      if(newval < bvs[idx].getCount()) // roll
        bvs[idx].setCount(Long.MAX_VALUE);
      count = newval - bvs[idx].getCount();
      bvs[idx].setCount(newval);
    }
    return count;
  }
  public long insert(Double val, long count) {
    return insert(new HistBin(val, count));
  }
  public long insert(Double val) { return insert(new HistBin(val, 1)); }
  public long insert(Integer val, long count) {
    return insert(new HistBin(val, count));
  }
  public long insert(Integer val) { return insert(new HistBin(val, 1)); }
  public long insert(Long val, long count) {
    return insert(new HistBin(val, count));
  }
  public long insert(Long val) { return insert(new HistBin(val, 1)); }

  public synchronized Double getApproxMean() {
    double divisor = 0.0;
    double sum = 0.0;
    for(int i=0;i<used;i++) {
      if(bvs[i].getVal() > 99 || bvs[i].getVal() < -99) continue;
      double midpoint = bvs[i].getMidpoint();
      double cardinality = (double)bvs[i].getCount();
      divisor += cardinality;
      sum += midpoint * cardinality;
    }
    if(divisor == 0.0) return Double.NaN;
    return sum/divisor;
  }
  public synchronized Double getApproxSum() {
    int i;
    double sum = 0.0;
    for(i=0;i<used;i++) {
      if(bvs[i].getVal() > 99 || bvs[i].getVal() < -99) continue;
      double value = bvs[i].getMidpoint();
      double cardinality = (double)bvs[i].getCount();
      sum += value * cardinality;
    }
    return sum;
  }
  public synchronized Double[] getApproxQuantiles(Double[] q_in) {
    int i_q, i_b;
    double total_cnt = 0.0, bucket_width = 0.0,
       bucket_left = 0.0, lower_cnt = 0.0, upper_cnt = 0.0;
    if(q_in.length < 1) return null;
    for(i_b=0;i_b<used;i_b++) {
      if(bvs[i_b].getVal() < -99 || bvs[i_b].getVal() > 99) continue;
      total_cnt += (double)bvs[i_b].getCount();
    }
    for(i_q=1;i_q<q_in.length;i_q++) if(q_in[i_q-1] > q_in[i_q]) return null;

    Double[] q_out = new Double[q_in.length];
    for(i_q=0;i_q<q_in.length;i_q++) {
      if(q_in[i_q] < 0.0 || q_in[i_q] > 1.0) return null;
      q_out[i_q] = total_cnt * q_in[i_q];
    } 

    for(i_b=0;i_b<used;i_b++) {
      if(bvs[i_b].getVal() < -99 || bvs[i_b].getVal() > 99) continue;
      bucket_width = bvs[i_b].getBinWidth();
      bucket_left = bvs[i_b].getLeft();
      lower_cnt = upper_cnt;
      upper_cnt = lower_cnt + bvs[i_b].getCount();
      break;
    }

    for(i_q=0;i_q<q_in.length;i_q++) {
      while(i_b < (used-1) && upper_cnt < q_out[i_q]) {
        i_b++;
        bucket_width = bvs[i_b].getBinWidth();
        bucket_left = bvs[i_b].getLeft();
        lower_cnt = upper_cnt;
        upper_cnt = lower_cnt + bvs[i_b].getCount();
      }
      if(lower_cnt == q_out[i_q]) {
        q_out[i_q] = bucket_left;
      }
      else if(upper_cnt == q_out[i_q]) {
        q_out[i_q] = bucket_left + bucket_width;
      }
      else {
        if(bucket_width == 0) q_out[i_q] = bucket_left;
        else q_out[i_q] = bucket_left +
               (q_out[i_q] - lower_cnt) / (upper_cnt - lower_cnt) * bucket_width;
      }
    }
    return q_out;
  }

  static final NumberFormat formatter = new DecimalFormat("0.#E0");

  public synchronized String[] toDecStrings() {
    String[] out = new String[used];
    for(int i=0;i<used;i++) {
      out[i] = "H[" + formatter.format(bvs[i].getDouble()) + "]=" +
               bvs[i].getCount();
    }
    return out;
  }
  public synchronized HistImpl copy() {
    HistBin[] slice = new HistBin[used];
    System.arraycopy(bvs, 0, slice, 0, used);
    return new HistImpl(slice);
  }
  private synchronized void swap(HistImpl other) {
    synchronized(other) {
      HistBin[] tmp_bvs;
      short tmp_used, tmp_allocd;
      tmp_bvs = this.bvs;   tmp_used = this.used;   tmp_allocd = this.allocd;
      this.bvs = other.bvs; this.used = other.used; this.allocd = other.allocd;
      other.bvs = tmp_bvs;  other.used = tmp_used;  other.allocd = tmp_allocd;
    }
  }
  public synchronized HistImpl copyAndReset() {
    HistImpl replacement = new HistImpl();
    swap(replacement);
    return replacement;
  }
}
