package com.blooddonation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BloodStockDto {
    private Long id;
    private Long bloodBankId;
    private String bloodBankName;
    
    @NotBlank(message = "Blood group is required")
    private String bloodGroup;
    
    @NotNull(message = "Units are required")
    @Min(value = 0, message = "Units cannot be negative")
    private Integer units;

    public BloodStockDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBloodBankId() { return bloodBankId; }
    public void setBloodBankId(Long bloodBankId) { this.bloodBankId = bloodBankId; }

    public String getBloodBankName() { return bloodBankName; }
    public void setBloodBankName(String bloodBankName) { this.bloodBankName = bloodBankName; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public Integer getUnits() { return units; }
    public void setUnits(Integer units) { this.units = units; }
}
