package com.hrgenius.repository;
import com.hrgenius.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
 List<Candidate> findByEmail(String email);
 List<Candidate> findByJobOpeningId(Long jobOpeningId);
 List<Candidate> findByApplicationStatus(String applicationStatus);
 List<Candidate> findByJobOpeningIdAndApplicationStatus(Long jobOpeningId, String applicationStatus);
}
