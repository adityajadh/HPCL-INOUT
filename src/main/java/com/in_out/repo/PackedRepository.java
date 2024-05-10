package com.in_out.repo;


import com.in_out.model.Packed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackedRepository extends JpaRepository<Packed, Long> {
  Packed findByAadharNumber(String paramString);
}
