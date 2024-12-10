package owenwang.ems;

public abstract class EmployeeInfo {

    public static final String[] GENDERS = { "Male", "Female", "Other", "Prefer not to say"};

    public static final class Gender {
        public static final int MALE = 0;
        public static final int FEMALE = 1;
        public static final int OTHER = 2;
        public static final int PREFER_NOT_TO_SAY = 3;
    }

    
    // ATTRIBUTES
    private final int empNum;
    private String firstName;
    private String lastName;
    private int gender; // encode e.g. 0 for M, 1 for F, etc.
    private int workLoc; // encode e.g. 0 for Mississauga, etc.
    private double deductRate; // e.g. 0.21 for 21%
    
    
    // CONSTRUCTORS
    
    public EmployeeInfo(int eN, String fN, String lN, int g, int wL, double dR) {
    	empNum = eN;
    	setFirstName(fN);
    	setLastName(lN);
    	setGender(g);
    	setWorkLoc(wL);
    	setDeductRate(dR);
    }
    
    
    // METHODS
    
    public int getEmpNum() {
    	return empNum;
    }

    public int getGender() {
        return gender;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getWorkLoc() {
        return workLoc;
    }

    public void setWorkLoc(int workLoc) {
        this.workLoc = workLoc;
    }

    public void setDeductRate(double deductRate) {
        this.deductRate = deductRate;
    }

    public double getDeductRate() {
        return deductRate;
    }


    public String getFirstName() {
    	return firstName;
    }
    
    public String getLastName() {
    	return lastName;
    }
    

    public abstract double calcGrossAnnualIncome();

    public double calcNetAnnualIncome() {
        return calcGrossAnnualIncome() * (1d - getDeductRate());
    }
    
}
