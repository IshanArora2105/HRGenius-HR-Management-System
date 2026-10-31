package com.hrgenius.service;
import java.math.*;
/** Centralises the specified payroll formulas and HALF_UP monetary rounding. */
public final class PayrollCalculator {
 private static final BigDecimal HUNDRED=new BigDecimal("100");
 private PayrollCalculator(){}
 public record Result(BigDecimal gross,BigDecimal pf,BigDecimal tds,BigDecimal otherDeduction,BigDecimal totalDeductions,BigDecimal net){}
 private static BigDecimal money(BigDecimal n){return n.setScale(2,RoundingMode.HALF_UP);}
 public static BigDecimal monthlyTds(BigDecimal monthlyGross){
  BigDecimal annual=monthlyGross.multiply(BigDecimal.valueOf(12));
  BigDecimal rate=annual.compareTo(new BigDecimal("300000"))<=0?BigDecimal.ZERO:
   annual.compareTo(new BigDecimal("600000"))<=0?new BigDecimal("0.05"):
   annual.compareTo(new BigDecimal("1000000"))<=0?new BigDecimal("0.10"):new BigDecimal("0.15");
  return money(monthlyGross.multiply(rate));
 }
 public static Result calculate(BigDecimal basic,BigDecimal hra,BigDecimal allowances,BigDecimal otherDeduction,BigDecimal tds){
  BigDecimal gross=money(basic.add(hra).add(allowances)); BigDecimal pf=money(basic.multiply(new BigDecimal("0.12")));
  BigDecimal tax=money(tds); BigDecimal other=money(otherDeduction);
  BigDecimal total=money(pf.add(tax).add(other)); return new Result(gross,pf,tax,other,total,money(gross.subtract(total)));
 }
}
