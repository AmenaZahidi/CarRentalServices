




INSERT INTO addresses (addressLine1, addressLine2, city, county, postcode, country) VALUES
('12 Main Street', 'Apt 3B', 'Dundalk', 'Louth', 'A91 XY23', 'Ireland'),
('45 Oakwood Drive', NULL, 'Drogheda', 'Louth', 'A92 HT71', 'Ireland'),
('7 Riverbank Close', NULL, 'Ardee', 'Louth', 'A92 P9K4', 'Ireland');

INSERT INTO users (addressId, username, email, dateOfBirth, password, userType) VALUES
(1, 'john_doe', 'john@example.com', '1990-05-12', 'hashedpass1', 1),
(2, 'admin_user', 'admin@example.com', '1985-03-20', 'hashedpass2', 2),
(3, 'staff_mary', 'mary.staff@example.com', '1992-11-02', 'hashedpass3', 1);

INSERT INTO location (branchName, address, phoneNumber) VALUES
('Dublin Central', '12 Main Street, Dublin', '012345678'),
('Cork City', '45 River Road, Cork', '021987654'),
('Galway West', '78 Ocean Drive, Galway', '091223344');

-- CAR DETAILS
INSERT INTO carDetails (regNumber, make, model, carYear, colour, mileage, transmission, currentStatus, fuelType) VALUES
('191D12345', 'Toyota', 'Corolla', 2019, 'Silver', 45000, 'automatic', 'available', 'hybrid'),
('201C54321', 'Volkswagen', 'Golf', 2020, 'Blue', 30000, 'manual', 'rented', 'diesel'),
('181G99887', 'Nissan', 'Leaf', 2018, 'White', 52000, 'automatic', 'maintenance', 'electric');

-- DRIVER DETAILS
INSERT INTO driverdetails (addressId, firstName, lastName, email, phoneNumber, licenseNumber, dateOfBirth, permitType) VALUES
(1, 'John', 'Murphy', 'john.murphy@example.com', '0851112222', 'D1234567', '1990-01-15', 'manual'),
(2, 'Amy', 'Donoghue', 'amy.donoghue@example.com', '0899769309', 'D123421', '1999-04-16', 'automatic'),
(3, 'Sarah', 'OBrien', 'sarah.obrien@example.com', '0863334444', 'C7654321', '1988-07-09', 'automatic');

INSERT INTO bookings (driverId, userId, carId, pickupDatetime, returnDatetime, pickupLocationId, totalPrice, status) VALUES
(1, 1, 1, '2024-03-01 10:00:00', '2024-03-05 10:00:00', 1, 250.00, 'confirmed'),
(2, 1, 2, '2024-03-10 09:00:00', '2024-03-12 09:00:00', 2, 180.00, 'active'),
(1, 3, 3, '2024-02-20 14:00:00', '2024-02-22 14:00:00', 3, 150.00, 'returned');

INSERT INTO payment (bookingId, amount) VALUES
(1, 250.00),
(2, 180.00),
(3, 150.00);




