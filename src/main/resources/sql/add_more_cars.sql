USE tDatabase;

INSERT IGNORE INTO carDetails (regNumber, make, model, carYear, colour, mileage, transmission, currentStatus, fuelType) VALUES
('222D45781', 'BMW', '3 Series', 2022, 'Black', 18000, 'automatic', 'available', 'petrol'),
('231D77890', 'Audi', 'A4', 2023, 'Grey', 12000, 'automatic', 'available', 'diesel'),
('202D33445', 'Ford', 'Focus', 2020, 'Red', 39000, 'manual', 'available', 'petrol'),
('211L90876', 'Hyundai', 'Tucson', 2021, 'White', 28000, 'automatic', 'available', 'hybrid'),
('232C11223', 'Tesla', 'Model 3', 2023, 'Blue', 9000, 'automatic', 'available', 'electric'),
('191G66118', 'Kia', 'Sportage', 2019, 'Silver', 47000, 'manual', 'available', 'diesel'),
('221D77001', 'Mercedes-Benz', 'C-Class', 2022, 'Navy', 21000, 'automatic', 'available', 'petrol'),
('201D88002', 'Skoda', 'Octavia', 2020, 'Green', 36000, 'manual', 'available', 'diesel'),
('241D99112', 'Toyota', 'Yaris Cross', 2024, 'Pearl', 5000, 'automatic', 'available', 'hybrid');
