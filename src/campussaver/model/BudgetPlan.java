package campussaver.model;

/** Budget split as parallel arrays. Percents must total 100. */
public class BudgetPlan {
    public static final int MAX = 10;
    private final String[] categories = new String[MAX];
    private final int[] percents = new int[MAX];
    private int count = 0;

    public boolean add(String category, int percent) {
        if (count >= MAX || category == null || category.trim().isEmpty()) return false;
        if (percent < 0 || percent > 100) return false;
        categories[count] = category.trim();
        percents[count] = percent;
        count++;
        return true;
    }

    public String getCategory(int i) { return categories[i]; }
    public int getPercent(int i) { return percents[i]; }
    public int getCount() { return count; }
}