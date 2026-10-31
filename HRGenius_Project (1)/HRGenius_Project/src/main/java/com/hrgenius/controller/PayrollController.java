package com.hrgenius.controller;

import com.hrgenius.entity.Employee;
import com.hrgenius.entity.Payroll;
import com.hrgenius.entity.SalaryStructure;
import com.hrgenius.repository.EmployeeRepository;
import com.hrgenius.repository.PayrollRepository;
import com.hrgenius.repository.SalaryStructureRepository;
import com.hrgenius.service.PayrollCalculator;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Month;
import java.util.*;

@RestController
@RequestMapping("/api/v1/payroll")
public class PayrollController {
 private final PayrollRepository payrollRepo;
 private final EmployeeRepository employeeRepo;
 private final SalaryStructureRepository salaryRepo;

 public PayrollController(
     PayrollRepository payrollRepo,
     EmployeeRepository employeeRepo,
     SalaryStructureRepository salaryRepo
 ) {
  this.payrollRepo = payrollRepo;
  this.employeeRepo = employeeRepo;
  this.salaryRepo = salaryRepo;
 }

 @GetMapping
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
 public java.util.List<Map<String, Object>> getAllPayroll() {
  Map<Long, Employee> emps = new HashMap<>();
  for (Employee e : employeeRepo.findAll()) {
   emps.put(e.getId(), e);
  }

  java.util.List<Map<String, Object>> result = new ArrayList<>();
  for (Payroll p : payrollRepo.findAll()) {
   Employee emp = emps.get(p.getEmployeeId());
   Map<String, Object> map = new LinkedHashMap<>();
   map.put("id", p.getId());
   map.put("employeeId", p.getEmployeeId());
   map.put("employeeCode", emp != null ? emp.getEmployeeCode() : "EMP-" + p.getEmployeeId());
   map.put("employeeName", emp != null ? emp.getName() : "Employee #" + p.getEmployeeId());
   map.put("designation", emp != null ? emp.getDesignation() : "Staff");
   map.put("month", p.getMonth());
   map.put("year", p.getYear());
   map.put("grossPay", p.getGrossPay());
   map.put("grossSalary", p.getGrossPay());
   map.put("pf", p.getPf());
   map.put("tds", p.getTds());
   map.put("otherDeductions", p.getOtherDeductions());
   map.put("totalDeductions", p.getTotalDeductions());
   map.put("netPay", p.getNetPay());
   map.put("status", p.getStatus());
   result.add(map);
  }
  return result;
 }

 @GetMapping("/{employeeId}")
 public java.util.List<Payroll> getPayrollHistory(@PathVariable Long employeeId) {
  return payrollRepo.findByEmployeeIdOrderByYearDescMonthDesc(employeeId);
 }

