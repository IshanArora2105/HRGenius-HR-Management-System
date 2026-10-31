package com.hrgenius.repository;
import com.hrgenius.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
 List<Interview> findByCandidateId(Long candidateId);
 List<Interview> findByInterviewerId(Long interviewerId);
}
