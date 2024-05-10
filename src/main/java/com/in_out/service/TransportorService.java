package com.in_out.service;


import com.in_out.model.Inscan;
import com.in_out.model.Licensegate;
import com.in_out.model.Transportor;
import com.in_out.repo.TransportorRepository;
import com.in_out.service.InscanService;
import com.in_out.service.LicenseGateService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransportorService {
  @Autowired
  private TransportorRepository transportorRepository;
  
  @Autowired
  private InscanService inscanService;
  
  @Autowired
  private LicenseGateService licenseGateService;
  
  public List<Transportor> addTransportor(List<Integer> transportorData) {
    List<Transportor> transportor = transportorData.stream().map(intValue -> new Transportor()).toList();
    return this.transportorRepository.saveAll(transportor);
  }
  
  public List<Transportor> getAllTransportorDetails() {
    return this.transportorRepository.findAll();
  }
  
  public Optional<Transportor> getTransportorById(Long id) {
    return this.transportorRepository.findById(id);
  }
  
  public Transportor addTransportor(Transportor transportor) {
    Transportor existingTransportor = this.transportorRepository.findByAadharNumber(transportor.getAadharNumber());
    if (existingTransportor != null)
      throw new IllegalArgumentException("An Transportor with the same Aadhar number already exists"); 
    return (Transportor)this.transportorRepository.save(transportor);
  }
  
  public Transportor updateTransportor(Long id, Transportor updatedTransportor) {
    Optional<Transportor> existingTransportor = this.transportorRepository.findById(id);
    if (existingTransportor.isPresent()) {
      Transportor transportorToUpdate = existingTransportor.get();
      Transportor existingTransportorWithNewAadhar = this.transportorRepository.findByAadharNumber(updatedTransportor.getAadharNumber());
      if (existingTransportorWithNewAadhar != null && !existingTransportorWithNewAadhar.getId().equals(id))
        throw new IllegalArgumentException("An Transportor with the same Aadhar number already exists"); 
      transportorToUpdate.setAadharNumber(updatedTransportor.getAadharNumber());
      transportorToUpdate.setFullName(updatedTransportor.getFullName());
      transportorToUpdate.setMobileNumber(updatedTransportor.getMobileNumber());
      transportorToUpdate.setAddress(updatedTransportor.getAddress());
      return (Transportor)this.transportorRepository.save(transportorToUpdate);
    } 
    throw new IllegalArgumentException("Transportor not found");
  }
  
  public Transportor deleteTransportorDetails(Long id) {
    Optional<Transportor> existingTransportor = this.transportorRepository.findById(id);
    if (existingTransportor.isPresent()) {
      Transportor transportorToUpdate = existingTransportor.get();
      transportorToUpdate.setAadharNumber(null);
      transportorToUpdate.setFullName(null);
      transportorToUpdate.setMobileNumber(null);
      transportorToUpdate.setAddress(null);
      return (Transportor)this.transportorRepository.save(transportorToUpdate);
    } 
    throw new IllegalArgumentException("Transportor not found");
  }
  
  public String processAndSaveDetails(Long transportorId) {
    if (this.transportorRepository == null || this.inscanService == null)
      throw new IllegalStateException("Transportor repository or Inscan service not initialized"); 
    Optional<Transportor> optionalTransportor = this.transportorRepository.findById(transportorId);
    if (optionalTransportor.isPresent()) {
      Transportor transportor = optionalTransportor.get();
      StringBuilder detailsBuilder = (new StringBuilder("TR/HPNSK/")).append(transportor.getId());
      String department = "Driver";
      String sub_department = "TR";
      String details = "TR/HPNSK/";
      Long ofcid = transportor.getId();
      String name = transportor.getFullName();
      String aadharNumber = transportor.getAadharNumber();
      String mobile = transportor.getMobileNumber();
      String address = transportor.getAddress();
      Inscan inscan = this.inscanService.findByAadhar(aadharNumber);
      if (inscan == null) {
        String str = "Y";
        this.inscanService.saveDetailsToInscan(details, name, aadharNumber, mobile, address, ofcid, department, sub_department, str);
        return "In";
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
    throw new IllegalArgumentException("Transportor not found");
  }
  
  public Transportor getDetailsByAadharNumber(String aadharNumber) {
    return this.transportorRepository.findByAadharNumber(aadharNumber);
  }
  
  public String getFullName(Long entityId) {
    Transportor transportor = this.transportorRepository.findById(entityId).orElse(null);
    if (transportor != null) {
      String fullName = transportor.getFullName();
      return fullName;
    } 
    return "Unknown Transportor";
  }
  
  public String processAndSaveLicenseGateDetails(Long transportorId) {
    if (this.transportorRepository == null || this.licenseGateService == null)
      throw new IllegalStateException("Transportor repository or LicenseGateService service not initialized"); 
    Optional<Transportor> optionalTransportor = this.transportorRepository.findById(transportorId);
    if (optionalTransportor.isPresent()) {
      Transportor transportor = optionalTransportor.get();
      StringBuilder detailsBuilder = (new StringBuilder("TR/HPNSK/")).append(transportor.getId());
      String department = "Operation";
      String sub_department = "TR";
      String details = detailsBuilder.toString();
      Long ofcId = transportor.getId();
      String name = transportor.getFullName();
      String aadharNumber = transportor.getAadharNumber();
      String mobile = transportor.getMobileNumber();
      String address = transportor.getAddress();
      Licensegate licenseGate = this.licenseGateService.findByAadhar(aadharNumber);
      if (licenseGate == null) {
        this.licenseGateService.saveDetailsToLicenseGate(details, name, aadharNumber, mobile, address, transportorId, department, sub_department);
        return "In";
      } 
      if (licenseGate.getExitDateTime() != null) {
        this.licenseGateService.saveDetailsToLicenseGate(details, name, aadharNumber, mobile, address, transportorId, department, sub_department);
        return "Scan In";
      } 
      this.licenseGateService.updateDetailsToLicenseGate(licenseGate);
      return "Scan Out";
    } 
    throw new IllegalArgumentException("Transportor not found");
  }
}
