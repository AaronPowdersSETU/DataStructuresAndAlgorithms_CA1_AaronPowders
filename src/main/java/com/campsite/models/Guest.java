package com.campsite.models;

import com.campsite.datastructures.MyLinkedList;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * A guest who can make bookings at any campsite.
 * The guest keeps a list of all their bookings (past and pending).
 */
public class Guest implements Serializable {

    private int id;
    private String name;
    private LocalDate dateOfBirth;
    private String address;
    private String phone;
    private String email;
    private String extraInfo;                 // any other useful notes
    private MyLinkedList<Booking> bookings;

    public Guest(int id, String name, LocalDate dateOfBirth, String address,
                 String phone, String email, String extraInfo) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.extraInfo = extraInfo;
        this.bookings = new MyLinkedList<>();
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
    }

    public boolean removeBooking(Booking booking) {
        return bookings.remove(booking);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getExtraInfo() { return extraInfo; }
    public MyLinkedList<Booking> getBookings() { return bookings; }

    @Override
    public String toString() {
        return id + ": " + name;
    }
}
