package owenwang.ems;

import java.util.Optional;

public class Util {
    private Util() {}

    public static double toDouble(String s) {
        try {
            return Double.parseDouble(s.strip());
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    public static Optional<Integer> toInt(String s) {
        try {
            return Optional.of(Integer.parseInt(s.strip()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
