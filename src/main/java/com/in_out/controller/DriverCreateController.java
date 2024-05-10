package com.in_out.controller;


import com.in_out.service.AmcService;
import com.in_out.service.BulkService;
import com.in_out.service.PackedService;
import com.in_out.service.TransportorService;
import com.in_out.service.VisitorService;
import com.in_out.service.WorkmanService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DriverCreateController {
  @Autowired
  private PackedService packedService;
  
  @Autowired
  private BulkService bulkService;
  
  @Autowired
  private TransportorService transportorService;
  
  @Autowired
  private WorkmanService workmanService;
  
  @Autowired
  private AmcService amcService;
  
  @Autowired
  private VisitorService visitorService;
  
  @PostMapping({"/submitPacked"})
  public String submitPacked(@RequestBody List<Integer> packedData) {
    if (packedData != null && !packedData.isEmpty()) {
      this.packedService.addPacked(packedData);
      return "200";
    } 
    return "ERROR";
  }
  
  @PostMapping({"/submitBulk"})
  public String submitBulk(@RequestBody List<Integer> bulkData) {
    if (bulkData != null && !bulkData.isEmpty()) {
      this.bulkService.addBulk(bulkData);
      return "200";
    } 
    return "ERROR";
  }
  
  @PostMapping({"/submitTransportor"})
  public String submitTransportor(@RequestBody List<Integer> transportorData) {
    if (transportorData != null && !transportorData.isEmpty()) {
      this.transportorService.addTransportor(transportorData);
      return "200";
    } 
    return "ERROR";
  }
  
  @PostMapping({"/submitWorkman"})
  public String submitWorkman(@RequestBody List<Integer> workmanData) {
    if (workmanData != null && !workmanData.isEmpty()) {
      this.workmanService.addWorkman(workmanData);
      return "200";
    } 
    return "ERROR";
  }
  
  @PostMapping({"/submitAmc"})
  public String submitAmc(@RequestBody List<Integer> amcData) {
    if (amcData != null && !amcData.isEmpty()) {
      this.amcService.addAmc(amcData);
      return "200";
    } 
    return "ERROR";
  }
  
  @PostMapping({"/submitVisitor"})
  public String submitVisitor(@RequestBody List<Integer> visitorData) {
    if (visitorData != null && !visitorData.isEmpty()) {
      this.visitorService.addVisitor(visitorData);
      return "200";
    } 
    return "ERROR";
  }
}
