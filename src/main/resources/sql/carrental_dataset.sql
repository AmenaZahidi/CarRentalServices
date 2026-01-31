USE carrental;

-- USERS
INSERT INTO users (username, email, password, userType) VALUES
         ('admin1',   'admin1@email.com',   'pass_admin1', 2),
         ('staff1',   'staff1@email.com',   'pass_staff1', 3),
         ('alice01',  'alice01@email.com',  'pass_alice',  1),
         ('bob02',    'bob02@email.com',    'pass_bob',    1),
         ('cathy03',  'cathy03@email.com',  'pass_cathy',  1);

--  LOCATION

INSERT INTO location (branchName, address, contactInfo) VALUES
    ('Main Branch', 'Main Street, Dundalk', '042-000-0001');

--  CARS

INSERT INTO carDetails (regNumber, make, model, year, colour, mileage, fuelType, dailyRate, status) VALUES
         ('08-LH-1234', 'Toyota',    'Corolla',   2018, 'White',  85000, 'Petrol',   50.00, 'Available'),
         ('12-D-9876',  'Honda',     'Civic',     2020, 'Black',  42000, 'Diesel',   65.00, 'Available'),
         ('16-MH-2468', 'Ford',      'Focus',     2017, 'Blue',   91000, 'Petrol',   45.00, 'Available'),
         ('19-G-7788',  'Volkswagen','Golf',      2019, 'Grey',   60000, 'Diesel',   60.00, 'Available'),
         ('20-WH-3333', 'Hyundai',   'i30',       2020, 'Silver', 52000, 'Petrol',   55.00, 'Available'),
         ('21-KK-9090', 'Kia',       'Sportage',  2021, 'Red',    40000, 'Diesel',   80.00, 'Available'),
         ('18-D-5555',  'Nissan',    'Qashqai',   2018, 'White',  73000, 'Petrol',   75.00, 'Available'),
         ('17-L-1122',  'Renault',   'Clio',      2017, 'Yellow', 88000, 'Petrol',   40.00, 'Available'),
         ('22-RN-4545', 'Skoda',     'Octavia',   2022, 'Black',  25000, 'Diesel',   70.00, 'Available'),
         ('23-CE-7777', 'Tesla',     'Model 3',   2023, 'White',  12000, 'Electric', 95.00, 'Available'),
         ('15-DL-8080', 'BMW',       '320d',      2015, 'Blue',  110000,'Diesel',   85.00, 'Maintenance'),
         ('14-G-9099',  'Audi',      'A4',        2014, 'Grey',  130000,'Diesel',   80.00, 'Available'),
         ('19-D-1010',  'Peugeot',   '3008',      2019, 'Green',  58000, 'Diesel',   78.00, 'Available'),
         ('22-WW-2020', 'Mercedes',  'A180',      2022, 'Black',  19000, 'Petrol',   92.00, 'Available');

-- BOOKINGS + PAYMENT
-- Leave empty: the  system will insert these when booking/payment happens



-- INSERT INTO bookings (username, carID, pickUpDate, returnDate, locationID, totalCost, status)
-- VALUES ('alice01', 1, '2026-02-01', '2026-02-05', 1, 200.00, 'Booked');


-- INSERT INTO payment (bookingID, cardNumber, cvv, expiryDate)
-- VALUES (1, '4111111111111111', '123', '12/27');
