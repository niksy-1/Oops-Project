#include <cctype>
#include <iostream>
#include "parkingLot.h"
#include "parkingUtils.h"

int main() {
    char testChoice = '\0';
    std::cout << "Test mode? (y/n): ";
    while (!(std::cin >> testChoice) || (std::tolower(static_cast<unsigned char>(testChoice)) != 'y'
        && std::tolower(static_cast<unsigned char>(testChoice)) != 'n')) {//Checking whether or not to run in test mode
        std::cin.clear();
        int ch;
        while ((ch = std::cin.get()) != '\n' && ch != EOF) {}
        std::cout << "Invalid. Enter y or n: ";
    }
    if (tolower(static_cast<unsigned char>(testChoice)) == 'y') {
        setTimeScale(300);
        std::cout << "Test mode enabled: 1 second = 5 minutes.\n";
    } else {
        setTimeScale(1);
    }
    int lotSize = 0;
    std::cout << "Enter number of parking slots: ";
    while (!(std::cin >> lotSize) || lotSize <= 0) {
        std::cin.clear();
        int ch;
        while ((ch = std::cin.get()) != '\n' && ch != EOF) {}
        std::cout << "Invalid. Enter a positive number of slots: ";
    }
    ParkingLot lot(lotSize);
    int choice = 0;

    while (choice != 6) {
        std::cout << "\n1) Park\n2) Unpark\n3) Status\n4) Ticket information for a specific license plate\n5) Dump active tickets\n6) End\nChoice: ";
        if (!(std::cin >> choice)) {//Adding this because it kept crashing when closed with ctrl + c
            std::cin.clear();
            std::cin.ignore(100000, '\n');
            break;
        }
        //Normal switch case menu based system.
        switch (choice) {
            case 1: {
                std::string plate;
                int typeInput;
                std::cout << "Enter License Plate: ";
                std::cin >> plate;
                plate = normalizePlate(plate);
                if (!isValidPlateFormat(plate)) {
                    std::cout << "Invalid plate format. Expected format: AA11AA1111\n";
                    break;
                }
                if (lot.hasActivePlate(plate)) {
                    std::cout << "That vehicle is already parked.\n";
                    break;
                }
                std::cout << "Enter Vehicle Type(1 for Two Wheeler, 2 for Four Wheeler): ";
                while (!(std::cin >> typeInput) || (typeInput != 1 && typeInput != 2)) {
                    std::cin.clear();
                    int ch;
                    while ((ch = std::cin.get()) != '\n' && ch != EOF) {} // discard bad line
                    std::cout << "Invalid. Enter 1 (Two Wheeler) or 2 (Four Wheeler): ";
                }
                VehicleType type = (typeInput == 1) ? VehicleType::TwoWheeler : VehicleType::FourWheeler;
                std::string ticketId = lot.park(plate, type); //Uses the park method to write the license plate and vehicle type into a lot.
                if (ticketId.empty()) std::cout << "Parking full.\n";
                else std::cout << "Parked.\nTicket ID: " << ticketId << "\n";
                break;
            }
            case 2: {
                std::string ticketId;
                std::cout << "Enter Ticket ID: ";
                std::cin >> ticketId;
                if (lot.unpark(ticketId)) std::cout << "Unparked.\n";
                else std::cout << "Invalid ticket.\n";
                break;
            }
            case 3:
                lot.status();
                break;
            case 4: {
                std::string plate;
                std::cout<<"Enter License Plate: ";
                std::cin>>plate;
                if(!lot.checkByPlate(plate)) std::cout<<"Plate not found.\n";
                break;
            }
            case 5:
                lot.dumpActiveTickets();
                break;
            case 6:
                break;
            default:
                std::cout << "Invalid choice.\n";
        }
        std::cout<<std::endl;
    }
}
