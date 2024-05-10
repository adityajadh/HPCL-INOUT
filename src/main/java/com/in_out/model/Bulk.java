package com.in_out.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Bulk")
public class Bulk {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(unique = true, name = "aadharNumber", length = 12)
  private String aadharNumber;
  
  private String fullName;
  
  private String mobileNumber;
  
  private String address;
  
  private String firmName;
  
  private String truckNumber;
  
  private String bulk;
  
  private String qr;
  
  public Bulk() {
    this.bulk = "Driver-Bulk";
    this.qr = "BK/HPNSK";
  }
  
  public Bulk(Long id, String aadharNumber, String fullName, String mobileNumber, String address, String firmName, String truckNumber, String bulk, String qr) {
    this.id = id;
    this.aadharNumber = aadharNumber;
    this.fullName = fullName;
    this.mobileNumber = mobileNumber;
    this.address = address;
    this.firmName = firmName;
    this.truckNumber = truckNumber;
    this.bulk = bulk;
    this.qr = qr;
  }
  
  public Long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public String getAadharNumber() {
    return this.aadharNumber;
  }
  
  public void setAadharNumber(String aadharNumber) {
    this.aadharNumber = aadharNumber;
  }
  
  public String getFullName() {
    return this.fullName;
  }
  
  public void setFullName(String fullName) {
    this.fullName = fullName;
  }
  
  public String getMobileNumber() {
    return this.mobileNumber;
  }
  
  public void setMobileNumber(String mobileNumber) {
    this.mobileNumber = mobileNumber;
  }
  
  public String getAddress() {
    return this.address;
  }
  
  public void setAddress(String address) {
    this.address = address;
  }
  
  public String getFirmName() {
    return this.firmName;
  }
  
  public void setFirmName(String firmName) {
    this.firmName = firmName;
  }
  
  public String getTruckNumber() {
    return this.truckNumber;
  }
  
  public void setTruckNumber(String truckNumber) {
    this.truckNumber = truckNumber;
  }
  
  public String getBulk() {
    return this.bulk;
  }
  
  public void setBulk(String bulk) {
    this.bulk = bulk;
  }
  
  public String getQr() {
    return this.qr;
  }
  
  public void setQr(String qr) {
    this.qr = qr;
  }
}