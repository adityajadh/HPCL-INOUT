package com.in_out.repo;


import com.in_out.model.Transportor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransportorRepository extends JpaRepository<Transportor, Long> {
  Transportor findByAadharNumber(String paramString);
}
