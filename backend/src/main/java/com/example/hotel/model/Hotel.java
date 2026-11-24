package com.example.hotel.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "hotels")
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String city;
    private String address;
    private String description;
    private int stars;
    private BigDecimal pricePerNight;
    private int totalRooms;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hotel_amenities", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "amenity")
    private Set<String> amenities = new HashSet<>();

    protected Hotel() {}

    public Hotel(String name, String city, String address, String description,
                 int stars, BigDecimal pricePerNight, int totalRooms) {
        this.name = name;
        this.city = city;
        this.address = address;
        this.description = description;
        this.stars = stars;
        this.pricePerNight = pricePerNight;
        this.totalRooms = totalRooms;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public String getDescription() { return description; }
    public int getStars() { return stars; }
    public BigDecimal getPricePerNight() { return pricePerNight; }
    public int getTotalRooms() { return totalRooms; }
    public Set<String> getAmenities() { return amenities; }
}
