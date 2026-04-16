package com.blooddonation.repository;

import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    List<BloodRequest> findByRequesterOrderByRequestDateDesc(User requester);
    List<BloodRequest> findByStatusOrderByRequestDateDesc(String status);
}
