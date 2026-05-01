package com.blooddonation.repository;

import com.blooddonation.model.BloodStock;
import com.blooddonation.model.BloodBank;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BloodStockRepository extends JpaRepository<BloodStock, Long> {
    List<BloodStock> findByBloodBank(BloodBank bloodBank);
    Optional<BloodStock> findByBloodBankAndBloodGroup(BloodBank bloodBank, String bloodGroup);
    List<BloodStock> findByBloodGroupAndUnitsGreaterThan(String bloodGroup, Integer units);
    List<BloodStock> findByBloodGroupAndBloodBank_LocationContainingIgnoreCaseAndUnitsGreaterThan(String bloodGroup, String location, Integer units);
}
