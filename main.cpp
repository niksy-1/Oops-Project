#include <iostream>
#include <vector>
#include <unordered_map>
#include <string>
#include <chrono> //chrono is used to use the system time
#include <iomanip>
#include <sstream>
#include <ctime>
#include <cctype>
#include <regex>

using namespace std;
enum class VehicleType {TwoWheeler, FourWheeler}; // We use an enum to standardize inputs, remove errors with typos and stuff
static long long nowMs() {
    using namespace std::chrono;
    return duration_cast<milliseconds>(system_clock::now().time_since_epoch()).count(); //To count how much time its been since the ticket started
                                                                                        // makes future computation easy.
}
static string formatTime(long long ms) {
    time_t tt = (time_t)(ms / 1000);
    tm* lt = localtime(&tt);
    ostringstream out;
    out << put_time(lt, "%Y-%m-%d %H:%M:%S");
    return out.str();
}
static string normalizePlate(string plate) { //used to normalize license plate input
    for (char& c : plate) {// looks through characters the plate string
        c = static_cast<char>(toupper(static_cast<unsigned char>(c)));//each character is converted to uppercase
    }
    return plate; //the uppercase string is returned
}
static bool isValidPlateFormat(const string& plate) {//used to check whether or license plate inputs are valid.
    static const regex plateRegex("^[A-Z]{2}[\\ -]{0,1}[0-9]{2}[\\ "
        "-]{0,1}[A-Z]{1,2}[\\ -]{0,1}[0-9]{4}$"); //Only god knows how regex works
    return regex_match(plate, plateRegex); //returns a boolean if it matches or not
}

class ParkingLot {
    //Ticket holds ticket details, ie, the id of the ticket specifically, the License plate of the ticket, the parking slot its in, and the amount of time its been in the parking.
    struct Ticket {
        string ticketId;
        string plate;
        int slotId;
        VehicleType type;
        long long entryMs; //This needs to be in long long since we're storing time in milliseconds, so 64 bit number is needed

    };
    int nSlots; //total number of slots in the parking lot
    vector<bool> occupied;
    vector<string> slotPlate;
    unordered_map<string, Ticket> activeTickets; //creates a map for each ticket and each car number
    int nextTicketNo = 1; //Should be edited, technically issuing ticket numbers sequentially is bad cybersecurity practice.
    void outputReceipt(const Ticket& t, long long exitMs, long long diffMs, long long billableHours, long long fee){
        long long totalSecs = diffMs / 1000;
        long long mins = totalSecs / 60;
        long long secs = totalSecs % 60;

        cout << "\n===== RECEIPT =====\n";
        cout << "Ticket ID   : " << t.ticketId << "\n";
        cout << "Plate       : " << t.plate << "\n";
        cout << "Type        : " << (t.type == VehicleType::TwoWheeler ? "2W" : "4W") << "\n";
        cout << "Slot        : " << (t.slotId+1)<< "\n";
        cout << "Time In     : " << formatTime(t.entryMs) << "\n";
        cout << "Time Out    : " << formatTime(exitMs) << "\n";
        cout << "Duration    : " << mins << " min " << secs << " sec\n";
        cout << "Billed Hours: " << billableHours << "\n";
        cout << "Amount      : Rs " << fee << "\n";
        cout << "===================\n"; //We could use iomanip here to output this without having to do this manually
    }
public:
    ParkingLot(int slots) : nSlots(slots), occupied(slots, false), slotPlate(slots, "") {} //Constructor to actually create parking lots, and set the value of all to empty.
                                                                    // occupied(slots, false) means make a vector of length slots, and fill it with false (all free).
                                                                    // slotPlate initializes an empty vector for the License plate in each slot (needed for status)
    string park(const string& plate, VehicleType type) { //const string& plate basically means that even though we're accessing plate directly and not through a copy, we can't modify it.
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
        t.ticketId = "T" + to_string(nextTicketNo++);
        t.plate = plate;
        t.type = type;
        t.slotId = slot;
        slotPlate[slot] = plate;
        t.entryMs = nowMs();
        activeTickets[t.ticketId] = t;
        return t.ticketId;
    }
    bool hasActivePlate(const string& plate) const {
        for (const auto& kv : activeTickets) {
            if (kv.second.plate == plate) {
                return true;
            }
        }
        return false;
    }
    int computeBill(VehicleType t, long long h) {
        if (t == VehicleType::FourWheeler) {
            if (h==0) return 0;
            if (h==1) return 100;
            if (h<=3) return (100+(h-1)*300);
            return (100+2*300+(h-3)*500);
        }
        else {
            if (h==0) return 0;
            if (h==1) return 50;
            if (h<=3) return (50+(h-1)*100);
            return (50+2*100+(h-3)*250);
        }
    }

