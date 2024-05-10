package com.in_out.controller;


import com.in_out.model.Inscan;
import com.in_out.model.License;
import com.in_out.model.User;
import com.in_out.repo.UserRepository;
import com.in_out.service.InscanService;
import com.in_out.service.LicenseGateService;
import com.in_out.service.OfficerService;
import java.security.Principal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReportController {
  @Autowired
  private OfficerService officerService;
  
  @Autowired
  private LicenseGateService LicensegateService;
  
  @Autowired
  private UserRepository userRepository;
  
  @Autowired
  private InscanService inscanService;
  
  private void addUsernameAndRoleToModel(Model model, Principal principal) {
    String username = principal.getName();
    User user = this.userRepository.findByUserName(username);
    if (user != null) {
      String role = user.getRole();
      model.addAttribute("username", username);
      model.addAttribute("userRole", role);
    } 
  }
  
  private void addLicenseInfoToModel(Model model) {
    User admin = (User)this.userRepository.getReferenceById(Integer.valueOf(1));
    License license = admin.getLicense();
    long remainingdays = ChronoUnit.DAYS.between(LocalDate.now(), license.getExpirydate());
    if (remainingdays == 0L) {
      model.addAttribute("remainingdays", Boolean.valueOf(false));
    } else if (remainingdays == 1L) {
      model.addAttribute("onedayremain", Boolean.valueOf(true));
    } else {
      model.addAttribute("remainingdays", Long.valueOf(remainingdays));
    } 
  }
  
  @GetMapping({"/report"})
  public String report(Model model) {
    List<String> officerDetails = this.officerService.getAllOfficerFullNames();
    model.addAttribute("OfficerDetails", officerDetails);
    List<Inscan> inscandetails = this.inscanService.getAllInscanDetails();
    model.addAttribute("reportdetails", inscandetails);
    addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
    addLicenseInfoToModel(model);
    return "report";
  }
  
  @PostMapping({"/generatereport"})
  public String generateReport(@RequestParam("from_date") String fromDate, @RequestParam("to_date") String toDate, @RequestParam("department") String department, @RequestParam("sub_department") String subDepartment, @RequestParam("name") String name, @RequestParam("gate") String gate, Model model) {
    System.out.println(fromDate + ", " + fromDate + ", " + toDate + ", " + department + ", " + subDepartment);
    if ("maingate".equalsIgnoreCase(gate)) {
      List<?> reportData = this.inscanService.generateReportDataMainGate(fromDate, toDate, department, subDepartment, name);
      model.addAttribute("reportData", reportData);
    } else if ("licensegate".equalsIgnoreCase(gate)) {
      List<?> reportData = this.LicensegateService.generateReportDataLicenseGate(fromDate, toDate, department, subDepartment, name);
      model.addAttribute("reportData", reportData);
    } else if ("all".equalsIgnoreCase(gate)) {
      List<?> reportData = this.inscanService.generateReportDataMainGate(fromDate, toDate, department, subDepartment, name);
      model.addAttribute("reportData", reportData);
    } else {
      throw new IllegalArgumentException("Invalid gate value: " + gate);
    } 
    addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
    addLicenseInfoToModel(model);
    List<String> officerDetails = this.officerService.getAllOfficerFullNames();
    model.addAttribute("OfficerDetails", officerDetails);
    return "report";
  }
}
