-- Sample data: 6 hotels. INSERT IGNORE + unique keys make this safe to run on every start.

INSERT IGNORE INTO hotels (name, city, address, description, stars, price_per_night, total_rooms) VALUES
  ('Palm Shore Resort', 'Goa', 'Calangute Beach Road, Calangute', 'Beachfront resort a two-minute walk from Calangute beach, with a large pool and an all-day cafe.', 4, 6500, 6),
  ('Fontainhas Courtyard', 'Goa', '31st January Road, Panaji', 'Small heritage guesthouse in Panaji''s Latin Quarter, close to cafes and the river promenade.', 3, 3200, 4),
  ('Marine Drive Grand', 'Mumbai', 'Marine Drive, Churchgate', 'Sea-facing rooms along Marine Drive, five minutes from Churchgate station.', 5, 12500, 8),
  ('Pink City Haveli', 'Jaipur', 'Chandpole Bazaar, Jaipur', 'Restored haveli with a rooftop restaurant that looks across to Nahargarh Fort.', 4, 5400, 5),
  ('Connaught Business Inn', 'Delhi', 'Barakhamba Road, Connaught Place', 'Practical rooms next to Rajiv Chowk metro with fast Wi-Fi and a quiet work lounge.', 3, 4100, 10),
  ('Backwater Retreat', 'Kochi', 'Fort Kochi Waterfront', 'Quiet stay a short walk from the Chinese fishing nets, with sunset views over the harbour.', 4, 5800, 3);

INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Pool' FROM hotels WHERE name = 'Palm Shore Resort';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Wi-Fi' FROM hotels WHERE name = 'Palm Shore Resort';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Breakfast included' FROM hotels WHERE name = 'Palm Shore Resort';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Beach access' FROM hotels WHERE name = 'Palm Shore Resort';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Air conditioning' FROM hotels WHERE name = 'Palm Shore Resort';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Wi-Fi' FROM hotels WHERE name = 'Fontainhas Courtyard';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Breakfast included' FROM hotels WHERE name = 'Fontainhas Courtyard';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Air conditioning' FROM hotels WHERE name = 'Fontainhas Courtyard';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Courtyard garden' FROM hotels WHERE name = 'Fontainhas Courtyard';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Sea view' FROM hotels WHERE name = 'Marine Drive Grand';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Pool' FROM hotels WHERE name = 'Marine Drive Grand';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Spa' FROM hotels WHERE name = 'Marine Drive Grand';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Wi-Fi' FROM hotels WHERE name = 'Marine Drive Grand';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Gym' FROM hotels WHERE name = 'Marine Drive Grand';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Airport transfer' FROM hotels WHERE name = 'Marine Drive Grand';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Rooftop restaurant' FROM hotels WHERE name = 'Pink City Haveli';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Wi-Fi' FROM hotels WHERE name = 'Pink City Haveli';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Breakfast included' FROM hotels WHERE name = 'Pink City Haveli';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Air conditioning' FROM hotels WHERE name = 'Pink City Haveli';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Heritage tours' FROM hotels WHERE name = 'Pink City Haveli';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Wi-Fi' FROM hotels WHERE name = 'Connaught Business Inn';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Work lounge' FROM hotels WHERE name = 'Connaught Business Inn';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Gym' FROM hotels WHERE name = 'Connaught Business Inn';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Air conditioning' FROM hotels WHERE name = 'Connaught Business Inn';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, '24-hour reception' FROM hotels WHERE name = 'Connaught Business Inn';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Sea view' FROM hotels WHERE name = 'Backwater Retreat';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Breakfast included' FROM hotels WHERE name = 'Backwater Retreat';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Wi-Fi' FROM hotels WHERE name = 'Backwater Retreat';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Ayurvedic massage' FROM hotels WHERE name = 'Backwater Retreat';
INSERT IGNORE INTO hotel_amenities (hotel_id, amenity) SELECT id, 'Air conditioning' FROM hotels WHERE name = 'Backwater Retreat';
