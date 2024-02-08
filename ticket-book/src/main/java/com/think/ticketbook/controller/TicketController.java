
package com.think.ticketbook.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api")

public class TicketController {

    private final Map<String, Ticket> tickets = new HashMap<>();
    private final Map<String, User> users = new HashMap<>();


    @PostMapping("/purchase")
    public String purchaseTicket(@RequestParam String from,
                                 @RequestParam String to,
                                 @RequestParam String firstName,
                                 @RequestParam String lastName,
                                 @RequestParam String email,
                                 @RequestParam String section) {
        // Generate a unique user ID
        String userId = generateUserId(firstName, lastName, email);


        // Check if the section is valid
        if (!section.equalsIgnoreCase("A") && !section.equalsIgnoreCase("B")) {
            return "Invalid section. Please choose either 'A' or 'B'.";
        }

        // Check if the user already has a ticket
        if (tickets.containsKey(userId)) {
            return "User already has a ticket. Cannot purchase another.";
        }

        // Allocate a seat and store the ticket in memory
        String seat = allocateSeat(section);
        Ticket ticket = new Ticket(from, to, userId, 20.0, seat);
        tickets.put(userId, ticket);

        // Store the user details in memory
        User user = new User(firstName, lastName, email, seat, section);
        users.put(userId, user);

        return "Ticket purchased successfully. Seat: " + seat;
    }

   /* @GetMapping("/receipt/{firstName}")
    public String getReceiptDetailsByUserName(@PathVariable String firstName) {
        // Search for the user based on user name
        String userId = findUserIdByUserName(firstName);

        if (userId != null) {
            Ticket ticket = tickets.get(userId);
            return "Receipt details for user: " + firstName + " - " + ticket.toString();
        } else {
            return "User not found for receipt details: " + firstName;
        }
    }

    // Method to find user ID based on user name
    private String findUserIdByUserName(String firstName) {
        for (Map.Entry<String, User> entry : users.entrySet()) {
            if (entry.getValue().firstName.equalsIgnoreCase(firstName) ||
                    entry.getValue().lastName.equalsIgnoreCase(firstName)) {
                return entry.getKey(); // Return the user ID
            }
        }
        return null; // User not found
    }

    */

    @GetMapping("/receipt/{firstName}/{lastName}")
    public String viewUserDetails(@PathVariable String firstName, @PathVariable String lastName) {
        String userId = findUserIdByFLName(firstName, lastName);

        if (userId != null) {
            User user = users.get(userId);
            return "User details: " + user.toString();
        } else {
            return "User not found: " + firstName + lastName;
        }
    }

    private String findUserIdByFLName(String firstName, String lastName) {
        for (Map.Entry<String, User> entry : users.entrySet()) {
            System.out.println("Comparing: " + entry.getValue().firstName + " " + entry.getValue().lastName);
            if (entry.getValue().firstName.equalsIgnoreCase(firstName) &&
                    entry.getValue().lastName.equalsIgnoreCase(lastName)) {
                System.out.println("Match found. User ID: " + entry.getKey());
                return entry.getKey(); // Return the user ID
            }
        }
        System.out.println("User not found");
        return null; // User not found
    }

    @GetMapping("/users/{section}")
    public String getUsersAndSeats(@PathVariable String section) {
        StringBuilder result = new StringBuilder("Users and Seats in Section " + section + ":\n");

        for (Map.Entry<String, User> entry : users.entrySet()) {
            if (entry.getValue().section.equalsIgnoreCase(section)) {
                User user = entry.getValue();
                result.append("User: ")
                        .append("First Name: ").append(user.firstName).append(", ")
                        .append("Last Name: ").append(user.lastName).append(", ")
                        .append("Email: ").append(user.email).append(", ")
                        .append("Seat: ").append(user.seat).append(", ")
                        .append("Section: ").append(user.section)
                        .append("\n");
            }
        }

        return result.toString();
    }

