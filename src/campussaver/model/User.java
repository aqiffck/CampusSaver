package campussaver.model;

/** Abstract parent of every role (student, admin, sponsor). */
public abstract class User {
    private final String userId;
    private String name;
    private String baseCurrency;
    private String language = "EN"; // "EN" or "MS"

    public User(String userId, String name, String baseCurrency) {
        this.userId = userId;
        this.name = name;
        this.baseCurrency = baseCurrency;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) {
        this.language = "MS".equalsIgnoreCase(language) ? "MS" : "EN";
    }

    /** Each subclass returns its own role name: STUDENT, ADMIN or SPONSOR. */
    public abstract String getUserRole();
}