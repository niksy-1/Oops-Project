#include "parkingLot.h"
#include <fstream>
#include <iomanip>
#include <iostream>
#include <sstream>
#include <ctime>
#include <filesystem>
#include "parkingUtils.h"
ParkingLot::ParkingLot(int slots)
    : nSlots(slots), occupied(slots, false), slotPlate(slots, "") {} //Constructor to actually create parking lots, and set the value of all to empty.
                                                                    // occupied(slots, false) means make a vector of length slots, and fill it with false (all free).
                                                                    // slotPlate initializes an empty vector for the License plate in each slot (needed for status)

std::string ParkingLot::park(const std::string& plate, VehicleType type) { //const string& plate basically means that even though we're accessing plate directly and not through a copy, we can't modify it.
    if (hasActivePlate(plate)) return "";
    int slot = -1;
    for (int i = 0; i < nSlots; i++) {
        if (!occupied[i]) { slot = i; break; }
    }
    // the for loop just finds the first free slot
    if (slot == -1) return ""; //the case when all slots are full
    occupied[slot] = true; //if there is an empty slot, it marks it as filled.
    //creates and stores a ticket for the car.
    Ticket t;
    t.ticketId = "T" + std::to_string(nextTicketNo++);
    t.plate = plate;
    t.type = type;
    t.slotId = slot;
    slotPlate[slot] = plate;
    t.entryMs = nowMs();
    activeTickets[t.ticketId] = t;
    return t.ticketId;
}

bool ParkingLot::hasActivePlate(const std::string& plate) const {
    for (const auto& kv : activeTickets) {
        if (kv.second.plate == plate) {
            return true;
        }
    }
    return false;
}

int ParkingLot::computeBill(VehicleType t, long long h) const {
    if (t == VehicleType::FourWheeler) {
        if (h == 0) return 0;
        if (h == 1) return 100;
        if (h <= 3) return (100 + (h - 1) * 300);
        return (100 + 2 * 300 + (h - 3) * 500);
    }
    if (h == 0) return 0;
    if (h == 1) return 50;
    if (h <= 3) return (50 + (h - 1) * 100);
    return (50 + 2 * 100 + (h - 3) * 250);
}

bool ParkingLot::unpark(const std::string& ticketId) {     // Returns true if ticket existed and car was removed, false otherwise.
                                            //We use bool as the return type because we can return true if the operation is successful, and false if the operation is
                                            //not successful, We could use integer as our return type if we wanted more detailed error codes, ie return 404 if the id
                                            //is not found, or return 403 if the ID exists but couldn't remove it for some reason and so on.
    auto it = activeTickets.find(ticketId);
    if (it == activeTickets.end()) return false; // Ticket does not exist (invalid ID)
    Ticket t = it->second; //create a copy of ticket before we erase it
    long long exitMs = nowMs();
    long long diffMs = exitMs - t.entryMs;
    long long billableHours = diffMs / 3600000;  // floor hours
    long long fee = computeBill(t.type, billableHours);
    outputReceipt(t, exitMs, diffMs, billableHours, fee);
    logVisit(t, exitMs, diffMs, fee); //logs the ticket
    occupied[it->second.slotId] = false; // Free the slot that this ticket was occupying
                                            //"second." means go to the second item in the map, which is the ticket struct and .slotId finds the data in the struct
    slotPlate[it->second.slotId] = "";
    activeTickets.erase(it);
    return true;
}

void ParkingLot::status() const {
    int freeCount = 0;
    for (bool x : occupied) if (!x) freeCount++; //Just a compressed method to loop through the occupied matrix,

    std::cout << "Total slots: " << nSlots
              << " | Free: " << freeCount
              << " | Occupied: " << (nSlots - freeCount) << "\n";

    for (int i = 0; i < static_cast<int>(slotPlate.size()); i++) { //We cast the .size method to int because it outputs an unsigned integer rather than an int type
        if (slotPlate[i] == "") { //Just checks whether or not the string is empty
            std::cout << "Slot " << (i + 1) << ": Empty\n";
        } else {
            std::cout << "Slot " << (i + 1) << ": " << slotPlate[i] << "\n";
        }
    }
}

