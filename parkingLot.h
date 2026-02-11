#pragma once
#include <string>
#include <unordered_map>
#include <vector>
#include <fstream>
#include <sstream>
#include <ctime>
#include <iomanip>
#include <iostream>
enum class VehicleType { TwoWheeler, FourWheeler }; // We use an enum to standardize inputs, remove errors with typos and stuff
class ParkingLot {
public:
    void showDailyReport();
    explicit ParkingLot(int slots);

    std::string park(const std::string& plate, VehicleType type);
    bool hasActivePlate(const std::string& plate) const;
    bool unpark(const std::string& ticketId);
    void status() const;
    bool checkByPlate(const std::string& plate) const;
    void dumpActiveTickets() const;
private:
    //Ticket holds ticket details, ie, the id of the ticket specifically, the License plate of the ticket, the parking slot its in, and the amount of time its been in the parking.
    struct Ticket {
        std::string ticketId;
        std::string plate;
        int slotId;
        VehicleType type;
        long long entryMs; //This needs to be in long long since we're storing time in milliseconds, so 64 bit number is needed
    };
    void outputReceipt(const Ticket& t, long long exitMs, long long diffMs, long long billableHours, long long fee) const;
    void logVisit(const Ticket& t, long long exitMs, long long diffMs, long long fee) const;
    void logActiveSnapshot(const Ticket& t, long long snapshotMs, long long diffMs) const;
    int computeBill(VehicleType t, long long h) const;
    int nSlots; //total number of slots in the parking lot
    std::vector<bool> occupied;
    std::vector<std::string> slotPlate;
    std::unordered_map<std::string, Ticket> activeTickets; //creates a map for each ticket and each car number
    int nextTicketNo = 1; //Should be edited, technically issuing ticket numbers sequentially is bad cybersecurity practice.
};
