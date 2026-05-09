package com.blooddonation.dto;

public class StatsDto {
    private long totalDonors;
    private long livesSaved;
    private long activeRequests;
    private long totalBloodBanks;

    public StatsDto(long totalDonors, long livesSaved, long activeRequests, long totalBloodBanks) {
        this.totalDonors = totalDonors;
        this.livesSaved = livesSaved;
        this.activeRequests = activeRequests;
        this.totalBloodBanks = totalBloodBanks;
    }

    public long getTotalDonors() { return totalDonors; }
    public void setTotalDonors(long totalDonors) { this.totalDonors = totalDonors; }

    public long getLivesSaved() { return livesSaved; }
    public void setLivesSaved(long livesSaved) { this.livesSaved = livesSaved; }

    public long getActiveRequests() { return activeRequests; }
    public void setActiveRequests(long activeRequests) { this.activeRequests = activeRequests; }

    public long getTotalBloodBanks() { return totalBloodBanks; }
    public void setTotalBloodBanks(long totalBloodBanks) { this.totalBloodBanks = totalBloodBanks; }
}
