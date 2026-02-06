USE tDatabase;
--USERS
INSERT INTO users (username, email, password, DateOfBirth, userType) VALUES
('john_doe', 'john@example.com', 'hashedpass1', '1990-05-12', 1),
('admin_user', 'admin@example.com', 'hashedpass2', '1985-03-20', 2),
('staff_mary', 'mary.staff@example.com', 'hashedpass3', '1992-11-02', 3);

-- LOCATIONS
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
INSERT INTO driverdetails (firstName, lastName, email, phoneNumber, address, licenseNumber, dateOfBirth, permitType) VALUES
('John', 'Murphy', 'john.murphy@example.com', '0851112222', '10 Green Road, Dublin', 'D1234567', '1990-01-15', 'manual'),
('Sarah', 'OBrien', 'sarah.obrien@example.com', '0863334444', '22 Oak Street, Cork', 'C7654321', '1988-07-09', 'automatic');

-- BOOKINGS
INSERT INTO bookings (driverId, userId, carId, pickupDatetime, returnDatetime, pickupLocationId, totalPrice, status) VALUES
(1, 1, 1, '2024-03-01 10:00:00', '2024-03-05 10:00:00', 1, 250.00, 'confirmed'),
(2, 1, 2, '2024-03-10 09:00:00', '2024-03-12 09:00:00', 2, 180.00, 'active'),
(1, 3, 3, '2024-02-20 14:00:00', '2024-02-22 14:00:00', 3, 150.00, 'returned');

-- PAYMENTS
INSERT INTO payment (bookingId, amount) VALUES
(1, 250.00),
(2, 180.00),
(3, 150.00);

