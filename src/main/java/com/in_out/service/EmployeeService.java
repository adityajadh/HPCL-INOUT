package com.in_out.service;


import com.in_out.model.Employee;
import com.in_out.model.Inscan;
import com.in_out.model.Licensegate;
import com.in_out.repo.EmployeeRepository;
import com.in_out.service.InscanService;
import com.in_out.service.LicenseGateService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {
  @Autowired
  private EmployeeRepository employeeRepository;
  
  @Autowired
  private InscanService inscanService;
  
  @Autowired
  private LicenseGateService licenseGateService;
  
  public List<Employee> addEmployee(List<Integer> employeeData) {
    List<Employee> employee = employeeData.stream().map(intValue -> new Employee()).toList();
    return this.employeeRepository.saveAll(employee);
  }
  
  public List<Employee> getAllEmployeeDetails() {
    return this.employeeRepository.findAll();
  }
  
  public Optional<Employee> getEmployeeById(Long id) {
    return this.employeeRepository.findById(id);
  }
  
  public Employee addEmployee(Employee employee) {
    Employee existingEmployee = this.employeeRepository.findByAadharNumber(employee.getAadharNumber());
    if (existingEmployee != null)
      throw new IllegalArgumentException("An Transportor with the same Aadhar number already exists"); 
    return (Employee)this.employeeRepository.save(employee);
  }
  
  public Employee updateEmployee(Long id, Employee updatedEmployee) {
    Optional<Employee> existingEmployee = this.employeeRepository.findById(id);
    if (existingEmployee.isPresent()) {
      Employee employeeToUpdate = existingEmployee.get();
      Employee existingEmployeeWithNewAadhar = this.employeeRepository.findByAadharNumber(updatedEmployee.getAadharNumber());
      if (existingEmployeeWithNewAadhar != null && !existingEmployeeWithNewAadhar.getId().equals(id))
        throw new IllegalArgumentException("An Employee with the same Aadhar number already exists"); 
      employeeToUpdate.setAadharNumber(updatedEmployee.getAadharNumber());
      employeeToUpdate.setFullName(updatedEmployee.getFullName());
      employeeToUpdate.setMobileNumber(updatedEmployee.getMobileNumber());
      employeeToUpdate.setAddress(updatedEmployee.getAddress());
      return (Employee)this.employeeRepository.save(employeeToUpdate);
    } 
    throw new IllegalArgumentException("Employee not found");
  }
  
  public Employee deleteEmployeeDetails(Long id) {
    Optional<Employee> existingEmployee = this.employeeRepository.findById(id);
    if (existingEmployee.isPresent()) {
      Employee employeeToUpdate = existingEmployee.get();
      employeeToUpdate.setAadharNumber(null);
      employeeToUpdate.setFullName(null);
      employeeToUpdate.setMobileNumber(null);
      employeeToUpdate.setAddress(null);
      return (Employee)this.employeeRepository.save(employeeToUpdate);
    } 
    throw new IllegalArgumentException("Employee not found");
  }
  
  public String processAndSaveDetails(Long employeeId) {
    if (this.employeeRepository == null || this.inscanService == null)
      throw new IllegalStateException("Employee repository or Inscan service not initialized"); 
    Optional<Employee> optionalEmployee = this.employeeRepository.findById(employeeId);
    if (optionalEmployee.isPresent()) {
      Employee employee = optionalEmployee.get();
      StringBuilder detailsBuilder = (new StringBuilder("EMP/HPNSK/")).append(employee.getId());
      String department = "Operation";
      String sub_department = "EMP";
      String details = "EMP/HPNSK/";
      Long ofcid = employee.getId();
      String name = employee.getFullName();
      String aadharNumber = employee.getAadharNumber();
      String mobile = employee.getMobileNumber();
      String address = employee.getAddress();
      Inscan inscan = this.inscanService.findByAadhar(aadharNumber);
      if (inscan == null) {
        String str = "Y";
        this.inscanService.saveDetailsToInscan(details, name, aadharNumber, mobile, address, ofcid, department, sub_department, str);
        return "IN";
      } 
      String mainGateStatus = "N";
      Licensegate licensegate = this.licenseGateService.findByAadhar(aadharNumber);
      boolean isLicenseGateIn = false;
      if (licensegate == null || (licensegate != null && licensegate.getExitDateTime() != null))
        isLicenseGateIn = true; 
      if (!isLicenseGateIn)
        return "Please exit from License gate"; 
      if (inscan.getExitDateTime() != null) {
        this.inscanService.saveDetailsToInscan(details, name, aadharNumber, mobile, address, ofcid, department, sub_department, mainGateStatus);
        return "Scan In";
      } 
      this.inscanService.updateDetailsToInscan(inscan);
      return "Scan Out";
    } 
    throw new IllegalArgumentException("Employee not found");
  }
  
  public Employee getDetailsByAadharNumber(String aadharNumber) {
    return this.employeeRepository.findByAadharNumber(aadharNumber);
  }
  
  public String getFullName(Long entityId) {
    Employee employee = this.employeeRepository.findById(entityId).orElse(null);
    if (employee != null) {
      String fullName = employee.getFullName();
      return fullName;
    } 
    return "Unknown Employee";
  }
  
  public String processAndSaveLicenseGateDetails(Long employeeId) {
    if (this.employeeRepository == null || this.licenseGateService == null)
      throw new IllegalStateException("Employee repository or LicenseGateService service not initialized"); 
    Optional<Employee> optionalEmployee = this.employeeRepository.findById(employeeId);
    if (optionalEmployee.isPresent()) {
      Employee employee = optionalEmployee.get();
      StringBuilder detailsBuilder = (new StringBuilder("EMP/HPNSK/")).append(employee.getId());
      String department = "Operation";
      String sub_department = "EMP";
      String details = detailsBuilder.toString();
      Long ofcid = employee.getId();
      String name = employee.getFullName();
      String aadharNumber = employee.getAadharNumber();
      String mobile = employee.getMobileNumber();
      String address = employee.getAddress();
      Licensegate licensegate = this.licenseGateService.findByAadhar(aadharNumber);
      if (licensegate == null) {
        this.licenseGateService.saveDetailsToLicenseGate(details, name, aadharNumber, mobile, address, ofcid, department, sub_department);
        return "IN";
      } 
      if (licensegate.getExitDateTime() != null) {
        this.licenseGateService.saveDetailsToLicenseGate(details, name, aadharNumber, mobile, address, ofcid, department, sub_department);
        return "Scan IN";
      } 
      this.licenseGateService.updateDetailsToLicenseGate(licensegate);
      return "Scan Out";
    } 
    throw new IllegalArgumentException("Employee not found");
  }
}
