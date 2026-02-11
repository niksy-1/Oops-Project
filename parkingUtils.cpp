#include "parkingUtils.h"
#include <chrono> //chrono is used to use the system time
#include <cctype>
#include <ctime>
#include <iomanip>
#include <regex>
#include <sstream>

namespace {
    long long timeScale = 1; //Adding this to scale time up further, 1 second = 5 minutes, just to make the payment system easier to test.
    long long realStartMs = 0;
    long long scaledStartMs = 0;
}

void setTimeScale(long long scale) {
    timeScale = scale;
    using namespace std::chrono;
    realStartMs = duration_cast<milliseconds>(system_clock::now().time_since_epoch()).count();
    scaledStartMs = realStartMs;
}

long long nowMs() {
    using namespace std::chrono;
    long long realNow = duration_cast<milliseconds>(system_clock::now().time_since_epoch()).count();
    if (timeScale == 1) {
        return realNow;
    }
    return scaledStartMs + (realNow - realStartMs) * timeScale; //Translates the epoch time to scaled time.
}

std::string formatTime(long long ms) {
    time_t tt = static_cast<time_t>(ms / 1000);
    tm* lt = localtime(&tt);
    std::ostringstream out;
    out << std::put_time(lt, "%Y-%m-%d %H:%M:%S");
    return out.str();
}

std::string normalizePlate(std::string plate) { //used to normalize license plate input
    for (char& c : plate) { // looks through characters the plate string
        c = static_cast<char>(std::toupper(static_cast<unsigned char>(c))); //each character is converted to uppercase
    }
    return plate; //the uppercase string is returned
}

bool isValidPlateFormat(const std::string& plate) { //used to check whether or license plate inputs are valid.
    static const std::regex plateRegex("^[A-Z]{2}[\\ -]{0,1}[0-9]{2}[\\ -]{0,1}[A-Z]{1,2}[\\ -]{0,1}[0-9]{4}$");
    //Only god knows how regex works
    return std::regex_match(plate, plateRegex); //returns a boolean if it matches or not
}

std::string formatDuration(long long ms) {
    long long totalSecs = ms / 1000;
    long long hours = totalSecs / 3600;
    long long mins = (totalSecs % 3600) / 60;
    long long secs = totalSecs % 60;
    std::ostringstream out;
    out << std::setfill('0') << std::setw(2) << hours << ":"
        << std::setw(2) << mins << ":"
        << std::setw(2) << secs;
    return out.str();
}

std::string jsonEscape(const std::string& input) { // used to insure that stuff doesn't break when special characters are used
    std::ostringstream out; //looks through the ostream output
    for (char c : input) { //loops through the input into the function
        switch (c) {
            case '\\': out << "\\\\"; break; //whenever it encounters a special character, it adds an escape character before it to turn it into a normal word
            case '"': out << "\\\""; break;
            case '\n': out << "\\n"; break;
            case '\r': out << "\\r"; break;
            case '\t': out << "\\t"; break;
            default: out << c; break;
        }
    }
    return out.str();
}
