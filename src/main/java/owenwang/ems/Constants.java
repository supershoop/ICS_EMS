package owenwang.ems;


import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Constants {
    private Constants() {}
    public static final double MAX_INPUT_MONEY = 9_999_999_999.99;
    public static final double HOURS_PER_WEEK = 24 * 7;
    public static final double WEEKS_PER_YEAR = 52;

    public static final String[] WORK_LOCATIONS = {"Mississauga", "Toronto", "Ottawa", "Saskatoon"};

    public static final NumberFormat MONEY_FORMAT = new DecimalFormat("$#,##0.00");
    public static final NumberFormat PERCENTAGE_FORMAT = new DecimalFormat("#0.##'%'");

    public static final NumberFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.##");

}
