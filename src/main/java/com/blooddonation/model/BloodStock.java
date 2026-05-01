package com.blooddonation.model;

import jakarta.persistence.*;

@Entity
@Table(name = "blood_stocks")
public class BloodStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "blood_bank_id", nullable = false)
    private BloodBank bloodBank;

    @Column(nullable = false)
    private String bloodGroup;

    @Column(nullable = false)
    private Integer units;

    public BloodStock() {}

    public BloodStock(BloodBank bloodBank, String bloodGroup, Integer units) {
        this.bloodBank = bloodBank;
        this.bloodGroup = bloodGroup;
        this.units = units;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BloodBank getBloodBank() { return bloodBank; }
    public void setBloodBank(BloodBank bloodBank) { this.bloodBank = bloodBank; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public Integer getUnits() { return units; }
    public void setUnits(Integer units) { this.units = units; }
}
