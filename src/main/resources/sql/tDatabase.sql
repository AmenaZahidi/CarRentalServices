DROP DATABASE IF EXISTS tDatabase;
CREATE DATABASE IF NOT EXISTS tDatabase;
USE tDatabase;

-- USERS TABLE
DROP DATABASE IF EXISTS tDatabase;
CREATE DATABASE tDatabase;
USE tDatabase;

DROP TABLE IF EXISTS users;

CREATE TABLE users (
userId   INT AUTO_INCREMENT PRIMARY KEY,
username VARCHAR(50)  NOT NULL UNIQUE,
email    VARCHAR(255) NOT NULL UNIQUE,
password VARCHAR(255) NOT NULL,
userType INT NOT NULL DEFAULT 1
);


CREATE TABLE location (
locationId	INT AUTO_INCREMENT PRIMARY KEY,
branchName	VARCHAR(100) NOT NULL,
address		VARCHAR(255),
phoneNumber VARCHAR(15)
);
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
CREATE TABLE driverdetails (
driverId INT AUTO_INCREMENT PRIMARY KEY,
firstName VARCHAR(50) NOT NULL,
lastName VARCHAR(50) NOT NULL,
email VARCHAR(255) UNIQUE NOT NULL,
phoneNumber VARCHAR(15),
address TEXT,
licenseNumber VARCHAR(20) UNIQUE NOT NULL,
dateOfBirth DATE NOT NULL,
permitType ENUM('manual', 'automatic')
);

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


CREATE TABLE payment (
paymentId INT AUTO_INCREMENT PRIMARY KEY,
bookingId INT NOT NULL,
paymentDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
amount DECIMAL (10,2) NOT NULL,
FOREIGN KEY (bookingId) REFERENCES bookings(bookingId) ON DELETE CASCADE
);

