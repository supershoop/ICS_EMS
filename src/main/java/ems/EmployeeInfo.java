package ems;

public abstract class EmployeeInfo {
    public static final class Gender {
        public static final int MALE = 0;
        public static final int FEMALE = 1;
        public static final int OTHER = 2;
        public static final int PREFER_NOT_TO_SAY = 3;
    }

    
    // ATTRIBUTES
    public int empNum;
    public String firstName;
    public String lastName;
    public int gender; // encode e.g. 0 for M, 1 for F, etc.
    public int workLoc; // encode e.g. 0 for Mississauga, etc.
    public double deductRate; // e.g. 0.21 for 21%
    
    
    // CONSTRUCTORS
    
    public EmployeeInfo(int eN, String fN, String lN, int g, int wL, double dR) {
    	empNum = eN;
    	firstName = fN;
    	lastName = lN;
    	gender = g;
    	workLoc = wL;
    	deductRate = dR;
    }
    
    
    // METHODS
    
    public int getEmpNum() {
    	return empNum;
    }

    public int getGender() {
        return gender;
    }

    public int getWorkLocation() {
        return workLoc;
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
        return calcGrossAnnualIncome() * (1d - deductRate);
    }
    
}
