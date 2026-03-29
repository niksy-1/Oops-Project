import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public final class ParkingUtils {
    private static final Pattern PLATE_REGEX = Pattern.compile("^[A-Z]{2}[\\ -]{0,1}[0-9]{2}[\\ -]{0,1}[A-Z]{1,2}[\\ -]{0,1}[0-9]{4}$");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static long timeScale = 1;
    private static long realStartMs = 0;
    private static long scaledStartMs = 0;

    private ParkingUtils() {}

    public static void setTimeScale(long scale) {
        timeScale = scale;
        realStartMs = System.currentTimeMillis();
        scaledStartMs = realStartMs;
    }

    public static long nowMs() {
        long realNow = System.currentTimeMillis();
        if (timeScale == 1) {
            return realNow;
        }
        return scaledStartMs + (realNow - realStartMs) * timeScale;
    }

    public static String formatTime(long ms) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault());
        return dateTime.format(DATE_TIME_FMT);
    }

    public static String formatDuration(long ms) {
        long totalSecs = ms / 1000;
        long hours = totalSecs / 3600;
        long mins = (totalSecs % 3600) / 60;
        long secs = totalSecs % 60;
        return String.format("%02d:%02d:%02d", hours, mins, secs);
    }

    public static String normalizePlate(String plate) {
        return plate.toUpperCase();
    }

    public static boolean isValidPlateFormat(String plate) {
        return PLATE_REGEX.matcher(plate).matches();
    }

    public static String jsonEscape(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