 @PostMapping("/run")
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
 public ResponseEntity<?> runPayroll(@RequestBody Map<String, Object> body) {
  int month = ((Number) body.get("month")).intValue();
  int year = ((Number) body.get("year")).intValue();

  java.util.List<Employee> activeEmployees = employeeRepo.findAll().stream()
      .filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus()))
      .toList();

  int processedCount = 0;
  int skippedCount = 0;
  List<String> skippedNames = new ArrayList<>();

  for (Employee emp : activeEmployees) {
   Optional<SalaryStructure> opt = salaryRepo.findByEmployeeId(emp.getId());
   SalaryStructure structure;
   if (opt.isPresent()) {
    structure = opt.get();
   } else if (body.containsKey("defaultBasic") && body.get("defaultBasic") != null) {
    // User explicitly provided defaults in the payroll run modal
    BigDecimal defBasic = new BigDecimal(String.valueOf(body.get("defaultBasic")));
    BigDecimal defHra = body.containsKey("defaultHra") ? new BigDecimal(String.valueOf(body.get("defaultHra"))) : defBasic.multiply(new BigDecimal("0.40"));
    BigDecimal defOther = body.containsKey("defaultAllowances") ? new BigDecimal(String.valueOf(body.get("defaultAllowances"))) : BigDecimal.ZERO;
    BigDecimal defDeductions = body.containsKey("defaultDeductions") ? new BigDecimal(String.valueOf(body.get("defaultDeductions"))) : defBasic.multiply(new BigDecimal("0.12"));

    SalaryStructure ns = new SalaryStructure();
    ns.setEmployeeId(emp.getId());
    ns.setBasic(defBasic);
    ns.setHra(defHra);
    ns.setOtherAllowances(defOther);
    ns.setDeductions(defDeductions);
    structure = salaryRepo.save(ns);
   } else {
    skippedCount++;
    skippedNames.add(emp.getName());
    continue;
   }

   BigDecimal basic = structure.getBasic() != null ? structure.getBasic() : BigDecimal.ZERO;
   BigDecimal hra = structure.getHra() != null ? structure.getHra() : BigDecimal.ZERO;
   BigDecimal allowances = structure.getOtherAllowances() != null ? structure.getOtherAllowances() : BigDecimal.ZERO;
   BigDecimal monthlyGross = basic.add(hra).add(allowances);
   BigDecimal tds = PayrollCalculator.monthlyTds(monthlyGross);
   BigDecimal otherDeductions = structure.getDeductions() != null ? structure.getDeductions() : BigDecimal.ZERO;

   PayrollCalculator.Result res = PayrollCalculator.calculate(basic, hra, allowances, otherDeductions, tds);

   Payroll p = payrollRepo.findByEmployeeIdAndMonthAndYear(emp.getId(), month, year)
       .orElseGet(() -> {
        Payroll np = new Payroll();
        np.setEmployeeId(emp.getId());
        np.setMonth(month);
        np.setYear(year);
        return np;
       });

   p.setGrossPay(res.gross());
   p.setPf(res.pf());
   p.setTds(res.tds());
   p.setOtherDeductions(res.otherDeduction());
   p.setTotalDeductions(res.totalDeductions());
   p.setNetPay(res.net());
   p.setStatus("PROCESSED");

   payrollRepo.save(p);
   processedCount++;
  }

  String note = skippedCount > 0
      ? " (" + skippedCount + " employees skipped due to missing salary structures: " + String.join(", ", skippedNames) + ")"
      : "";

  return ResponseEntity.ok(Map.of(
      "message", "Payroll executed successfully. " + processedCount + " employee(s) processed" + note,
      "processedEmployees", processedCount,
      "skippedEmployees", skippedCount,
      "month", month,
      "year", year
  ));
 }

 @GetMapping("/{employeeId}/payslip")
 public ResponseEntity<byte[]> getPayslip(
     @PathVariable Long employeeId,
     @RequestParam int month,
     @RequestParam int year
 ) {
  Employee emp = employeeRepo.findById(employeeId)
      .orElseThrow(() -> new NoSuchElementException("Employee not found with ID " + employeeId));

  Payroll p = payrollRepo.findByEmployeeIdAndMonthAndYear(employeeId, month, year)
      .orElseGet(() -> {
       SalaryStructure ss = salaryRepo.findByEmployeeId(employeeId)
           .orElseThrow(() -> new NoSuchElementException("No salary structure configured for employee " + emp.getName() + " (ID " + employeeId + "). Please configure salary structure first."));

       BigDecimal basic = ss.getBasic() != null ? ss.getBasic() : BigDecimal.ZERO;
       BigDecimal hra = ss.getHra() != null ? ss.getHra() : BigDecimal.ZERO;
       BigDecimal allowances = ss.getOtherAllowances() != null ? ss.getOtherAllowances() : BigDecimal.ZERO;
       BigDecimal monthlyGross = basic.add(hra).add(allowances);
       BigDecimal tds = PayrollCalculator.monthlyTds(monthlyGross);
       BigDecimal deductions = ss.getDeductions() != null ? ss.getDeductions() : BigDecimal.ZERO;

       PayrollCalculator.Result r = PayrollCalculator.calculate(basic, hra, allowances, deductions, tds);
       Payroll np = new Payroll();
       np.setEmployeeId(employeeId);
       np.setMonth(month);
       np.setYear(year);
       np.setGrossPay(r.gross());
       np.setPf(r.pf());
       np.setTds(r.tds());
       np.setOtherDeductions(r.otherDeduction());
       np.setTotalDeductions(r.totalDeductions());
       np.setNetPay(r.net());
       np.setStatus("PROCESSED");
       return payrollRepo.save(np);
      });

  try {
   ByteArrayOutputStream out = new ByteArrayOutputStream();
   Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
   PdfWriter.getInstance(doc, out);
   doc.open();

   Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(16, 43, 78));
   Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(40, 117, 199));
   Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
   Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);

   Paragraph title = new Paragraph("HRGenius Enterprise - Payslip", titleFont);
   title.setAlignment(Element.ALIGN_CENTER);
   doc.add(title);

   String monthName = Month.of(Math.max(1, Math.min(12, month))).name();
   Paragraph period = new Paragraph("Pay Period: " + monthName + " " + year, regularFont);
   period.setAlignment(Element.ALIGN_CENTER);
   period.setSpacingAfter(20);
   doc.add(period);

   // Employee Info Table
   PdfPTable infoTable = new PdfPTable(2);
   infoTable.setWidthPercentage(100);
   infoTable.setSpacingAfter(15);

   addCell(infoTable, "Employee Name:", boldFont);
   addCell(infoTable, emp.getName(), regularFont);
   addCell(infoTable, "Employee Code:", boldFont);
   addCell(infoTable, emp.getEmployeeCode(), regularFont);
   addCell(infoTable, "Designation:", boldFont);
   addCell(infoTable, emp.getDesignation(), regularFont);
   addCell(infoTable, "Email:", boldFont);
   addCell(infoTable, emp.getEmail(), regularFont);

   doc.add(infoTable);

   // Salary Breakdown Table
   PdfPTable payTable = new PdfPTable(4);
   payTable.setWidthPercentage(100);
   payTable.setWidths(new float[]{30, 20, 30, 20});
   payTable.setSpacingAfter(20);

   addHeaderCell(payTable, "Earnings", headerFont);
   addHeaderCell(payTable, "Amount (INR)", headerFont);
   addHeaderCell(payTable, "Deductions", headerFont);
   addHeaderCell(payTable, "Amount (INR)", headerFont);

   addCell(payTable, "Gross Earnings", regularFont);
   addCell(payTable, p.getGrossPay().toString(), regularFont);
   addCell(payTable, "Provident Fund (PF)", regularFont);
   addCell(payTable, p.getPf().toString(), regularFont);

   addCell(payTable, "", regularFont);
   addCell(payTable, "", regularFont);
   addCell(payTable, "Income Tax (TDS)", regularFont);
   addCell(payTable, p.getTds().toString(), regularFont);

   addCell(payTable, "", regularFont);
   addCell(payTable, "", regularFont);
   addCell(payTable, "Other Deductions", regularFont);
   addCell(payTable, p.getOtherDeductions().toString(), regularFont);

   addCell(payTable, "Total Gross Pay", boldFont);
   addCell(payTable, p.getGrossPay().toString(), boldFont);
   addCell(payTable, "Total Deductions", boldFont);
   addCell(payTable, p.getTotalDeductions().toString(), boldFont);

   doc.add(payTable);

   // Net Pay Highlight
   Paragraph net = new Paragraph("Net Pay: INR " + p.getNetPay().toString(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(22, 132, 91)));
   net.setAlignment(Element.ALIGN_RIGHT);
   net.setSpacingAfter(30);
   doc.add(net);

   Paragraph footer = new Paragraph("This is a computer-generated payslip and does not require a physical signature.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY));
   footer.setAlignment(Element.ALIGN_CENTER);
   doc.add(footer);

   doc.close();

   byte[] pdfBytes = out.toByteArray();
   return ResponseEntity.ok()
       .contentType(MediaType.APPLICATION_PDF)
       .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"HRGenius-payslip-" + month + "-" + year + ".pdf\"")
       .body(pdfBytes);
  } catch (NoSuchElementException nse) {
   throw nse;
  } catch (Exception ex) {
   throw new RuntimeException("Error generating PDF payslip: " + ex.getMessage(), ex);
  }
 }

 private void addCell(PdfPTable table, String text, Font font) {
  PdfPCell cell = new PdfPCell(new Phrase(text, font));
  cell.setPadding(6);
  cell.setBorderColor(new Color(230, 235, 240));
  table.addCell(cell);
 }

 private void addHeaderCell(PdfPTable table, String text, Font font) {
  PdfPCell cell = new PdfPCell(new Phrase(text, font));
  cell.setPadding(8);
  cell.setBackgroundColor(new Color(245, 248, 252));
  cell.setBorderColor(new Color(220, 228, 236));
  table.addCell(cell);
 }
}
