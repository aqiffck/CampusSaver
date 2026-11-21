package campussaver.model;

/** A student who tracks spending. Budget is in cents. */
public class StudentUser extends User {
    private String livingType; // "HOSTEL" or "COMMUTER"
    private boolean hasScholarship;
    private long monthlyBudgetCents;
    private String sponsorId = ""; // linked sponsor, empty if none

    public StudentUser(String userId, String name, String baseCurrency,
                       String livingType, boolean hasScholarship, long monthlyBudgetCents) {
        super(userId, name, baseCurrency);
        this.livingType = livingType;
        this.hasScholarship = hasScholarship;
        this.monthlyBudgetCents = monthlyBudgetCents;
    }

    public String getLivingType() { return livingType; }
    public boolean hasScholarship() { return hasScholarship; }
    public long getMonthlyBudgetCents() { return monthlyBudgetCents; }
    public void setMonthlyBudgetCents(long cents) { this.monthlyBudgetCents = cents; }
    public String getSponsorId() { return sponsorId; }
    public void setSponsorId(String sponsorId) { this.sponsorId = sponsorId == null ? "" : sponsorId; }

    @Override
    public String getUserRole() { return "STUDENT"; }
}