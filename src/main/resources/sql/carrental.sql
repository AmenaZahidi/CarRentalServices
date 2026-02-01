DROP DATABASE IF EXISTS carrental;
CREATE DATABASE IF NOT EXISTS carrental;

USE carrental;

-- ============================================================
-- USERS
-- userType: 1 = customer, 2 = admin, 3 = staff
-- ============================================================
CREATE TABLE users
(
    username  VARCHAR(50)         NOT NULL,
    email     VARCHAR(255) UNIQUE NOT NULL,
    password  VARCHAR(255)        NOT NULL,
    userType  INT(1) NOT NULL DEFAULT 1 COMMENT '1 customer, 2 admin, 3 staff',
    PRIMARY KEY (username)
);

-- ============================================================
-- LOCATION
-- (you will insert only 1 location in dataset)
-- ============================================================
CREATE TABLE location
(
    locationID  INT AUTO_INCREMENT,
    branchName  VARCHAR(100) NOT NULL,
    address     VARCHAR(200),
    contactInfo VARCHAR(120),
    PRIMARY KEY (locationID)
);

-- ============================================================
-- CAR DETAILS
-- ============================================================
CREATE TABLE carDetails
(
    carID      INT AUTO_INCREMENT,
    regNumber  VARCHAR(40) NOT NULL UNIQUE,
    make       VARCHAR(60) NOT NULL,
    model      VARCHAR(60) NOT NULL,
    year       INT NOT NULL,
    colour     VARCHAR(40),
    mileage    INT,
    fuelType   VARCHAR(30),
    dailyRate  DOUBLE NOT NULL,
    status     VARCHAR(30) NOT NULL DEFAULT 'Available',
    PRIMARY KEY (carID)
);

-- ============================================================
-- BOOKINGS
-- (your app inserts rows here when someone books)
-- ============================================================
CREATE TABLE bookings
(
    bookingID   INT AUTO_INCREMENT,
    username    VARCHAR(50) NOT NULL,
    carID       INT NOT NULL,
    pickUpDate  DATE NOT NULL,
    returnDate  DATE NOT NULL,
    locationID  INT NOT NULL,
    totalCost   DOUBLE NOT NULL,
    status      VARCHAR(30) NOT NULL DEFAULT 'Booked',
    PRIMARY KEY (bookingID),

    FOREIGN KEY (username)  REFERENCES users (username) ON UPDATE CASCADE,
    FOREIGN KEY (carID)     REFERENCES carDetails (carID),
    FOREIGN KEY (locationID) REFERENCES location (locationID)
);

CREATE INDEX idx_bookings_car_dates ON bookings (carID, pickUpDate, returnDate);
CREATE INDEX idx_bookings_user      ON bookings (username);

-- ============================================================
-- PAYMENT
-- (your app inserts rows here after someone pays)
-- ============================================================
CREATE TABLE payment
(
    paymentID   INT AUTO_INCREMENT,
    bookingID   INT NOT NULL,
    cardNumber  VARCHAR(25) NOT NULL,
    cvv         VARCHAR(10) NOT NULL,
    expiryDate  VARCHAR(10) NOT NULL,
    PRIMARY KEY (paymentID),

    FOREIGN KEY (bookingID) REFERENCES bookings (bookingID)
);

CREATE INDEX idx_payment_booking ON payment (bookingID);
