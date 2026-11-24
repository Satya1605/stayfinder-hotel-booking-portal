# Stayfinder – Project Design and Technical Details

## 1. Introduction

Stayfinder is a hotel booking portal developed as part of my full-stack development interview assignment.

The main purpose of this application is to allow users to search for hotels, check room availability, view prices and book rooms online. Users can also find their existing bookings and cancel them when required.

While developing this project, I focused on keeping the application simple to use and making sure that important operations, such as checking availability and creating bookings, are handled by the backend.

The application is divided into three main parts:

- Frontend: React
- Backend: Java with Spring Boot
- Database: MySQL

## 2. Application Architecture

The application follows a simple frontend-backend-database architecture.

The frontend communicates with the backend using REST APIs. The backend processes requests, applies the booking rules and communicates with the MySQL database.

### How the application works

1. The user opens the Stayfinder website in a browser.
2. The frontend sends requests to the Spring Boot backend.
3. The backend receives the request through the appropriate controller.
4. The service layer processes the required operation.
5. The repository layer communicates with the MySQL database.
6. The backend sends the response to the frontend.
7. The frontend displays the result to the user.

### Architecture diagram

```text
              USER
                |
                v
        REACT FRONTEND
        (Vite - Port 5173)
                |
                | REST API Requests
                v
       SPRING BOOT BACKEND
         (Port 8080)
                |
                v
          CONTROLLERS
                |
                v
           SERVICES
       (Business Logic)
                |
                v
          REPOSITORIES
                |
                v
            MYSQL 8
          (Database)
```

### Technologies used

| Component | Technology | Purpose |
|---|---|---|
| Frontend | React 18 | User interface |
| Build tool | Vite | Runs the frontend development server |
| Backend | Java 17 | Application logic |
| Framework | Spring Boot 3.3 | Backend development |
| Database | MySQL 8 | Stores hotels and bookings |
| ORM | Spring Data JPA | Database operations |
| Testing | JUnit 5 and Mockito | Backend unit testing |

**Frontend note:** The assignment specified Angular, but I used React with Vite because I am more familiar with React. The backend is independent of the frontend and communicates through REST APIs.

## 3. Database Design

MySQL is used to store hotel information, room availability and booking details.

The database name used in the project is `hotel_portal`.

The database contains four main tables.

### 3.1 Hotels Table

This table stores information about the hotels displayed on the website.

It includes details such as:

- Hotel ID
- Hotel name
- City
- Description
- Star rating
- Price per night
- Total number of rooms

Each hotel has a unique ID.

### 3.2 Hotel Amenities Table

This table stores the facilities available at each hotel.

Examples include Wi-Fi, swimming pool, parking and restaurant facilities.

One hotel can have multiple amenities. The hotel ID connects the amenities to the corresponding hotel.

### 3.3 Room Availability Table

This table keeps track of the number of rooms booked for each hotel on each date.

It stores:

- Hotel ID
- Stay date
- Number of booked rooms

For example, if a hotel has 5 rooms and 2 rooms are already booked for a particular night, then 3 rooms are still available.

If there is no record for a particular date, it means that no rooms have been booked for that date.

This table helps the application check availability before confirming a booking.

### 3.4 Bookings Table

This table stores the details of every booking made through the application.

It includes:

- Booking ID
- Booking reference
- Hotel ID
- Guest name
- Guest email
- Check-in date
- Check-out date
- Number of nights
- Total booking amount
- Booking status

Every booking receives a unique reference number.

The booking status can be either `CONFIRMED` or `CANCELLED`.

### Database relationships

- One hotel can have multiple amenities.
- One hotel can have multiple availability records.
- One hotel can have multiple bookings.
- Each booking belongs to one hotel.

The SQL scripts used to create the tables and insert sample hotel data are available in the backend resources folder.

## 4. Backend Structure

The backend is developed using Java and Spring Boot.

I divided the backend into different packages so that each part has a separate responsibility.

### Controller

