package com.virtualmarathon.marathon.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

@Entity
public class User {
    @Id
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false)
    private Long marathonsCompleted;

    @Column(nullable = false)
    private int currentOrganizerCount;

    @Column(nullable = false)
    private Long totalPoints;

    @OneToMany(mappedBy = "athlete",fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Lap> lapList;

    @OneToMany(mappedBy = "organizer",fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Marathon> marathonsOrganized;

    public User() {
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.totalPoints =0L;
        this.currentOrganizerCount=0;
        this.marathonsCompleted=0L;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Long getMarathonsCompleted() {
        return marathonsCompleted;
    }

    public int getCurrentOrganizerCount() {
        return currentOrganizerCount;
    }

    public Long getTotalPoints() {
        return totalPoints;
    }

    public List<Lap> getLapList() {
        return lapList;
    }

    public List<Marathon> getMarathonsOrganized() {
        return marathonsOrganized;
    }

    public void addMarathonsOrganized(Marathon marathon) {
        this.marathonsOrganized.add(marathon);
    }

    public void addLap(Lap lap) {
        this.lapList.add(lap);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMarathonsCompleted(Long marathonsCompleted) {
        this.marathonsCompleted = marathonsCompleted;
    }

    public void setCurrentOrganizerCount(int currentOrganizerCount) {
        this.currentOrganizerCount = currentOrganizerCount;
    }

    public void setTotalPoints(Long totalPoints) {
        this.totalPoints = totalPoints;
    }
}
