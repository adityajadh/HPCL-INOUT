package com.in_out.service;


import com.in_out.model.Inscan;
import com.in_out.repo.InscanRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InscanService {
  @Autowired
  private InscanRepository inscanRepository;
  
  public void saveDetailsToInscan(String details, String name, String aadharNumber, String mobile, String address, Long ofcid, String department, String sub_department, String mainGateStatus) {
    Inscan inscan = new Inscan();
    inscan.setDetails(details);
    inscan.setOfcid(ofcid);
    inscan.setName(name);
    inscan.setAadharNumber(aadharNumber);
    inscan.setMobile(mobile);
    inscan.setAddress(address);
    inscan.setDepartment(department);
    inscan.setSub_department(sub_department);
    inscan.setMainGateSatus(mainGateStatus);
    this.inscanRepository.save(inscan);
  }
  
  public List<Inscan> getAllInscanDetails() {
    return this.inscanRepository.findAll();
  }
  
  public List<Inscan> getAllInscanDetailsForCurrentDay() {
    LocalDateTime currentDateTime = LocalDateTime.now();
    LocalDateTime startOfDay = currentDateTime.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1L);
    Date startDate = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    Date endDate = Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
    return this.inscanRepository.findByEntryDateTimeBetweenOrderByDetails(startDate, endDate);
  }
  
  public Long getCountOfInscanDetailsForCurrentDay() {
    LocalDateTime currentDateTime = LocalDateTime.now();
    LocalDateTime startOfDay = currentDateTime.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1L);
    Date startDate = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    Date endDate = Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
    return this.inscanRepository.countByEntryDateTimeBetween(startDate, endDate);
  }
  
  public List<Inscan> findByEntryDateTimeBetweenOrderByDetailsForOperation() {
    LocalDateTime currentDateTime = LocalDateTime.now();
    LocalDateTime startOfDay = currentDateTime.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1L);
    Date startDate = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    Date endDate = Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
    return this.inscanRepository.findByEntryDateTimeBetweenOrderByDetailsForOperation(startDate, endDate);
  }
  
  public Long countByEntryDateTimeBetweenForOperation() {
    LocalDateTime currentDateTime = LocalDateTime.now();
    Date startDate = Date.from(currentDateTime.toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    Date endDate = Date.from(currentDateTime.plusDays(1L).toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    return this.inscanRepository.countByEntryDateTimeBetweenForOperation(startDate, endDate);
  }
  
  public List<Inscan> findByEntryDateTimeBetweenOrderByDetailsForProject() {
    LocalDateTime currentDateTime = LocalDateTime.now();
    LocalDateTime startOfDay = currentDateTime.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1L);
    Date startDate = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    Date endDate = Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
    return this.inscanRepository.findByEntryDateTimeBetweenOrderByDetailsForProject(startDate, endDate);
  }
  
  public Long countByEntryDateTimeBetweenForProject() {
    LocalDateTime currentDateTime = LocalDateTime.now();
    Date startDate = Date.from(currentDateTime.toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    Date endDate = Date.from(currentDateTime.plusDays(1L).toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    return this.inscanRepository.countByEntryDateTimeBetweenForProject(startDate, endDate);
  }
  
  public List<Inscan> findByEntryDateTimeBetweenOrderByDetailsForVisitor() {
    LocalDateTime currentDateTime = LocalDateTime.now();
    LocalDateTime startOfDay = currentDateTime.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1L);
    Date startDate = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    Date endDate = Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
    return this.inscanRepository.findByEntryDateTimeBetweenOrderByDetailsForVisitor(startDate, endDate);
  }
  
  public Long countByEntryDateTimeBetweenForVisitor() {
    LocalDateTime currentDateTime = LocalDateTime.now();
    Date startDate = Date.from(currentDateTime.toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    Date endDate = Date.from(currentDateTime.plusDays(1L).toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    return this.inscanRepository.countByEntryDateTimeBetweenForVisitor(startDate, endDate);
  }
  
  public Inscan findByAadhar(String aadharNumber) {
    return this.inscanRepository.findByAadharNumber(aadharNumber);
  }
  
  public void updateDetailsToInscan(Inscan inscan) {
    inscan.setExitDateTime(new Date());
    inscan.setMainGateSatus("Y");
    this.inscanRepository.save(inscan);
  }
  
  public Inscan findByAadharExitDateTime(String aadharNumber, Date exitDateTime) {
    return this.inscanRepository.findByAadharNumber(aadharNumber);
  }
  
  public List<Inscan> generateReportDataMainGate(String fromDate, String toDate, String department, String subDepartment, String name) {
    LocalDateTime startDateTime = LocalDateTime.parse(fromDate + "T00:00");
    LocalDateTime endDateTime = LocalDateTime.parse(toDate + "T23:59");
    Date startDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
    Date endDate = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());
    List<String> dsf = new LinkedList<>();
    if (startDate != null)
      dsf.add(" AND i._date_time >= '" + fromDate + "'"); 
    if (endDate != null)
      dsf.add(" AND i.exit_date_time <= '" + toDate + "'"); 
    if (department != null && !department.isEmpty())
      dsf.add(" AND i.department = '" + department + "'"); 
    if (subDepartment != null && !subDepartment.isEmpty())
      dsf.add(" AND i.sub_department = '" + subDepartment + "'"); 
    String finalQuery = "SELECT * FROM inscan i WHERE i.aadhar_number IS NOT NULL ";
    for (int i = 0; i < dsf.size(); i++)
      finalQuery = finalQuery + finalQuery; 
    if (!"All".equalsIgnoreCase(name))
      finalQuery = finalQuery + " AND LOWER(i.name) LIKE LOWER(CONCAT('%', '" + finalQuery + "', '%'))"; 
    System.out.println("QUERY " + finalQuery);
    if ("All".equals(subDepartment))
      return this.inscanRepository.findByEntryDateTimeBetweenAndDepartmentOrderByDetails(startDate, endDate, department); 
    return this.inscanRepository.findByEntryDateTimeBetweenAndDepartmentAndSubDepartmentOrderByDetails(startDate, endDate, department, subDepartment);
  }
  
  public String getMainGateSatus(Long entityId, String entityType) {
    return this.inscanRepository.findBySubDepartmentAndExitDateTimeAndMainGateStatus(entityId, entityType);
  }
  
  public void updateNullExitDateTime() {
    List<Inscan> nullExitDateTimeList = this.inscanRepository.findByExitDateTimeIsNull();
    for (Inscan inscan : nullExitDateTimeList)
      inscan.setExitDateTime(new Date()); 
    this.inscanRepository.saveAll(nullExitDateTimeList);
  }
}
