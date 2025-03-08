-- Insert Users with hashed passwords (password is 'password')
INSERT INTO usr (id, email, name, picture, active, roles, password) VALUES
(1, 'tbeerbower@yahoo.com', 'Tom Beerbower', '', true, 'ROLE_ADMIN', '$2a$10$TXkorQjx0GhjdjgJ2A84.OQ5W3Q5OWWu.SXXCKjyDt.vXD2WdzxyS'),
(2, 'jane.smith@example.com', 'Jane Smith', '', true, 'ROLE_USER', '$2a$10$TXkorQjx0GhjdjgJ2A84.OQ5W3Q5OWWu.SXXCKjyDt.vXD2WdzxyS'),
(3, 'bob.wilson@example.com', 'Bob Wilson', '', true, 'ROLE_USER', '$2a$10$TXkorQjx0GhjdjgJ2A84.OQ5W3Q5OWWu.SXXCKjyDt.vXD2WdzxyS'),
(4, 'alice.jones@example.com', 'Alice Jones', '', true, 'ROLE_USER', '$2a$10$TXkorQjx0GhjdjgJ2A84.OQ5W3Q5OWWu.SXXCKjyDt.vXD2WdzxyS'),
(5, 'john.doe@example.com', 'John Doe', '', true, 'ROLE_USER', '$2a$10$TXkorQjx0GhjdjgJ2A84.OQ5W3Q5OWWu.SXXCKjyDt.vXD2WdzxyS'),
(6, 'kate@example.com', 'Kate Spate', '', true, 'ROLE_USER', '$2a$10$TXkorQjx0GhjdjgJ2A84.OQ5W3Q5OWWu.SXXCKjyDt.vXD2WdzxyS'),
(7, 'gtb5028@gmail.com', 'Greg Beerbower', '', true, 'ROLE_ADMIN', '$2a$10$TXkorQjx0GhjdjgJ2A84.OQ5W3Q5OWWu.SXXCKjyDt.vXD2WdzxyS');

-- Reset the sequence value to avoid primary key conflicts
SELECT setval('usr_id_seq', (SELECT MAX(id) FROM usr));

-- Insert default location types
INSERT INTO location_type (id, name, description) VALUES
(1, 'Campground', 'Public or private campground'),
(2, 'Parking', 'Public parking area'),
(3, 'Water Source', 'Public potable water source'),
(4, 'National Park', 'Public or fee required for entry national park'),
(5, 'Restaurant', 'Public or private restaurant');

INSERT INTO location_type_overpass_tags(location_type_id, tag_key, tag_value) VALUES
(1, 'tourism', 'camp_site'),
(2, 'amenity', 'parking'),
(2, 'access', 'public'),
(3, 'amenity', 'drinking_water'),
(3, 'access', 'public'),
(4, 'boundary', 'national_park'),
(5, 'amenity', 'restaurant');

-- Reset the sequence value to avoid primary key conflicts
SELECT setval('location_type_id_seq', (SELECT MAX(id) FROM location_type));

-- Insert Locations
INSERT INTO location (id, source, name, latitude, longitude, location_type_id, description, created_by_id, created_at) VALUES
(1, 0, 'Yellowstone National Park', 44.4280, -110.5885, 4, 'Famous national park with geysers and wildlife.', 7, CURRENT_TIMESTAMP),
(2, 0, 'Grand Canyon', 36.1069, -112.1129, 4, 'Iconic canyon with breathtaking views.', 7, CURRENT_TIMESTAMP),
(3, 0, 'Yosemite National Park', 37.8651, -119.5383, 4, 'Known for its waterfalls and giant sequoias.', 7, CURRENT_TIMESTAMP),
(4, 0, 'Mount Rainier', 46.8523, -121.7603, 4, 'A massive stratovolcano in Washington.', 7, CURRENT_TIMESTAMP),
(5, 0, 'Lake Tahoe', 39.0968, -120.0324, 4, 'A large freshwater lake in the Sierra Nevada.', 7, CURRENT_TIMESTAMP),
(6, 0, 'Zion National Park', 37.2982, -113.0263, 4, 'Famous for towering sandstone cliffs.', 7, CURRENT_TIMESTAMP),
(7, 0, 'Arches National Park', 38.7331, -109.5925, 4, 'Home to more than 2,000 natural stone arches.', 7, CURRENT_TIMESTAMP);

-- Reset the sequence value to avoid primary key conflicts
SELECT setval('location_id_seq', (SELECT MAX(id) FROM location));

-- Insert Reviews
INSERT INTO review (id, created_by_id, location_id, rating, comment, created_at) VALUES
(1, 7, 1, 9, 'Amazing views and peaceful atmosphere!', CURRENT_TIMESTAMP),
(2, 7, 1, 8, 'Nice spot but can get crowded.', CURRENT_TIMESTAMP),
(3, 7, 5, 7, 'Decent place, but the lake was a bit dirty.', CURRENT_TIMESTAMP);

-- Reset the sequence value to avoid primary key conflicts
SELECT setval('review_id_seq', (SELECT MAX(id) FROM review));
