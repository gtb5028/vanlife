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

-- Insert Locations
INSERT INTO location (id, name, latitude, longitude, type, description, created_by_id, created_at) VALUES
(1, 'Yellowstone National Park', 44.4280, -110.5885, 'PARK', 'Famous national park with geysers and wildlife.', 7, CURRENT_TIMESTAMP),
(2, 'Grand Canyon', 36.1069, -112.1129, 'CANYON', 'Iconic canyon with breathtaking views.', 7, CURRENT_TIMESTAMP),
(3, 'Yosemite National Park', 37.8651, -119.5383, 'PARK', 'Known for its waterfalls and giant sequoias.', 7, CURRENT_TIMESTAMP),
(4, 'Mount Rainier', 46.8523, -121.7603, 'MOUNTAIN', 'A massive stratovolcano in Washington.', 7, CURRENT_TIMESTAMP),
(5, 'Lake Tahoe', 39.0968, -120.0324, 'LAKE', 'A large freshwater lake in the Sierra Nevada.', 7, CURRENT_TIMESTAMP),
(6, 'Zion National Park', 37.2982, -113.0263, 'PARK', 'Famous for towering sandstone cliffs.', 7, CURRENT_TIMESTAMP),
(7, 'Arches National Park', 38.7331, -109.5925, 'PARK', 'Home to more than 2,000 natural stone arches.', 7, CURRENT_TIMESTAMP);

-- Reset the sequence value to avoid primary key conflicts
SELECT setval('location_id_seq', (SELECT MAX(id) FROM location));
