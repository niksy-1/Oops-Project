import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class StorageService {
    private final String filePath;

    public StorageService(String filePath) {
        this.filePath = filePath;
    }

    public void saveToLog(Ticket ticket, double fee, long exitTime) {
        String file = dailyLogPath(exitTime);
        long diffMs = exitTime - ticket.getEntryTime();
        String vehicleType = ticket.getVehicle().getType() == VehicleType.TWO_WHEELER ? "TwoWheeler" : "FourWheeler";

        String entry = "  {\n"
                + "    \"ticket_id\": \"" + ParkingUtils.jsonEscape(ticket.getTicketID()) + "\",\n"
                + "    \"plate\": \"" + ParkingUtils.jsonEscape(ticket.getVehicle().getLicensePlate()) + "\",\n"
                + "    \"vehicleType\": \"" + vehicleType + "\",\n"
                + "    \"entry_time\": \"" + ParkingUtils.jsonEscape(ParkingUtils.formatTime(ticket.getEntryTime())) + "\",\n"
                + "    \"exit_time\": \"" + ParkingUtils.jsonEscape(ParkingUtils.formatTime(exitTime)) + "\",\n"
                + "    \"time_spent\": \"" + ParkingUtils.jsonEscape(ParkingUtils.formatDuration(diffMs)) + "\",\n"
                + "    \"amount\": " + Math.round(fee) + "\n"
                + "  }";

        appendJsonObject(file, entry);
    }

    public void saveToLog(Ticket ticket, double fee) {
        saveToLog(ticket, fee, ParkingUtils.nowMs());
    }

    public void saveActiveSnapshot(Ticket ticket, long snapshotTime) {
        String file = dailyLogPath(snapshotTime);
        long diffMs = snapshotTime - ticket.getEntryTime();

        String entry = "  {\n"
                + "    \"ticket_id\": \"" + ParkingUtils.jsonEscape(ticket.getTicketID()) + "\",\n"
                + "    \"plate\": \"" + ParkingUtils.jsonEscape(ticket.getVehicle().getLicensePlate()) + "\",\n"
                + "    \"entry_time\": \"" + ParkingUtils.jsonEscape(ParkingUtils.formatTime(ticket.getEntryTime())) + "\",\n"
                + "    \"exit_time\": \"NA\",\n"
                + "    \"time_spent\": \"" + ParkingUtils.jsonEscape(ParkingUtils.formatDuration(diffMs)) + "\",\n"
                + "    \"amount\": 0,\n"
                + "    \"snapshot_time\": \"" + ParkingUtils.jsonEscape(ParkingUtils.formatTime(snapshotTime)) + "\"\n"
                + "  }";

        appendJsonObject(file, entry);
    }

    public void generateDailySummary() {
        String file = dailyLogPath(ParkingUtils.nowMs());
        Path path = Paths.get(file);
        if (!Files.exists(path)) {
            System.out.println("\nNo records found for today.");
            return;
        }

        int totalVehicles = 0;
        int twoWheelers = 0;
        int fourWheelers = 0;
        long totalRevenue = 0;

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.contains("\"vehicleType\": \"TwoWheeler\"")) {
                    twoWheelers++;
                }
                if (line.contains("\"vehicleType\": \"FourWheeler\"")) {
                    fourWheelers++;
                }
                if (line.contains("\"amount\":")) {
                    String amount = line.substring(line.indexOf("\"amount\":") + 9).trim().replace(",", "");
                    try {
                        totalRevenue += Long.parseLong(amount);
                        totalVehicles++;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Unable to read daily log: " + e.getMessage());
            return;
        }

        System.out.println("\n========= Daily Report =========");
        System.out.println("Total Vehicles Parked Today: " + totalVehicles);
        System.out.println("Two-Wheelers: " + twoWheelers);
        System.out.println("Four-Wheelers: " + fourWheelers);
        System.out.println("Total Revenue: Rs. " + totalRevenue);
        System.out.println("================================");
    }

    private String dailyLogPath(long timestamp) {
        LocalDate date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
        String fileName = String.format("log_%d_%02d_%02d.json", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        return Paths.get(filePath, fileName).toString();
    }

    private void appendJsonObject(String path, String jsonObject) {
        try {
            Files.createDirectories(Paths.get(filePath));
            Path file = Paths.get(path);

            String contents = Files.exists(file) ? Files.readString(file, StandardCharsets.UTF_8) : "";
            String output;
            if (contents.isBlank()) {
                output = "[\n" + jsonObject + "\n]\n";
            } else {
                int lastBracket = contents.lastIndexOf(']');
                if (lastBracket == -1) {
                    output = "[\n" + jsonObject + "\n]\n";
                } else {
                    String trimmed = contents.substring(0, lastBracket).stripTrailing();
                    if (trimmed.endsWith("[")) {
                        output = trimmed + "\n" + jsonObject + "\n]\n";
                    } else {
                        output = trimmed + ",\n" + jsonObject + "\n]\n";
                    }
                }
            }
            Files.writeString(file, output, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}