    @PutMapping("/modify/{firstName}/{lastName}/{newSeat}")
    public String modifyUser(@PathVariable String firstName, @PathVariable String lastName, @PathVariable String newSeat) {
        String userId = findUserIdByName(firstName, lastName);

        if (userId != null) {
            Ticket modifiedTicket = tickets.get(userId);
            User modifiedUser = users.get(userId);

            if (modifiedTicket != null && modifiedUser != null) {
                // Update the seat for the user
                modifiedUser.seat = newSeat;

                return "User details modified successfully. New seat for " + firstName + " " + lastName + ": " + newSeat;
            } else {
                return "Unexpected error during modification.";
            }
        } else {
            return "User not found for modification: " + firstName + " " + lastName;
        }
    }

    // Method to find user ID based on first name and last name
    private String findUserIdByName(String firstName, String lastName) {
        for (Map.Entry<String, User> entry : users.entrySet()) {
            if (entry.getValue().firstName.equalsIgnoreCase(firstName) &&
                    entry.getValue().lastName.equalsIgnoreCase(lastName)) {
                return entry.getKey(); // Return the user ID
            }
        }
        return null; // User not found
    }


    @DeleteMapping("/remove/{firstName}/{lastName}")
    public String removeUserByUsername(@PathVariable String firstName, @PathVariable String lastName) {
        String userId = findUserIdByNameAndLastName(firstName, lastName);

        if (userId != null) {
            Ticket removedTicket = tickets.remove(userId);
            User removedUser = users.remove(userId);

            if (removedTicket != null && removedUser != null) {
                return "User removed successfully. Removed Ticket: " + removedTicket.toString();
            } else {
                return "Unexpected error during removal.";
            }
        } else {
            return "User not found for removal: " + firstName + " " + lastName;
        }
    }

    // Method to find user ID based on first name and last name

    private String findUserIdByNameAndLastName(String firstName, String lastName) {
        for (Map.Entry<String, User> entry : users.entrySet()) {
            System.out.println("Comparing: " + entry.getValue().firstName + " " + entry.getValue().lastName);
            if (entry.getValue().firstName.equalsIgnoreCase(firstName) &&
                    entry.getValue().lastName.equalsIgnoreCase(lastName)) {
                System.out.println("Match found. User ID: " + entry.getKey());
                return entry.getKey(); // Return the user ID
            }
        }
        System.out.println("User not found");
        return null; // User not found
    }

    private String generateUserId(String firstName, String lastName, String email) {
        // Simple example of generating a unique user ID
        return firstName.substring(0, 1) + lastName.substring(0, 1) + email.hashCode();
    }

    private String allocateSeat(String section) {
        // Simple seat allocation logic
        int sectionCapacity = 50; // Assuming each section can have 50 seats
        long currentSectionCount = users.values().stream().filter(user -> user.section.equalsIgnoreCase(section)).count();
        return section + (currentSectionCount + 1);
    }

    private static class Ticket {
        private String from;
        private String to;
        private String user;
        private double pricePaid;
        private String seat;

        public Ticket(String from, String to, String user, double pricePaid, String seat) {
            this.from = from;
            this.to = to;
            this.user = user;
            this.pricePaid = pricePaid;
            this.seat = seat;
        }

        @Override
        public String toString() {
            return "Ticket{" +
                    "from='" + from + '\'' +
                    ", to='" + to + '\'' +
                    ", user='" + user + '\'' +
                    ", pricePaid=" + pricePaid +
                    ", seat='" + seat + '\'' +
                    '}';
        }
    }

    private static class User {
        private String firstName;
        private String lastName;
        private String email;
        private String seat;
        private String section;

        public User(String firstName, String lastName, String email, String seat, String section) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.seat = seat;
            this.section = section;
        }

        @Override
        public String toString() {
            return "User{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", email='" + email + '\'' +
                    ", seat='" + seat + '\'' +
                    ", section='" + section + '\'' +
                    '}';
        }
    }

}
