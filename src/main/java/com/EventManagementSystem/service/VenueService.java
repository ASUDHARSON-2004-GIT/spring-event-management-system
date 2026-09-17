package com.EventManagementSystem.service;

import java.util.List;

import com.EventManagementSystem.model.Venue;

public interface VenueService {

    Venue createVenue(String name, String doorNo, String street, String city, String state);

    List<Venue> listVenues();

    Venue findVenueById(long id);

    void updateVenue(long id, String name, String doorNo, String street, String city, String state);

    void deleteVenue(long id);
}
