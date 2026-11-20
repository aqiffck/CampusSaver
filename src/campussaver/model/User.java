package campussaver.model;

public abstract class User {
    private String userId;
    private String name;
    private String baseCurrency;

    public User(String userId, String name, String baseCurrency) {
        this.userId = userId;
        this.name = name;
        this.baseCurrency = baseCurrency;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }

    public abstract String getUserRole();
}