package com.blooddonation.service;

import com.blooddonation.dto.BloodStockDto;
import com.blooddonation.model.BloodBank;
import com.blooddonation.model.BloodStock;
import com.blooddonation.repository.BloodBankRepository;
import com.blooddonation.repository.BloodStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BloodStockService {

    private final BloodStockRepository bloodStockRepository;
    private final BloodBankRepository bloodBankRepository;

    public BloodStockService(BloodStockRepository bloodStockRepository, BloodBankRepository bloodBankRepository) {
        this.bloodStockRepository = bloodStockRepository;
        this.bloodBankRepository = bloodBankRepository;
    }

    public List<BloodStockDto> getStockByBank(Long bloodBankId) {
        BloodBank bloodBank = bloodBankRepository.findById(bloodBankId)
                .orElseThrow(() -> new RuntimeException("Blood Bank not found"));
        
        return bloodStockRepository.findByBloodBank(bloodBank).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BloodStockDto updateStock(Long bloodBankId, BloodStockDto stockDto) {
        BloodBank bloodBank = bloodBankRepository.findById(bloodBankId)
                .orElseThrow(() -> new RuntimeException("Blood Bank not found"));

        Optional<BloodStock> stockOptional = bloodStockRepository.findByBloodBankAndBloodGroup(bloodBank, stockDto.getBloodGroup());
        
        BloodStock stock;
        if (stockOptional.isPresent()) {
            stock = stockOptional.get();
            stock.setUnits(stockDto.getUnits());
        } else {
            stock = new BloodStock(bloodBank, stockDto.getBloodGroup(), stockDto.getUnits());
        }

        return convertToDto(bloodStockRepository.save(stock));
    }

    public List<BloodStockDto> findBanksByBloodGroup(String bloodGroup, String city) {
        List<BloodStock> stocks;
        if (city != null && !city.isEmpty()) {
            stocks = bloodStockRepository.findByBloodGroupAndBloodBank_LocationContainingIgnoreCaseAndUnitsGreaterThan(bloodGroup, city, 0);
        } else {
            stocks = bloodStockRepository.findByBloodGroupAndUnitsGreaterThan(bloodGroup, 0);
        }
        
        return stocks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private BloodStockDto convertToDto(BloodStock stock) {
        BloodStockDto dto = new BloodStockDto();
        dto.setId(stock.getId());
        dto.setBloodBankId(stock.getBloodBank().getId());
        dto.setBloodBankName(stock.getBloodBank().getBankName());
        dto.setBloodGroup(stock.getBloodGroup());
        dto.setUnits(stock.getUnits());
        return dto;
    }
}
