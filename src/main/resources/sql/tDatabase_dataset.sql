USE tDatabase;

INSERT INTO addresses (addressLine1, addressLine2, city, county, postcode, country) VALUES
('123 Admin St', 'Suite 1', 'Dundalk', 'Louth', 'A91 X123', 'Ireland'),
('45 Customer Road', NULL, 'Dublin', 'Dublin', 'D01 A111', 'Ireland'),
('78 Staff Avenue', NULL, 'Cork', 'Cork', 'T12 B222', 'Ireland');

INSERT INTO users (addressId, username, email, dateOfBirth, password, userType) VALUES
(1, 'john_doe', 'john@example.com', '1990-05-12', '$2a$12$C5vCOuYxG.WAJW4xSVMYEOe8ruelEdQ.uRcQ.KOOTkN8NX1W45Ucq', 1),
(2, 'admin_user', 'admin@example.com', '1985-03-20', '$2a$12$C5vCOuYxG.WAJW4xSVMYEOe8ruelEdQ.uRcQ.KOOTkN8NX1W45Ucq', 2),
(3, 'staff_mary', 'mary.staff@example.com', '1992-11-02', '$2a$12$C5vCOuYxG.WAJW4xSVMYEOe8ruelEdQ.uRcQ.KOOTkN8NX1W45Ucq', 1);

INSERT INTO location (branchName, address, phoneNumber) VALUES
('Dundalk Branch', 'Dublin Road, Dundalk', '0429370200'),
('Dublin Central', '12 Main Street, Dublin', '012345678');

INSERT INTO carDetails (regNumber, make, model, carYear, colour, mileage, transmission, currentStatus, fuelType) VALUES
('191D12345', 'Toyota', 'Corolla', 2019, 'Silver', 45000, 'automatic', 'available', 'hybrid'),
('201C54321', 'Volkswagen', 'Golf', 2020, 'Blue', 30000, 'manual', 'rented', 'diesel'),
('181G99887', 'Nissan', 'Leaf', 2018, 'White', 52000, 'automatic', 'maintenance', 'electric'),
('222D45781', 'BMW', '3 Series', 2022, 'Black', 18000, 'automatic', 'available', 'petrol'),
('231D77890', 'Audi', 'A4', 2023, 'Grey', 12000, 'automatic', 'available', 'diesel'),
('202D33445', 'Ford', 'Focus', 2020, 'Red', 39000, 'manual', 'available', 'petrol'),
('211L90876', 'Hyundai', 'Tucson', 2021, 'White', 28000, 'automatic', 'available', 'hybrid'),
('232C11223', 'Tesla', 'Model 3', 2023, 'Blue', 9000, 'automatic', 'available', 'electric'),
('191G66118', 'Kia', 'Sportage', 2019, 'Silver', 47000, 'manual', 'available', 'diesel'),
('221D77001', 'Mercedes-Benz', 'C-Class', 2022, 'Navy', 21000, 'automatic', 'available', 'petrol'),
('201D88002', 'Skoda', 'Octavia', 2020, 'Green', 36000, 'manual', 'available', 'diesel'),
('241D99112', 'Toyota', 'Yaris Cross', 2024, 'Pearl', 5000, 'automatic', 'available', 'hybrid');

INSERT INTO driverdetails (addressId, firstName, lastName, email, phoneNumber, licenseNumber, dateOfBirth, permitType) VALUES
(1, 'John', 'Murphy', 'john.murphy@example.com', '0851112222', 'D1234567', '1990-01-15', 'manual'),
(2, 'Amy', 'Donoghue', 'amy.donoghue@example.com', '0899769309', 'D123421', '1999-04-16', 'automatic'),
(3, 'Sarah', 'OBrien', 'sarah.obrien@example.com', '0863334444', 'C7654321', '1988-07-09', 'automatic');

INSERT INTO bookings (driverId, userId, carId, pickupDatetime, returnDatetime, pickupLocationId, totalPrice, status) VALUES
(1, 1, 1, '2024-03-01 10:00:00', '2024-03-05 10:00:00', 1, 250.00, 'confirmed'),
(2, 1, 2, '2024-03-10 09:00:00', '2024-03-12 09:00:00', 2, 180.00, 'active'),
(1, 3, 3, '2024-02-20 14:00:00', '2024-02-22 14:00:00', 1, 150.00, 'returned');

INSERT INTO payment (bookingId, amount, paymentStatus, transactionRef) VALUES
(1, 250.00, 'paid', 'SIM-SEED-0001'),
(2, 180.00, 'paid', 'SIM-SEED-0002'),
(3, 150.00, 'paid', 'SIM-SEED-0003');
