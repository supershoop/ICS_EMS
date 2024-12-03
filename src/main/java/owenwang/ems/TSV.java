package owenwang.ems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

// Tab-separated values
public final class TSV {
    public static void serialize(MyHashTable data, Writer out) {
        data.forEach(e -> {
            try {
                out.append(Integer.toString(e.empNum));
                out.append('\t');
                out.append(e.firstName);
                out.append('\t');
                out.append(e.lastName);
                out.append('\t');
                out.append(Integer.toString(e.gender));
                out.append('\t');
                out.append(Integer.toString(e.workLoc));
                out.append('\t');
                out.append(Double.toString(e.deductRate));
                out.append('\t');
                out.append(e instanceof FTE ? "FTE" : "PTE");
                out.append('\t');
                if (e instanceof FTE) {
                    out.append(Double.toString(((FTE) e).getYearlySalary()));
                    out.append('\n');
                } else if (e instanceof PTE) {
                    out.append(Double.toString(((PTE) e).getHourlyWage()));
                    out.append('\t');
                    out.append(Double.toString(((PTE) e).getHoursPerWeek()));
                    out.append('\t');
                    out.append(Double.toString(((PTE) e).getWeeksPerYear()));
                    out.append('\n');
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public static List<Integer> deserialize(BufferedReader input, MyHashTable data) {
        ArrayList<Integer> ids = new ArrayList<>();
        input.lines().forEach(line -> {
            try {
                var fields = line.split("\t");
                var empNum = Integer.parseInt(fields[0]);
                var fName = fields[1];
                var lName = fields[2];
                var gender = Integer.parseInt(fields[3]);
                var workLoc = Integer.parseInt(fields[4]);
                var deductRate = Double.parseDouble(fields[5]);
                EmployeeInfo e = null;
                if (fields[6].equals("FTE")) {
                    var salary = Double.parseDouble(fields[7]);
                    e = new FTE(empNum, fName, lName, gender, workLoc, deductRate, salary);
                } else if (fields[6].equals("PTE")) {
                    var hourlyWage = Double.parseDouble(fields[7]);
                    var hoursPerWeek = Double.parseDouble(fields[8]);
                    var weeksPerYear = Double.parseDouble(fields[9]);
                    e = new PTE(empNum, fName, lName, gender, workLoc, deductRate, hourlyWage, hoursPerWeek, weeksPerYear);
                }
                if (e != null && data.getFromTable(empNum) == null) {
                    data.addToTable(e);
                    ids.add(empNum);
                }
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Encountered incorrect number of fields.");
            }
        });
        return ids;
    }
}
