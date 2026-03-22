DROP DATABASE IF EXISTS tDatabase;
CREATE DATABASE IF NOT EXISTS tDatabase;
USE tDatabase;

-- ADDRESSES TABLE
CREATE TABLE addresses (
addressId INT AUTO_INCREMENT PRIMARY KEY,
addressLine1 VARCHAR(255) NOT NULL,
addressLine2 VARCHAR(255),
city VARCHAR(100) NOT NULL,
county VARCHAR(100) NOT NULL,
postcode VARCHAR(20) NOT NULL,
country VARCHAR(100) NOT NULL
);

-- USERS TABLE
CREATE TABLE users (
userId   INT AUTO_INCREMENT PRIMARY KEY,
addressId   INT NOT NULL,
username VARCHAR(50)  NOT NULL UNIQUE,
email    VARCHAR(255) NOT NULL UNIQUE,
dateOfBirth DATE NOT NULL,
password VARCHAR(255) NOT NULL,
userType INT NOT NULL DEFAULT 1 CHECK (userType IN (1,2)),

FOREIGN KEY (addressId) REFERENCES addresses(addressId)
);

-- LOCATION TABLE
CREATE TABLE location (
locationId	INT AUTO_INCREMENT PRIMARY KEY,
branchName	VARCHAR(100) NOT NULL,
address		VARCHAR(255),
phoneNumber VARCHAR(15)
);

-- CAR DETAILS TABLE
CREATE TABLE carDetails (
carId INT AUTO_INCREMENT PRIMARY KEY,
regNumber VARCHAR(15) UNIQUE NOT NULL,
make VARCHAR(50),
model VARCHAR(50),
carYear INT(11),
colour VARCHAR(20),
mileage INT(11),
transmission ENUM('manual', 'automatic'),
currentStatus ENUM('available', 'rented', 'maintenance') DEFAULT 'available',
fuelType ENUM('petrol', 'diesel', 'electric', 'hybrid')
);

-- DRIVER DETAILS TABLE
CREATE TABLE driverdetails (
driverId INT AUTO_INCREMENT PRIMARY KEY,
addressId INT NOT NULL,
firstName VARCHAR(50) NOT NULL,
lastName VARCHAR(50) NOT NULL,
email VARCHAR(255) UNIQUE NOT NULL,
phoneNumber VARCHAR(15),
licenseNumber VARCHAR(20) UNIQUE NOT NULL,
dateOfBirth DATE NOT NULL,
permitType ENUM('manual', 'automatic'),

FOREIGN KEY (addressId) REFERENCES addresses(addressId)
);

-- BOOKINGS TABLE
CREATE TABLE bookings (
bookingId INT AUTO_INCREMENT PRIMARY KEY,
driverId INT NOT NULL,
userId INT NOT NULL,
carId INT NOT NULL,
pickupDatetime DATETIME NOT NULL,
returnDatetime DATETIME NOT NULL,
pickupLocationId INT,
totalPrice DECIMAL(10, 2),
status ENUM('confirmed', 'active', 'returned', 'cancelled') DEFAULT 'confirmed',

FOREIGN KEY (userId) REFERENCES users(userId),
FOREIGN KEY (carId) REFERENCES carDetails(carId),
FOREIGN KEY (driverId) REFERENCES driverdetails(driverId),
FOREIGN KEY (pickupLocationId) REFERENCES location(locationId)
);

-- PAYMENT TABLE
CREATE TABLE payment (
paymentId INT AUTO_INCREMENT PRIMARY KEY,
bookingId INT NOT NULL,
paymentDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
amount DECIMAL (10,2) NOT NULL,
FOREIGN KEY (bookingId) REFERENCES bookings(bookingId) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS return_inspections (
inspectionId INT AUTO_INCREMENT PRIMARY KEY,
bookingId INT NOT NULL,
inspectedByUserId INT NOT NULL,
actualReturnDate DATE NOT NULL,
returnedOnTime BOOLEAN NOT NULL DEFAULT 1,
damageFound BOOLEAN NOT NULL DEFAULT 0,
damageNotes VARCHAR(500),
mileageIn INT,
fuelLevel VARCHAR(30),
inspectedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (bookingId) REFERENCES bookings(bookingId),
FOREIGN KEY (inspectedByUserId) REFERENCES users(userId)
);



CREATE INDEX idx_payment_booking ON payment (bookingId);