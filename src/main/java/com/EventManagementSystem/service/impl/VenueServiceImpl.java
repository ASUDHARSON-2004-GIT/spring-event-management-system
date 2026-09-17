package com.EventManagementSystem.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.EventManagementSystem.exception.ValidationException;
import com.EventManagementSystem.model.Venue;
import com.EventManagementSystem.repository.VenueRepository;
import com.EventManagementSystem.service.VenueService;

@Service
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;

    @Autowired
    public VenueServiceImpl(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Override
    public Venue createVenue(String name, String doorNo, String street, String city, String state) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Venue name cannot be empty");
        }
        Venue venue = new Venue(name, doorNo, street, city, state);
        return venueRepository.save(venue);
    }

    @Override
    public List<Venue> listVenues() {
        return venueRepository.findAll();
    }

    @Override
    public Venue findVenueById(long id) {
        return venueRepository.findById(id).orElse(null);
    }

    @Override
    public void updateVenue(long id, String name, String doorNo, String street, String city, String state) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Venue not found with id " + id));
        venue.setName(name);
        venue.setDoor_no(doorNo);
        venue.setStreet(street);
        venue.setCity(city);
        venue.setState(state);
        venueRepository.save(venue);
    }

    @Override
    public void deleteVenue(long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Venue not found with id " + id));
        venueRepository.delete(venue);
    }
}