bool ParkingLot::checkByPlate(const std::string& plate) const {   //the second const here signifies that this function doesn't modify the parking lot object, just reads it
    for (const auto& kv : activeTickets) {  //The auto keyword here means that the compiler will decide the type of the variable kv(key value)
                                            //The for loop loops through every element in the map, and is a pair of <id> and <struct>, where
                                            //the struct is ticket. so kv.first refers to ticketId, and kv.second refers to the actual ticket struct.
                                            //Currently this only works for currently active cars, but once we have a json file for logging, we can update
                                            //this to look through the json file so we can do stuff like showing all the times a single car has actually entered
        const Ticket& t = kv.second;
        if (t.plate == plate) {
            long long diffMs = nowMs() - t.entryMs;
            long long totalSecs = diffMs / 1000;
            long long mins = totalSecs / 60;
            long long secs = totalSecs % 60;

            std::cout << "Ticket ID: " << t.ticketId << "\n";
            std::cout << "Slot: " << (t.slotId + 1) << "\n";
            std::cout << "Time parked: " << mins << " minute(s) " << secs << " second(s)\n" << std::endl;
            return true;
        }
    }
    return false;
}

void ParkingLot::outputReceipt(const Ticket& t, long long exitMs, long long diffMs, long long billableHours, long long fee) const {
    long long totalSecs = diffMs / 1000;
    long long mins = totalSecs / 60;
    long long secs = totalSecs % 60;

    std::cout << "\n===== RECEIPT =====\n";
    std::cout << "Ticket ID   : " << t.ticketId << "\n";
    std::cout << "Plate       : " << t.plate << "\n ";
    std::cout << "Type        : " << (t.type == VehicleType::TwoWheeler ? "2W" : "4W") << "\n";
    std::cout << "Slot        : " << (t.slotId + 1) << "\n";
    std::cout << "Time In     : " << formatTime(t.entryMs) << "\n";
    std::cout << "Time Out    : " << formatTime(exitMs) << "\n";
    std::cout << "Duration    : " << mins << " min " << secs << " sec\n";
    std::cout << "Billed Hours: " << billableHours << "\n";
    std::cout << "Amount      : Rs " << fee << "\n";
    std::cout << "===================\n"; //We could use iomanip here to output this without having to do this manually
}

void ParkingLot::logVisit(const Ticket& t, long long exitMs, long long diffMs, long long fee) const {
    std::filesystem::create_directories("logs");
    time_t tt = static_cast<time_t>(exitMs / 1000);
    tm* lt = localtime(&tt);
    std::ostringstream fileName;
    fileName << "logs/log_" << (lt->tm_year + 1900) << "_"
             << std::setfill('0') << std::setw(2) << (lt->tm_mon + 1) << "_"
             << lt->tm_mday << ".json";
    const std::string logFile = fileName.str(); //where logs are stored
    std::ostringstream entry; // Sort of like the StringBuffer in Java, you can write to it without outputting anything instantly
    entry << "  {\n"
          << "    \"plate\": \"" << jsonEscape(t.plate) << "\",\n"  //json escape is used to make sure stuff like " or / dont break the code,
                                                                  //it explicitly treats things as strings.
          << "    \"entry_time\": \"" << jsonEscape(formatTime(t.entryMs)) << "\",\n"   //Format time and duration convert from epoch time to something that is human readable
          << "    \"exit_time\": \"" << jsonEscape(formatTime(exitMs)) << "\",\n"
          << "    \"time_spent\": \"" << jsonEscape(formatDuration(diffMs)) << "\",\n"
          << "    \"amount\": " << fee << "\n"
          << "  }";

    std::ifstream in(logFile); //if a json file already exists, use that
    std::string contents;
    if (in) {
        std::ostringstream buffer;
        buffer << in.rdbuf(); //Copy the contents of the file that already exist to the buffer
        contents = buffer.str();
    }
    in.close(); //close the file, like fclose in c

    std::ofstream out(logFile, std::ios::trunc); //clears the file, and opens it to write a new version
    if (!out) {
        return;
    }
    if (contents.empty()) { //If the file is empty, it adds a new entry,
        out << "[\n" << entry.str() << "\n]\n";
        return;
    }
    size_t lastBracket = contents.find_last_of(']'); // otherwise, it finds the last ], and inputs the entry after that
    if (lastBracket == std::string::npos) {
        out << "[\n" << entry.str() << "\n]\n";
        return;
    }
    std::string trimmed = contents.substr(0, lastBracket);   // if it can't find a closing bracket, it assumes there's something wrong with the file and tries to overwrite
                                                             // might want to change this behaviour, since it could overwrite valid data
    while (!trimmed.empty() && (trimmed.back() == '\n' || trimmed.back() == '\r' || trimmed.back() == ' ' || trimmed.back() == '\t')) {
        trimmed.pop_back();
    }
    if (!trimmed.empty() && trimmed.back() == '[') {
        out << trimmed << "\n" << entry.str() << "\n]\n";
    } else {
        out << trimmed << ",\n" << entry.str() << "\n]\n";
    }
}
