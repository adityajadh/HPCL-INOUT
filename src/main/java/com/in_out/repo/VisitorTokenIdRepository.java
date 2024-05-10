package com.in_out.repo;


import com.in_out.model.VisitorTokenId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitorTokenIdRepository extends JpaRepository<VisitorTokenId, Long> {
  VisitorTokenId findFirstByOrderByIdDesc();
}
