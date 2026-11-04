package com.smarttourism.tourist.service;

import com.smarttourism.tourist.model.TouristPlace;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory catalogue of tourist places.
 * Note: each instance keeps its own copy, so a POST to one instance is
 * not visible on the others. In production all instances would share a
 * database; it is kept in memory here so the demo runs with no setup.
 */
@Service
public class TouristPlaceService {

    private final Map<Long, TouristPlace> places = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public TouristPlaceService() {
        add(new TouristPlace(null, "Kanaka Durga Temple", "Vijayawada", "Temple",
                "Hilltop temple on Indrakeeladri overlooking the Krishna river", 4.7));
        add(new TouristPlace(null, "Prakasam Barrage", "Vijayawada", "Landmark",
                "Barrage across the Krishna river, lit up in the evenings", 4.4));
        add(new TouristPlace(null, "Undavalli Caves", "Guntur", "Heritage",
                "Rock-cut caves with a large reclining Vishnu statue", 4.5));
        add(new TouristPlace(null, "Araku Valley", "Visakhapatnam", "Nature",
                "Hill station known for coffee plantations and tribal culture", 4.6));
        add(new TouristPlace(null, "Tirumala Venkateswara Temple", "Tirupati", "Temple",
                "One of the most visited pilgrimage sites in the world", 4.8));
        add(new TouristPlace(null, "RK Beach", "Visakhapatnam", "Beach",
                "Popular city beach with a submarine museum nearby", 4.3));
    }

    public List<TouristPlace> findAll() {
        List<TouristPlace> list = new ArrayList<>(places.values());
        list.sort(Comparator.comparing(TouristPlace::getId));
        return list;
    }

    public Optional<TouristPlace> findById(Long id) {
        return Optional.ofNullable(places.get(id));
    }

    public List<TouristPlace> findByCity(String city) {
        return findAll().stream().filter(p -> p.getCity().equalsIgnoreCase(city)).toList();
    }

    public TouristPlace add(TouristPlace place) {
        place.setId(nextId.getAndIncrement());
        places.put(place.getId(), place);
        return place;
    }

    public Optional<TouristPlace> update(Long id, TouristPlace place) {
        if (!places.containsKey(id)) return Optional.empty();
        place.setId(id);
        places.put(id, place);
        return Optional.of(place);
    }

    public boolean delete(Long id) {
        return places.remove(id) != null;
    }
}
