package com.in_out.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Transportor")
public class Transportor {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(unique = true, name = "aadharNumber", length = 12)
  private String aadharNumber;
  
  private String fullName;
  
  private String mobileNumber;
  
  private String address;
  
  private String trans;
  
  public Transportor() {
    this.trans = "Driver-Transporter";
  }
  
  public Transportor(Long id, String aadharNumber, String fullName, String mobileNumber, String address, String trans) {
    this.id = id;
    this.aadharNumber = aadharNumber;
    this.fullName = fullName;
    this.mobileNumber = mobileNumber;
    this.address = address;
    this.trans = trans;
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
  
  public String getTrans() {
    return this.trans;
  }
  
  public void setTrans(String trans) {
    this.trans = trans;
  }
}