    bool unpark(const string& ticketId) {     // Returns true if ticket existed and car was removed, false otherwise.
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
        occupied[it->second.slotId] = false; // Free the slot that this ticket was occupying
                                            //"second." means go to the second item in the map, which is the ticket struct and .slotId finds the data in the struct
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
                cout << "Slot " << (i+1) << ": Empty\n";
            } else {
            cout << "Slot " << (i+1) << ": " << slotPlate[i] << "\n";
            }
        }
    }
    bool checkByPlate(const string& plate) const{   //the second const here signifies that this function doesn't modify the parking lot object, just reads it
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

                cout << "Ticket ID: " << t.ticketId << "\n";
                cout << "Slot: " << (t.slotId+1) << "\n";
                cout << "Time parked: " << mins << " minute(s) " << secs << " second(s)\n" << endl;
                return true;
            }
        }
        return false;
    }
};

int main() {
    int lotSize = 0;
    cout << "Enter number of parking slots: ";
    while (!(cin >> lotSize) || lotSize <= 0) {
        cin.clear();
        int ch;
        while ((ch = cin.get()) != '\n' && ch != EOF) {}
        cout << "Invalid. Enter a positive number of slots: ";
    }
    ParkingLot lot(lotSize);
    int choice = 0;

    while (choice != 5) {
        cout << "\n1) Park\n2) Unpark\n3) Status\n4) Ticket information for a specific license plate\n5) End\nChoice: ";
        if (!(cin >> choice)) {//Adding this because it kept crashing when closed with ctrl + c
            cin.clear();
            cin.ignore(100000, '\n');
            break;
        }
        //Normal switch case menu based system.
        switch (choice) {
            case 1: {
                string plate;
                int typeInput;
                cout << "Enter License Plate: ";
                cin >> plate;
                plate = normalizePlate(plate);
                if (!isValidPlateFormat(plate)) {
                    cout << "Invalid plate format. Expected format: AA11AA1111\n";
                    break;
                }
                if (lot.hasActivePlate(plate)) {
                    cout << "That vehicle is already parked.\n";
                    break;
                }
                cout << "Enter Vehicle Type(1 for Two Wheeler, 2 for Four Wheeler): ";
                while (!(cin >> typeInput) || (typeInput != 1 && typeInput != 2)) {
                    cin.clear();
                    int ch;
                    while ((ch = cin.get()) != '\n' && ch != EOF) {} // discard bad line
                    cout << "Invalid. Enter 1 (Two Wheeler) or 2 (Four Wheeler): ";
                }
                VehicleType type = (typeInput == 1) ? VehicleType::TwoWheeler : VehicleType::FourWheeler;
                string ticketId = lot.park(plate, type); //Uses the park method to write the license plate and vehicle type into a lot.
                if (ticketId.empty()) cout << "Parking full.\n";
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
            case 4: {
                string plate;
                cout<<"Enter License Plate: ";
                cin>>plate;
                if(!lot.checkByPlate(plate)) cout<<"Plate not found.\n";
                break;
            }
            case 5:
                break;
            default:
                cout << "Invalid choice.\n";
        }
        cout<<endl;
    }
}
