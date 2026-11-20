package src.campussaver.model;

public class StudentUser extends User {
    private String livingType; //"HOSTEL" or "COMMUTER"
    private boolean hasScholarship;
    private double monthlyBudgetLimit;

    public StudentUser(String userId, String name, String baseCurrency, String livingType, boolean hasScholarship, double monthlyBudgetLimit) {
        super(userId, name, baseCurrency);
        this.livingType = livingType;
        this.hasScholarship = hasScholarship;
        this.monthlyBudgetLimit = monthlyBudgetLimit;
    }

    public String getLivingType() { return livingType; }
    public boolean isHasScholarship() { return hasScholarship; }
    public double getMonthlyBudgetLimit() { return monthlyBudgetLimit; }
    public void setMonthlyBudgetLimit(double limit) { this.monthlyBudgetLimit = limit; }

    @Override
    public String getUserRole() {
        return "STUDENT";
    }
}
    
}
