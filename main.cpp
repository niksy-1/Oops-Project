#include <iostream>
#include <vector>
#include <unordered_map>
#include <string>
#include <chrono> //chrono is used to use the system time

using namespace std;
static long long nowMs() {
    using namespace std::chrono;
    return duration_cast<milliseconds>(system_clock::now().time_since_epoch()).count(); //To count how much time its been since the ticket started
                                                                                        // makes future computation easy.
}
class ParkingLot {
    //Ticket holds ticket details, ie, the id of the ticket specifically, the License plate of the ticket, the parking slot its in, and the amount of time its been in the parking.
    struct Ticket {
        string ticketId;
        string plate;
        int slotId;
        long long entryMs; //This needs to be in long long since we're storing time in milliseconds, so 64 bit number is needed
    };
    int nSlots; //total number of slots in the parking lot
    vector<bool> occupied;
    vector<string> slotPlate;
    unordered_map<string, Ticket> activeTickets; //creates a map for each ticket and each car number
    int nextTicketNo = 1; //Should be edited, technically issuing ticket numbers sequentially is bad cybersecurity practice.
public:
    ParkingLot(int slots) : nSlots(slots), occupied(slots, false), slotPlate(slots, "") {} //Constructor to actually create parking lots, and set the value of all to empty.
                                                                    // occupied(slots, false) means make a vector of length slots, and fill it with false (all free).
                                                                    // slotPlate initializes an empty vector for the License plate in each slot (needed for status)
    string park(const string& plate) { //const string& plate basically means that even though we're accessing plate directly and not through a copy, we can't modify it.
        int slot = -1;
        for (int i = 0; i < nSlots; i++) {
            if (!occupied[i]) { slot = i; break; }
        }
        // the for loop just finds the first free slot
        if (slot == -1) return ""; //the case when all slots are full
        occupied[slot] = true; //if there is an empty slot, it marks it as filled.
        //creates and stores a ticket for the car.
        Ticket t;
        t.ticketId = "T" + to_string(nextTicketNo++);
        t.plate = plate;
        t.slotId = slot;
        slotPlate[slot] = plate;
        t.entryMs = nowMs();
        activeTickets[t.ticketId] = t;
        return t.ticketId;
    }

    bool unpark(const string& ticketId) {     // Returns true if ticket existed and car was removed, false otherwise.
                                            //We use bool as the return type because we can return true if the operation is successful, and false if the operation is
                                            //not successful, We could use integer as our return type if we wanted more detailed error codes, ie return 404 if the id
                                            //is not found, or return 403 if the ID exists but couldn't remove it for some reason and so on.
        auto it = activeTickets.find(ticketId);
        if (it == activeTickets.end()) return false; // Ticket does not exist (invalid ID)
        // Free the slot that this ticket was occupying
        occupied[it->second.slotId] = false; //"second." means go to the second item in the map, which is the ticket struct and .slotId finds the data in the struct
        slotPlate[it->second.slotId] = "";
        activeTickets.erase(it);
        return true;
    }
    void status() const {
        int freeCount = 0;
        for (bool x : occupied) if (!x) freeCount++; //Just a compressed method to loop through the occupied matrix,

        cout << "Total slots: " << nSlots
             << " | Free: " << freeCount
             << " | Occupied: " << (nSlots - freeCount) << "\n";

        for (int i = 0; i < (int)slotPlate.size(); i++) { //We cast the .size method to int because it outputs an unsigned integer rather than an int type
            if (slotPlate[i] == "") { //Just checks whether or not the string is empty
                cout << "Slot " << i << ": Empty\n";
            } else {
            cout << "Slot " << i << ": " << slotPlate[i] << "\n";
            }
        }
    }
};

int main() {
    ParkingLot lot(2); //Should probably take input from the user in order to find out how large their parking lot is, or atleast hold 10 in a macro so that it's editable
    int choice = 0;

    while (choice != 4) {
        cout << "\n1) Park\n2) Unpark\n3) Status\n4) End\nChoice: ";
        cin >> choice;
        //Normal switch case menu based system.
        switch (choice) {
            case 1: {
                string plate;
                cout << "Enter License Plate: ";
                cin >> plate;
                string ticketId = lot.park(plate); //Uses the park method to write the license plate into a lot.
                if (ticketId == "") cout << "Parking full.\n";
                else cout << "Parked.\nTicket ID: " << ticketId << "\n";
                break;
            }
            case 2: {
                string ticketId;
                cout << "Enter Ticket ID: ";
                cin >> ticketId;
                if (lot.unpark(ticketId)) cout << "Unparked.\n";
                else cout << "Invalid ticket.\n";
                break;
            }
            case 3:
                lot.status();
                break;
            case 4:
                break;
            default:
                cout << "Invalid choice.\n";
        }
    }
}
