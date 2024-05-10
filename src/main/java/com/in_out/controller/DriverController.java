package com.in_out.controller;


import com.in_out.model.AadharNumberDetails;
import com.in_out.model.Amc;
import com.in_out.model.Bulk;
import com.in_out.model.Inscan;
import com.in_out.model.License;
import com.in_out.model.Packed;
import com.in_out.model.Transportor;
import com.in_out.model.User;
import com.in_out.model.Visitor;
import com.in_out.model.VisitorTokenId;
import com.in_out.model.Workman;
import com.in_out.repo.UserRepository;
import com.in_out.repo.VisitorTokenIdRepository;
import com.in_out.service.AadharNumberDetailsService;
import com.in_out.service.AmcService;
import com.in_out.service.BulkService;
import com.in_out.service.InscanService;
import com.in_out.service.OfficerService;
import com.in_out.service.PackedService;
import com.in_out.service.TransportorService;
import com.in_out.service.VisitorService;
import com.in_out.service.WorkmanService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DriverController {
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
  
  @Autowired
  private AadharNumberDetailsService adharNumberDetailsService;
  
  @Autowired
  private OfficerService officerService;
  
  @Autowired
  private InscanService inscanService;
  
  @Autowired
  private UserRepository userRepository;
  
  @Value("${app.upload-dir}")
  private String uploadDir;
  
  @Autowired
  private VisitorTokenIdRepository visitorTokenRepository;
  
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
  
  @GetMapping({"/packed"})
  public String getPackedDetails(Model model) {
    List<Packed> packedDetails = this.packedService.getAllPackedDetails();
    model.addAttribute("packedDetails", packedDetails);
    List<Inscan> operatorTotalDetails = this.inscanService.getAllInscanDetailsForCurrentDay();
    model.addAttribute("operatorTotalDetails", operatorTotalDetails);
    addLicenseInfoToModel(model);
    addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
    return "packed";
  }
  
  @GetMapping({"/packedDetails"})
  public String packedDetails(@RequestParam("productId") Long productId, @RequestParam("action") String action, Model model) {
    Optional<Packed> packed = this.packedService.getPackedById(productId);
    if (packed.isPresent()) {
      model.addAttribute("selectedProduct", packed.get());
      addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
      addLicenseInfoToModel(model);
      return "packedDetails";
    } 
    return "PackedNotFound";
  }
  
  @PostMapping({"/packedsave"})
  public String packedsave(@ModelAttribute Packed packed, RedirectAttributes redirectAttributes, Model model) {
    try {
      if (packed.getId() == null) {
        Packed createdPacked = this.packedService.addPacked(packed);
        if (createdPacked != null) {
          redirectAttributes.addFlashAttribute("successMessage", "Packed saved successfully.");
        } else {
          redirectAttributes.addFlashAttribute("errorMessage", "An Packed with the same token number already exists.");
        } 
      } else {
        Optional<Packed> packed_old_opt = this.packedService.getPackedById(packed.getId());
        Packed packed_old = null;
        if (packed_old_opt.isPresent())
          packed_old = packed_old_opt.get(); 
        if (packed_old.getAadharNumber() == null || "".equals(packed_old.getAadharNumber())) {
          AadharNumberDetails aadharNumberDetails = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(packed.getAadharNumber());
          if (aadharNumberDetails != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Aadhar Already Exists.");
          } else {
            AadharNumberDetails aadharNumberDetails2 = new AadharNumberDetails();
            aadharNumberDetails2.setAadharNumber(packed.getAadharNumber());
            aadharNumberDetails2.setEntity("Packed " + packed.getId());
            aadharNumberDetails2.setFullName(packed.getFullName());
            aadharNumberDetails2.setMobileNumber(packed.getMobileNumber());
            aadharNumberDetails2.setAddress(packed.getAddress());
            this.adharNumberDetailsService.saveAadharNumberDetails(aadharNumberDetails2);
            Packed updatedPacked = this.packedService.updatePacked(packed.getId(), packed);
            if (updatedPacked != null) {
              redirectAttributes.addFlashAttribute("successMessage", "Packed saved successfully.");
            } else {
              redirectAttributes.addFlashAttribute("errorMessage", "Failed to saved the Packed.");
            } 
          } 
        } else if (!packed.getAadharNumber().equalsIgnoreCase(packed_old.getAadharNumber())) {
          AadharNumberDetails aadharNumberDetails = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(packed.getAadharNumber());
          if (aadharNumberDetails != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Aadhar already used by someone else..Aadhar Already Exists.");
          } else {
            AadharNumberDetails aadharNumberDetails_delete = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(packed_old.getAadharNumber());
            this.adharNumberDetailsService.deleteAadharNumberDetails(aadharNumberDetails_delete.getId());
            AadharNumberDetails aadharNumberDetails2 = new AadharNumberDetails();
            aadharNumberDetails2.setAadharNumber(packed.getAadharNumber());
            aadharNumberDetails2.setEntity("Packed " + packed.getId());
            aadharNumberDetails2.setFullName(packed.getFullName());
            aadharNumberDetails2.setMobileNumber(packed.getMobileNumber());
            aadharNumberDetails2.setAddress(packed.getAddress());
            this.adharNumberDetailsService.saveAadharNumberDetails(aadharNumberDetails2);
            Packed updatedPacked = this.packedService.updatePacked(packed.getId(), packed);
            if (updatedPacked != null) {
              redirectAttributes.addFlashAttribute("successMessage", "Packed saved successfully.");
            } else {
              redirectAttributes.addFlashAttribute("errorMessage", "Failed to saved the Packed.");
            } 
          } 
        } else {
          Packed updatedPacked = this.packedService.updatePacked(packed.getId(), packed);
          if (updatedPacked != null) {
            redirectAttributes.addFlashAttribute("successMessage", "Packed updated successfully.");
          } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update the Packed.");
          } 
        } 
      } 
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
    } 
    addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
    addLicenseInfoToModel(model);
    return "redirect:/packed";
  }
  
  @GetMapping({"/selectedPacked"})
  public String packedview(@RequestParam("productId") long productId, Model model) {
    Optional<Packed> packed = this.packedService.getPackedById(Long.valueOf(productId));
    if (packed.isPresent())
      model.addAttribute("selectedProduct", packed.get()); 
    addLicenseInfoToModel(model);
    addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
    return "packed_id";
  }
  
  @GetMapping({"/delete-Packed-details"})
  public String deletePackedDetails(@RequestParam("productId") Long productId, RedirectAttributes redirectAttributes, Model model) {
    try {
      Optional<Packed> packedcopy = this.packedService.getPackedById(productId);
      String aadharno = null;
      if (packedcopy.isPresent())
        aadharno = ((Packed)packedcopy.get()).getAadharNumber(); 
      Packed updatepacked = this.packedService.deletePackedDetails(productId);
      if (updatepacked != null) {
        System.out.println("aadhar to delte " + aadharno);
        AadharNumberDetails aadharNumberDetails_delete = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(aadharno);
        this.adharNumberDetailsService.deleteAadharNumberDetails(aadharNumberDetails_delete.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Packed details deleted successfully.");
      } else {
        redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete Packed details.");
      } 
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
    } 
    addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
    addLicenseInfoToModel(model);
    return "redirect:/packed";
  }
  
  @GetMapping({"/bulk"})
  public String getBulkDetails(Model model) {
    List<Bulk> bulkDetails = this.bulkService.getAllBulkDetails();
    model.addAttribute("bulkDetails", bulkDetails);
    List<Inscan> operatorTotalDetails = this.inscanService.getAllInscanDetailsForCurrentDay();
    model.addAttribute("operatorTotalDetails", operatorTotalDetails);
    addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
    addLicenseInfoToModel(model);
    return "bulk";
  }
  
  @GetMapping({"/bulkDetails"})
  public String bulkDetails(@RequestParam("productId") Long productId, @RequestParam("action") String action, Model model) {
    Optional<Bulk> bulk = this.bulkService.getBulkById(productId);
    if (bulk.isPresent()) {
      model.addAttribute("selectedProduct", bulk.get());
      addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
      addLicenseInfoToModel(model);
      return "bulkDetails";
    } 
    return "Bulk NotFound";
  }
  
  @PostMapping({"/bulksave"})
  public String bulksave(@ModelAttribute Bulk bulk, RedirectAttributes redirectAttributes, Model model) {
    try {
      if (bulk.getId() == null) {
        Bulk createdBulk = this.bulkService.addBulk(bulk);
        if (createdBulk != null) {
          redirectAttributes.addFlashAttribute("successMessage", "Bulk saved successfully.");
        } else {
          redirectAttributes.addFlashAttribute("errorMessage", "An Bulk with the same token number already exists.");
        } 
      } else {
        Optional<Bulk> bulk_old_opt = this.bulkService.getBulkById(bulk.getId());
        Bulk bulk_old = null;
        if (bulk_old_opt.isPresent())
          bulk_old = bulk_old_opt.get(); 
        if (bulk_old.getAadharNumber() == null || "".equals(bulk_old.getAadharNumber())) {
          AadharNumberDetails aadharNumberDetails = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(bulk.getAadharNumber());
          if (aadharNumberDetails != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Aadhar Already Exists.");
          } else {
            AadharNumberDetails aadharNumberDetails2 = new AadharNumberDetails();
            aadharNumberDetails2.setAadharNumber(bulk.getAadharNumber());
            aadharNumberDetails2.setEntity("Bulk " + bulk.getId());
            aadharNumberDetails2.setFullName(bulk.getFullName());
            aadharNumberDetails2.setMobileNumber(bulk.getMobileNumber());
            aadharNumberDetails2.setAddress(bulk.getAddress());
            this.adharNumberDetailsService.saveAadharNumberDetails(aadharNumberDetails2);
            Bulk updatedBulk = this.bulkService.updateBulk(bulk.getId(), bulk);
            if (updatedBulk != null) {
              redirectAttributes.addFlashAttribute("successMessage", "Bulk saved successfully.");
            } else {
              redirectAttributes.addFlashAttribute("errorMessage", "Failed to save the Bulk.");
            } 
          } 
        } else if (!bulk.getAadharNumber().equalsIgnoreCase(bulk_old.getAadharNumber())) {
          AadharNumberDetails aadharNumberDetails = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(bulk.getAadharNumber());
          if (aadharNumberDetails != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Aadhar already used by someone else..Aadhar Already Exists.");
          } else {
            AadharNumberDetails aadharNumberDetails_delete = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(bulk_old.getAadharNumber());
            this.adharNumberDetailsService.deleteAadharNumberDetails(aadharNumberDetails_delete.getId());
            AadharNumberDetails aadharNumberDetails2 = new AadharNumberDetails();
            aadharNumberDetails2.setAadharNumber(bulk.getAadharNumber());
            aadharNumberDetails2.setEntity("Bulk " + bulk.getId());
            aadharNumberDetails2.setFullName(bulk.getFullName());
            aadharNumberDetails2.setMobileNumber(bulk.getMobileNumber());
            aadharNumberDetails2.setAddress(bulk.getAddress());
            this.adharNumberDetailsService.saveAadharNumberDetails(aadharNumberDetails2);
            Bulk updatedBulk = this.bulkService.updateBulk(bulk.getId(), bulk);
            if (updatedBulk != null) {
              redirectAttributes.addFlashAttribute("successMessage", "Bulk saved successfully.");
            } else {
              redirectAttributes.addFlashAttribute("errorMessage", "Failed to save the Bulk.");
            } 
          } 
        } else {
          Bulk updatedBulk = this.bulkService.updateBulk(bulk.getId(), bulk);
          if (updatedBulk != null) {
            redirectAttributes.addFlashAttribute("successMessage", "Bulk updated successfully.");
          } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update the Bulk.");
          } 
        } 
      } 
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
    } 
    addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
    addLicenseInfoToModel(model);
    return "redirect:/bulk";
  }
  
  @GetMapping({"/selectedBulk"})
  public String bulkview(@RequestParam("productId") long productId, Model model) {
    Optional<Bulk> bulkk = this.bulkService.getBulkById(Long.valueOf(productId));
    if (bulkk.isPresent())
      model.addAttribute("selectedProduct", bulkk.get()); 
    addLicenseInfoToModel(model);
    addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
    return "bulk_id";
  }
  
  @GetMapping({"/delete-Bulk-details"})
  public String deleteBulkDetails(@RequestParam("productId") Long productId, RedirectAttributes redirectAttributes) {
    try {
      Optional<Bulk> bulkcopy = this.bulkService.getBulkById(productId);
      String aadharno = null;
      if (bulkcopy.isPresent())
        aadharno = ((Bulk)bulkcopy.get()).getAadharNumber(); 
      Bulk updatebulk = this.bulkService.deleteBulkDetails(productId);
      if (updatebulk != null) {
        System.out.println("aadhar to delte " + aadharno);
        AadharNumberDetails aadharNumberDetails_delete = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(aadharno);
        this.adharNumberDetailsService.deleteAadharNumberDetails(aadharNumberDetails_delete.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Bulk details deleted successfully.");
      } else {
        redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete Bulk details.");
      } 
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
    } 
    return "redirect:/bulk";
  }
  
  @GetMapping({"/transportor"})
  public String getTransportorDetails(Model model) {
    List<Transportor> transportorDetails = this.transportorService.getAllTransportorDetails();
    model.addAttribute("transportorDetails", transportorDetails);
    List<Inscan> operatorTotalDetails = this.inscanService.getAllInscanDetailsForCurrentDay();
    model.addAttribute("operatorTotalDetails", operatorTotalDetails);
    addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
    addLicenseInfoToModel(model);
    return "transportor";
  }
  
  @GetMapping({"/transportorDetails"})
  public String transportorDetails(@RequestParam("productId") Long productId, @RequestParam("action") String action, Model model) {
    Optional<Transportor> transportor = this.transportorService.getTransportorById(productId);
    if (transportor.isPresent()) {
      model.addAttribute("selectedProduct", transportor.get());
      addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
      addLicenseInfoToModel(model);
      return "transportorDetails";
    } 
    return "Transportor NotFound";
  }
  
  @PostMapping({"/transportorsave"})
  public String transportorsave(@ModelAttribute Transportor transportor, RedirectAttributes redirectAttributes) {
    try {
      if (transportor.getId() == null) {
        Transportor createdTransportor = this.transportorService.addTransportor(transportor);
        if (createdTransportor != null) {
          redirectAttributes.addFlashAttribute("successMessage", "Transportor saved successfully.");
        } else {
          redirectAttributes.addFlashAttribute("errorMessage", "An Transportor with the same token number already exists.");
        } 
      } else {
        Optional<Transportor> transportor_old_opt = this.transportorService.getTransportorById(transportor.getId());
        Transportor transportor_old = null;
        if (transportor_old_opt.isPresent())
          transportor_old = transportor_old_opt.get(); 
        if (transportor_old.getAadharNumber() == null || "".equals(transportor_old.getAadharNumber())) {
          AadharNumberDetails aadharNumberDetails = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(transportor.getAadharNumber());
          if (aadharNumberDetails != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Aadhar Already Exists.");
          } else {
            AadharNumberDetails aadharNumberDetails2 = new AadharNumberDetails();
            aadharNumberDetails2.setAadharNumber(transportor.getAadharNumber());
            aadharNumberDetails2.setEntity("Transportor " + transportor.getId());
            aadharNumberDetails2.setFullName(transportor.getFullName());
            aadharNumberDetails2.setMobileNumber(transportor.getMobileNumber());
            aadharNumberDetails2.setAddress(transportor.getAddress());
            this.adharNumberDetailsService.saveAadharNumberDetails(aadharNumberDetails2);
            Transportor updatedTransportor = this.transportorService.updateTransportor(transportor.getId(), transportor);
            if (updatedTransportor != null) {
              redirectAttributes.addFlashAttribute("successMessage", "Transportor saved successfully.");
            } else {
              redirectAttributes.addFlashAttribute("errorMessage", "Failed to saved the Transportor.");
            } 
          } 
        } else if (!transportor.getAadharNumber().equalsIgnoreCase(transportor_old.getAadharNumber())) {
          AadharNumberDetails aadharNumberDetails = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(transportor.getAadharNumber());
          if (aadharNumberDetails != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Aadhar already used by someone else..Aadhar Already Exists.");
          } else {
            AadharNumberDetails aadharNumberDetails_delete = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(transportor_old.getAadharNumber());
            this.adharNumberDetailsService.deleteAadharNumberDetails(aadharNumberDetails_delete.getId());
            AadharNumberDetails aadharNumberDetails2 = new AadharNumberDetails();
            aadharNumberDetails2.setAadharNumber(transportor.getAadharNumber());
            aadharNumberDetails2.setEntity("Transportor " + transportor.getId());
            aadharNumberDetails2.setFullName(transportor.getFullName());
            aadharNumberDetails2.setMobileNumber(transportor.getMobileNumber());
            aadharNumberDetails2.setAddress(transportor.getAddress());
            this.adharNumberDetailsService.saveAadharNumberDetails(aadharNumberDetails2);
            Transportor updatedTransportor = this.transportorService.updateTransportor(transportor.getId(), transportor);
            if (updatedTransportor != null) {
              redirectAttributes.addFlashAttribute("successMessage", "Transportor saved successfully.");
            } else {
              redirectAttributes.addFlashAttribute("errorMessage", "Failed to saved the Transportor.");
            } 
          } 
        } else {
          Transportor updatedTransportor = this.transportorService.updateTransportor(transportor.getId(), transportor);
          if (updatedTransportor != null) {
            redirectAttributes.addFlashAttribute("successMessage", "Transportor updated successfully.");
          } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update the Transportor.");
          } 
        } 
      } 
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
    } 
    return "redirect:/transportor";
  }
  
  @GetMapping({"/selectedTransportor"})
  public String transportorview(@RequestParam("productId") long productId, Model model) {
    Optional<Transportor> transportor = this.transportorService.getTransportorById(Long.valueOf(productId));
    if (transportor.isPresent())
      model.addAttribute("selectedProduct", transportor.get()); 
    addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
    addLicenseInfoToModel(model);
    return "transportor_id";
  }
  
  @GetMapping({"/delete-Transportor-details"})
  public String deleteTransportorDetails(@RequestParam("productId") Long productId, RedirectAttributes redirectAttributes) {
    try {
      Optional<Transportor> transportorcopy = this.transportorService.getTransportorById(productId);
      String aadharno = null;
      if (transportorcopy.isPresent())
        aadharno = ((Transportor)transportorcopy.get()).getAadharNumber(); 
      Transportor updatetransportor = this.transportorService.deleteTransportorDetails(productId);
      if (updatetransportor != null) {
        System.out.println("aadhar to delte " + aadharno);
        AadharNumberDetails aadharNumberDetails_delete = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(aadharno);
        this.adharNumberDetailsService.deleteAadharNumberDetails(aadharNumberDetails_delete.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Transportor details deleted successfully.");
      } else {
        redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete Transportor details.");
      } 
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
    } 
    return "redirect:/transportor";
  }
  
  @GetMapping({"/workman"})
  public String getWorkmanDetails(Model model) {
    List<Workman> workmanDetails = this.workmanService.getAllWorkmanDetails();
    model.addAttribute("workmanDetails", workmanDetails);
    List<Inscan> inscanDetailsForProject = this.inscanService.findByEntryDateTimeBetweenOrderByDetailsForProject();
    model.addAttribute("inscanDetailsForProject", inscanDetailsForProject);
    addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
    addLicenseInfoToModel(model);
    return "workman";
  }
  
  @GetMapping({"/workmanDetails"})
  public String workmanDetails(@RequestParam("productId") Long productId, @RequestParam("action") String action, Model model) {
    Optional<Workman> workman = this.workmanService.getWorkmanById(productId);
    if (workman.isPresent()) {
      model.addAttribute("selectedProduct", workman.get());
      addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
      addLicenseInfoToModel(model);
      return "workmanDetails";
    } 
    return "Workman NotFound";
  }
  
  @PostMapping({"/workmansave"})
  public String workmansave(@ModelAttribute Workman workman, RedirectAttributes redirectAttributes) {
    try {
      if (workman.getId() == null) {
        Workman createdWorkman = this.workmanService.addWorkman(workman);
        if (createdWorkman != null) {
          redirectAttributes.addFlashAttribute("successMessage", "Workman saved successfully.");
        } else {
          redirectAttributes.addFlashAttribute("errorMessage", "An Workman with the same token number already exists.");
        } 
      } else {
        Optional<Workman> workman_old_opt = this.workmanService.getWorkmanById(workman.getId());
        Workman workman_old = null;
        if (workman_old_opt.isPresent())
          workman_old = workman_old_opt.get(); 
        if (workman_old.getAadharNumber() == null || "".equals(workman_old.getAadharNumber())) {
          AadharNumberDetails aadharNumberDetails = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(workman.getAadharNumber());
          if (aadharNumberDetails != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Aadhar Already Exists.");
          } else {
            AadharNumberDetails aadharNumberDetails2 = new AadharNumberDetails();
            aadharNumberDetails2.setAadharNumber(workman.getAadharNumber());
            aadharNumberDetails2.setEntity("Workman " + workman.getId());
            aadharNumberDetails2.setFullName(workman.getFullName());
            aadharNumberDetails2.setMobileNumber(workman.getMobileNumber());
            aadharNumberDetails2.setAddress(workman.getAddress());
            this.adharNumberDetailsService.saveAadharNumberDetails(aadharNumberDetails2);
            Workman updatedWorkman = this.workmanService.updateWorkman(workman.getId(), workman);
            if (updatedWorkman != null) {
              redirectAttributes.addFlashAttribute("successMessage", "Workman saved successfully.");
            } else {
              redirectAttributes.addFlashAttribute("errorMessage", "Failed to saved the Workman.");
            } 
          } 
        } else if (!workman.getAadharNumber().equalsIgnoreCase(workman_old.getAadharNumber())) {
          AadharNumberDetails aadharNumberDetails = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(workman.getAadharNumber());
          if (aadharNumberDetails != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Aadhar already used by someone else..Aadhar Already Exists.");
          } else {
            AadharNumberDetails aadharNumberDetails_delete = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(workman_old.getAadharNumber());
            this.adharNumberDetailsService.deleteAadharNumberDetails(aadharNumberDetails_delete.getId());
            AadharNumberDetails aadharNumberDetails2 = new AadharNumberDetails();
            aadharNumberDetails2.setAadharNumber(workman.getAadharNumber());
            aadharNumberDetails2.setEntity("Workman " + workman.getId());
            aadharNumberDetails2.setFullName(workman.getFullName());
            aadharNumberDetails2.setMobileNumber(workman.getMobileNumber());
            aadharNumberDetails2.setAddress(workman.getAddress());
            this.adharNumberDetailsService.saveAadharNumberDetails(aadharNumberDetails2);
            Workman updatedWorkman = this.workmanService.updateWorkman(workman.getId(), workman);
            if (updatedWorkman != null) {
              redirectAttributes.addFlashAttribute("successMessage", "Workman saved successfully.");
            } else {
              redirectAttributes.addFlashAttribute("errorMessage", "Failed to saved the Workman.");
            } 
          } 
        } else {
          Workman updatedWorkman = this.workmanService.updateWorkman(workman.getId(), workman);
          if (updatedWorkman != null) {
            redirectAttributes.addFlashAttribute("successMessage", "Workman updated successfully.");
          } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update the Workman.");
          } 
        } 
      } 
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
    } 
    return "redirect:/workman";
  }
  
  @GetMapping({"/selectedWorkman"})
  public String workmanview(@RequestParam("productId") long productId, Model model) {
    Optional<Workman> workman = this.workmanService.getWorkmanById(Long.valueOf(productId));
    if (workman.isPresent())
      model.addAttribute("selectedProduct", workman.get()); 
    addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
    addLicenseInfoToModel(model);
    return "workman_id";
  }
  
  @GetMapping({"/delete-Workman-details"})
  public String deleteWorkmanDetails(@RequestParam("productId") Long productId, RedirectAttributes redirectAttributes) {
    try {
      Optional<Workman> workmancopy = this.workmanService.getWorkmanById(productId);
      String aadharno = null;
      if (workmancopy.isPresent())
        aadharno = ((Workman)workmancopy.get()).getAadharNumber(); 
      Workman updateWorkman = this.workmanService.deleteWorkmanDetails(productId);
      if (updateWorkman != null) {
        System.out.println("aadhar to delte " + aadharno);
        AadharNumberDetails aadharNumberDetails_delete = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(aadharno);
        this.adharNumberDetailsService.deleteAadharNumberDetails(aadharNumberDetails_delete.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Workman details deleted successfully.");
      } else {
        redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete Workman details.");
      } 
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
    } 
    return "redirect:/workman";
  }
  
  @GetMapping({"/amc"})
  public String getAmcDetails(Model model) {
    List<Amc> AmcDetails = this.amcService.getAllAmcDetails();
    model.addAttribute("AmcDetails", AmcDetails);
    List<Inscan> inscanDetailsForProject = this.inscanService.findByEntryDateTimeBetweenOrderByDetailsForProject();
    model.addAttribute("inscanDetailsForProject", inscanDetailsForProject);
    addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
    addLicenseInfoToModel(model);
    return "amc";
  }
  
  @GetMapping({"/amcDetails"})
  public String amcDetails(@RequestParam("productId") Long productId, @RequestParam("action") String action, Model model) {
    Optional<Amc> amc = this.amcService.getAmcById(productId);
    if (amc.isPresent()) {
      model.addAttribute("selectedProduct", amc.get());
      addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
      addLicenseInfoToModel(model);
      return "amcDetails";
    } 
    return "Amc NotFound";
  }
  
  @PostMapping({"/amcsave"})
  public String amcsave(@ModelAttribute Amc amc, RedirectAttributes redirectAttributes) {
    try {
      if (amc.getId() == null) {
        Amc createdAmc = this.amcService.addAmc(amc);
        if (createdAmc != null) {
          redirectAttributes.addFlashAttribute("successMessage", "Amc saved successfully.");
        } else {
          redirectAttributes.addFlashAttribute("errorMessage", "An Amc with the same token number already exists.");
        } 
      } else {
        Optional<Amc> amc_old_opt = this.amcService.getAmcById(amc.getId());
        Amc amc_old = null;
        if (amc_old_opt.isPresent())
          amc_old = amc_old_opt.get(); 
        if (amc_old.getAadharNumber() == null || "".equals(amc_old.getAadharNumber())) {
          AadharNumberDetails aadharNumberDetails = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(amc.getAadharNumber());
          if (aadharNumberDetails != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Aadhar Already Exists.");
          } else {
            AadharNumberDetails aadharNumberDetails2 = new AadharNumberDetails();
            aadharNumberDetails2.setAadharNumber(amc.getAadharNumber());
            aadharNumberDetails2.setEntity("Amc " + amc.getId());
            aadharNumberDetails2.setFullName(amc.getFullName());
            aadharNumberDetails2.setMobileNumber(amc.getMobileNumber());
            aadharNumberDetails2.setAddress(amc.getAddress());
            this.adharNumberDetailsService.saveAadharNumberDetails(aadharNumberDetails2);
            Amc updatedAmc = this.amcService.updateAmc(amc.getId(), amc);
            if (updatedAmc != null) {
              redirectAttributes.addFlashAttribute("successMessage", "Amc saved successfully.");
            } else {
              redirectAttributes.addFlashAttribute("errorMessage", "Failed to saved the Amc.");
            } 
          } 
        } else if (!amc.getAadharNumber().equalsIgnoreCase(amc_old.getAadharNumber())) {
          AadharNumberDetails aadharNumberDetails = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(amc.getAadharNumber());
          if (aadharNumberDetails != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Aadhar already used by someone else..Aadhar Already Exists.");
          } else {
            AadharNumberDetails aadharNumberDetails_delete = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(amc_old.getAadharNumber());
            this.adharNumberDetailsService.deleteAadharNumberDetails(aadharNumberDetails_delete.getId());
            AadharNumberDetails aadharNumberDetails2 = new AadharNumberDetails();
            aadharNumberDetails2.setAadharNumber(amc.getAadharNumber());
            aadharNumberDetails2.setEntity("Amc " + amc.getId());
            aadharNumberDetails2.setFullName(amc.getFullName());
            aadharNumberDetails2.setMobileNumber(amc.getMobileNumber());
            aadharNumberDetails2.setAddress(amc.getAddress());
            this.adharNumberDetailsService.saveAadharNumberDetails(aadharNumberDetails2);
            Amc updatedAmc = this.amcService.updateAmc(amc.getId(), amc);
            if (updatedAmc != null) {
              redirectAttributes.addFlashAttribute("successMessage", "Amc saved successfully.");
            } else {
              redirectAttributes.addFlashAttribute("errorMessage", "Failed to saved the Amc.");
            } 
          } 
        } else {
          Amc updatedAmc = this.amcService.updateAmc(amc.getId(), amc);
          if (updatedAmc != null) {
            redirectAttributes.addFlashAttribute("successMessage", "Amc updated successfully.");
          } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update the Amc.");
          } 
        } 
      } 
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
    } 
    return "redirect:/amc";
  }
  
  @GetMapping({"/selectedAmc"})
  public String amcview(@RequestParam("productId") long productId, Model model) {
    Optional<Amc> amc = this.amcService.getAmcById(Long.valueOf(productId));
    if (amc.isPresent())
      model.addAttribute("selectedProduct", amc.get()); 
    addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
    addLicenseInfoToModel(model);
    return "amc_id";
  }
  
  @GetMapping({"/delete-amc-details"})
  public String deleteAmcDetails(@RequestParam("productId") Long productId, RedirectAttributes redirectAttributes) {
    try {
      Optional<Amc> amccopy = this.amcService.getAmcById(productId);
      String aadharno = null;
      if (amccopy.isPresent())
        aadharno = ((Amc)amccopy.get()).getAadharNumber(); 
      Amc updateAmc = this.amcService.deleteAmcDetails(productId);
      if (updateAmc != null) {
        System.out.println("aadhar to delte " + aadharno);
        AadharNumberDetails aadharNumberDetails_delete = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(aadharno);
        this.adharNumberDetailsService.deleteAadharNumberDetails(aadharNumberDetails_delete.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Amc details deleted successfully.");
      } else {
        redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete Amc details.");
      } 
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
    } 
    return "redirect:/amc";
  }
  
  @GetMapping({"/visitor"})
  public String getVisitorDetails(Model model) {
    List<Visitor> visitorDetails = this.visitorService.getAllVisitorDetails();
    model.addAttribute("visitorDetails", visitorDetails);
    System.out.println(visitorDetails);
    List<Inscan> inscanDetailsForVisitor = this.inscanService.findByEntryDateTimeBetweenOrderByDetailsForVisitor();
    model.addAttribute("inscanDetailsForVisitor", inscanDetailsForVisitor);
    addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
    addLicenseInfoToModel(model);
    return "visitor";
  }
  
  @GetMapping({"/visitorDetails"})
  public String visitorDetails(@RequestParam("visitorId") Long visitorId, @RequestParam("action") String action, Model model) {
    List<String> officerDetails = this.officerService.getAllOfficerFullNames();
    model.addAttribute("OfficerDetails", officerDetails);
    Optional<Visitor> visitor = this.visitorService.getVisitorById(visitorId);
    if (visitor.isPresent()) {
      model.addAttribute("selectedVisitor", visitor.get());
      addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
      addLicenseInfoToModel(model);
      return "visitorDetails";
    } 
    return "visitorNotFound";
  }
  
  @PostMapping({"/Visitorsave"})
  public String saveVisitor(@ModelAttribute Visitor visitor, RedirectAttributes redirectAttributes, @RequestParam("imgData") String imgData) {
    try {
      if (visitor.getId() == null) {
        visitor.setImageName(saveImgAndGetName(imgData, visitor));
        visitor.setRegular(true);
        Visitor createdVisitor = this.visitorService.addVisitor(visitor);
        if (createdVisitor != null) {
          redirectAttributes.addFlashAttribute("successMessage", "Visitor saved successfully.");
        } else {
          redirectAttributes.addFlashAttribute("errorMessage", "An Visitor with the same token number already exists.");
        } 
      } else {
        Optional<Visitor> visitor_old_opt = this.visitorService.getVisitorById(visitor.getId());
        Visitor visitor_old = null;
        if (visitor_old_opt.isPresent())
          visitor_old = visitor_old_opt.get(); 
        if (visitor_old.getAadharNumber() == null || "".equals(visitor_old.getAadharNumber())) {
          AadharNumberDetails aadharNumberDetails = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(visitor.getAadharNumber());
          if (aadharNumberDetails != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Aadhar Already Exists.");
          } else {
            AadharNumberDetails aadharNumberDetails2 = new AadharNumberDetails();
            aadharNumberDetails2.setAadharNumber(visitor.getAadharNumber());
            aadharNumberDetails2.setEntity("Visitor " + visitor.getId());
            aadharNumberDetails2.setFullName(visitor.getFullName());
            aadharNumberDetails2.setMobileNumber(visitor.getMobileNumber());
            aadharNumberDetails2.setAddress(visitor.getAddress());
            this.adharNumberDetailsService.saveAadharNumberDetails(aadharNumberDetails2);
            visitor.setImageName(saveImgAndGetName(imgData, visitor));
            if (visitor_old.getAadharNumber() == null || "".equals(visitor_old.getAadharNumber())) {
              visitor.setRegular(false);
            } else if (!visitor.getAadharNumber().equalsIgnoreCase(visitor_old.getAadharNumber())) {
              visitor.setRegular(true);
            } else {
              visitor.setRegular(visitor_old.isRegular());
            } 
            Visitor updatedVisitor = this.visitorService.updateVisitor(visitor.getId(), visitor);
            if (updatedVisitor != null) {
              redirectAttributes.addFlashAttribute("successMessage", "Visitor saved successfully.");
            } else {
              redirectAttributes.addFlashAttribute("errorMessage", "Failed to saved the Visitor.");
            } 
          } 
        } else if (!visitor.getAadharNumber().equalsIgnoreCase(visitor_old.getAadharNumber())) {
          AadharNumberDetails aadharNumberDetails = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(visitor.getAadharNumber());
          if (aadharNumberDetails != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Aadhar already used by someone else..Aadhar Already Exists.");
          } else {
            AadharNumberDetails aadharNumberDetails_delete = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(visitor_old.getAadharNumber());
            this.adharNumberDetailsService.deleteAadharNumberDetails(aadharNumberDetails_delete.getId());
            AadharNumberDetails aadharNumberDetails2 = new AadharNumberDetails();
            aadharNumberDetails2.setAadharNumber(visitor.getAadharNumber());
            aadharNumberDetails2.setEntity("Visitor " + visitor.getId());
            aadharNumberDetails2.setFullName(visitor.getFullName());
            aadharNumberDetails2.setMobileNumber(visitor.getMobileNumber());
            aadharNumberDetails2.setAddress(visitor.getAddress());
            this.adharNumberDetailsService.saveAadharNumberDetails(aadharNumberDetails2);
            visitor.setImageName(saveImgAndGetName(imgData, visitor));
            if (visitor.getAadharNumber() == null || "".equals(visitor.getAadharNumber())) {
              visitor.setRegular(false);
            } else if (!visitor.getAadharNumber().equalsIgnoreCase(visitor.getAadharNumber())) {
              visitor.setRegular(true);
            } else {
              visitor.setRegular(visitor.isRegular());
            } 
            Visitor updatedVisitor = this.visitorService.updateVisitor(visitor.getId(), visitor);
            if (updatedVisitor != null) {
              redirectAttributes.addFlashAttribute("successMessage", "Visitor saved successfully.");
            } else {
              redirectAttributes.addFlashAttribute("errorMessage", "Failed to saved the Visitor.");
            } 
          } 
        } else {
          visitor.setImageName(saveImgAndGetName(imgData, visitor));
          Visitor updatedVisitor = this.visitorService.updateVisitor(visitor.getId(), visitor);
          if (updatedVisitor != null) {
            redirectAttributes.addFlashAttribute("successMessage", "Visitor updated successfully.");
          } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update the Visitor.");
          } 
        } 
      } 
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
    } 
    return "redirect:/visitor";
  }
  
  public String saveImgAndGetName(String imgData, Visitor visitor) {
    String imageName = "";
    if (imgData != null && !"".equals(imgData)) {
      byte[] decodedImg = Base64.getDecoder().decode(imgData.split(",")[1]);
      String lastFourDigits = visitor.getAadharNumber().substring(visitor.getAadharNumber().length() - 4);
      imageName = visitor.getFullName().replaceAll("\\s+", "_") + "_" + visitor.getFullName().replaceAll("\\s+", "_") + ".jpg";
      this.uploadDir = "static/img/";
      try {
        String uploadPath = (new ClassPathResource(this.uploadDir)).getFile().getAbsolutePath();
        Path filePath = Paths.get(uploadPath + uploadPath + File.separator, new String[0]);
        Files.write(filePath, decodedImg, new java.nio.file.OpenOption[0]);
      } catch (IOException e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
      } 
    } 
    return imageName;
  }
  
  @GetMapping({"/selectedVisitor"})
  public String viewVisitor(@RequestParam("visitorId") long visitorId, Model model) {
    Optional<Visitor> visitorOptional = this.visitorService.getVisitorById(Long.valueOf(visitorId));
    if (visitorOptional.isPresent()) {
      Visitor visitor = visitorOptional.get();
      model.addAttribute("selectedVisitor", visitor);
      this.visitorService.storeVisitorDetailsIntoToken(visitor);
      VisitorTokenId lastVisitorToken = this.visitorTokenRepository.findFirstByOrderByIdDesc();
      model.addAttribute("lastVisitorToken", lastVisitorToken);
    } else {
      return "visitorNotFound";
    } 
    addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
    addLicenseInfoToModel(model);
    return "visitorId";
  }
  
  @GetMapping({"/delete-Visitor-details"})
  public String deleteVisitorDetails(@RequestParam("productId") Long productId, RedirectAttributes redirectAttributes, Model model) {
    try {
      Optional<Visitor> visitorcopy = this.visitorService.getVisitorById(productId);
      String aadharno = null;
      if (visitorcopy.isPresent())
        aadharno = ((Visitor)visitorcopy.get()).getAadharNumber(); 
      Visitor updateVisitor = this.visitorService.deleteVisitorDetails(productId);
      if (updateVisitor != null) {
        System.out.println("aadhar to delte " + aadharno);
        AadharNumberDetails aadharNumberDetails_delete = this.adharNumberDetailsService.getAadharNumberDetailsByAadharNumber(aadharno);
        this.adharNumberDetailsService.deleteAadharNumberDetails(aadharNumberDetails_delete.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Visitor details deleted successfully.");
      } else {
        redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete Visitor details.");
      } 
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
    } 
    addUsernameAndRoleToModel(model, (Principal)SecurityContextHolder.getContext().getAuthentication());
    addLicenseInfoToModel(model);
    return "redirect:/visitor";
  }
}
