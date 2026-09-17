package com.EventManagementSystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.EventManagementSystem.model.Venue;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}