The controller receives requests from the frontend and sends responses.

For example, when a user searches for hotels, the hotel controller receives the request and passes it to the service layer.

### Service

The service layer contains the main application logic.

It handles operations such as:

- Checking hotel availability.
- Calculating the booking amount.
- Validating booking dates.
- Creating bookings.
- Cancelling bookings.
- Finding alternative dates.

### Repository

The repository layer is responsible for communicating with the database.

It uses Spring Data JPA to perform database operations.

### Model

The model package contains the Java classes that represent database entities, such as hotels and bookings.

### DTO

DTO stands for Data Transfer Object.

These objects are used to transfer information between the frontend and backend without directly exposing database entities.

### Exception

This package handles application errors and validation problems.

The global exception handler returns errors in a consistent JSON format so that the frontend can display appropriate messages.

## 5. REST API Design

The frontend communicates with the backend through REST APIs.

The main endpoints used in the project are:

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/hotels` | Display and search hotels |
| GET | `/api/hotels/{id}` | Display details of a particular hotel |
| GET | `/api/hotels/{id}/alternatives` | Find nearby available dates |
| POST | `/api/bookings` | Create a new booking |
| GET | `/api/bookings/{reference}` | Retrieve booking details |
| POST | `/api/bookings/{reference}/cancel` | Cancel a booking |

### Error handling

The backend returns suitable HTTP status codes depending on the situation.

Some examples are:

- 400 – Invalid input or validation error.
- 404 – Hotel or booking not found.
- 409 – Room unavailable or booking conflict.
- 500 – Unexpected server error.

This helps the frontend understand what happened and show a meaningful message to the user.

## 6. Booking Process

The booking process is one of the most important parts of this project.

I have divided it into the following steps.

### Step 1: Select hotel and dates

The user searches for a hotel and selects check-in and check-out dates.

The backend checks whether the selected dates are valid.

The check-in date cannot be in the past, and the check-out date must be after the check-in date.

The maximum stay allowed is 30 nights.

### Step 2: Check room availability

The backend checks the availability of rooms for every night of the selected stay.

A room is considered occupied from the check-in date up to, but not including, the check-out date.

For example, if one guest checks out on 15 December, another guest can check in on 15 December.

This avoids treating the checkout date as an extra occupied night.

### Step 3: Calculate the price

The total booking amount is calculated by the backend.

The calculation is:

**Total Amount = Price Per Night × Number of Nights**

For example:

If the price of a room is ₹3,000 per night and the user selects 3 nights:

Total Amount = ₹3,000 × 3

Total Amount = ₹9,000

The backend uses `BigDecimal` for money calculations to avoid common floating-point precision problems.

### Step 4: Create the booking

After checking availability, the backend creates the booking.

The guest name, email, hotel, dates, number of nights and total amount are stored in the database.

A unique booking reference is generated, such as:

`HB-7K3M9QXA`

The booking is saved with the status `CONFIRMED`.

### Step 5: Display confirmation

The backend returns the booking details and reference number.

The frontend displays the confirmation to the user.

The user can use the reference number and email address later to retrieve the booking.

## 7. Booking Cancellation

Users can cancel their bookings through the booking lookup section.

The process works as follows:

1. The user enters the booking reference and email address.
2. The backend checks whether both details match.
3. It checks whether the booking has already been cancelled.
4. It checks whether the check-in date has passed.
5. If cancellation is allowed, the booking status is changed to `CANCELLED`.
6. The booked room count is reduced for every night of that stay.

After cancellation, the room becomes available again for those dates.

The booking record is not deleted. Keeping the record helps maintain the booking history.

## 8. Handling Simultaneous Bookings

One important problem in hotel booking systems occurs when two users try to book the last available room at the same time.

For example:

- A hotel has only one room remaining.
- User A checks availability.
- User B also checks availability before User A completes the booking.
- Both users see one room available.

Without proper handling, both users might successfully book the same room.

To avoid this problem, the project uses database transactions and pessimistic locking on the hotel record during booking.

The booking process works as follows:

1. A booking request starts a database transaction.
2. The backend locks the relevant hotel record.
3. It checks the availability of rooms.
4. If a room is available, it updates the availability and saves the booking.
5. The transaction is committed.
6. Another booking request for the same hotel must wait until the first transaction finishes.
7. When the second request continues, it checks the updated availability.

If no room is available, the backend rejects the booking instead of allowing an overbooking.

The database also has constraints to prevent duplicate availability records for the same hotel and date and to prevent negative booked-room counts.

This approach helps maintain correct room availability when multiple users try to book at the same time.

## 9. Surprise Feature – Nearby Free Dates

I added a feature called **Nearby Free Dates** to make the booking process more useful.

Normally, when a user selects dates for which no rooms are available, the user has to manually change the dates and search again.

This feature provides another option.

### How it works

1. The user selects a hotel and travel dates.
2. The backend checks room availability.
3. If no room is available, the frontend requests alternative dates.
4. The backend checks nearby dates within a 14-day range.
5. It keeps the same number of nights as the original selection.
6. It finds up to three alternative date ranges with available rooms.
7. The frontend displays the suggestions to the user.
8. The user can select an alternative and continue with the booking.

The search checks earlier dates first and then later dates, giving preference to closer options.

The application does not suggest dates in the past.

### Why I added this feature

I wanted to avoid showing users a dead end when their selected dates are unavailable.

Some travellers have flexibility in their travel dates. Showing nearby available dates can help them find another suitable option without repeating the entire search.

## 10. Important Design Decisions

During development, I made the following decisions:

| Decision | Reason |
|---|---|
| React for frontend | I am more familiar with React than Angular |
| Spring Boot for backend | It provides a structured way to develop REST APIs |
| MySQL for database | It stores hotel, availability and booking information |
| Separate service and repository layers | Makes the code easier to organise and maintain |
| Backend price calculation | Prevents the frontend from deciding the final booking amount |
| Separate room availability table | Makes it easier to track booked rooms for individual dates |
| Keep cancelled bookings | Maintains a record instead of permanently deleting bookings |
| Booking reference and email for lookup | Provides a simple way to access a booking without implementing user accounts |

## 11. Testing

The backend contains 19 unit tests written using JUnit 5 and Mockito.

The tests cover important application logic, including:

- Price calculation.
- Date validation.
- Room availability.
- Back-to-back bookings.
- Last-room availability cases.
- Booking cancellation.
- Nearby free dates suggestions.

I also tested the main application flow manually through the website.

The manual testing included:

1. Searching for hotels.
2. Selecting dates and checking availability.
3. Creating a booking.
4. Retrieving the booking using its reference and email.
5. Cancelling the booking.
6. Checking whether the room became available again.

## 12. Limitations and Future Improvements

The current version covers the main hotel booking requirements, but there are some features that could be added in the future.

- **Angular frontend:** The assignment requested Angular, but the current frontend uses React.
- **User authentication:** Users do not have separate accounts in this version.
- **Online payments:** The application displays the booking amount, but payment processing is not implemented.
- **Email confirmation:** Booking confirmation emails are not sent.
- **Frontend testing:** Automated React UI tests are not included.
- **Integration testing:** Tests with a real MySQL database and multiple simultaneous booking requests could be added.
- **Multiple room categories:** The current application supports one room category per hotel and one room per booking.
- **Search improvements:** Pagination and more advanced filters could be added if the number of hotels increases.

These improvements could make the application more suitable for a real hotel booking platform.

## 13. Conclusion

Stayfinder helped me understand how the different parts of a full-stack application work together.

While developing it, I worked with React, Java, Spring Boot, MySQL, REST APIs and database operations.

I also worked on important booking-related problems such as date validation, room availability, price calculation, booking cancellation and handling simultaneous booking requests.

The project gave me practical experience in connecting a frontend with a backend, storing information in a database and implementing business logic for a real-world use case.