package campussaver.service;

import campussaver.model.AdminUser;
import campussaver.model.StudentUser;
import campussaver.model.User;

/**
 * Manages all registered users (students and admins) using a plain array and counter.
 * Enforces validation rules, unique student names, and sequential ID generation.
 */
public class UserService {
    private static final String DEFAULT_CURRENCY = "MYR";

    private User[] users = new User[50];
    private int userCount = 0;
    private int nextStudentSeq = 1;
    private int nextAdminSeq = 1;

    /**
     * Registers a new student with an auto-generated ID (U001, U002, ...).
     * Rejects empty names, non-positive budgets, and duplicate student names.
     */
    public StudentUser registerStudent(String name, String livingType,
                                       boolean hasScholarship, long budgetCents) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Student name cannot be empty.");
        }
        if (budgetCents <= 0) {
            throw new IllegalArgumentException("Monthly budget must be greater than 0.");
        }

        String cleanName = name.trim();
        if (isDuplicateStudentName(cleanName)) {
            throw new IllegalArgumentException("A student with the name \"" + cleanName + "\" already exists.");
        }

        ensureCapacity();
        String id = String.format("U%03d", nextStudentSeq++);
        StudentUser student = new StudentUser(
                id, cleanName, DEFAULT_CURRENCY, livingType, hasScholarship, budgetCents
        );
        users[userCount++] = student;
        return student;
    }

    /**
     * Registers a new admin with an auto-generated ID (A001, A002, ...).
     */
    public AdminUser registerAdmin(String name, String department) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Admin name cannot be empty.");
        }
        String cleanDept = (department == null) ? "" : department.trim();

        ensureCapacity();
        String id = String.format("A%03d", nextAdminSeq++);
        AdminUser admin = new AdminUser(id, name.trim(), DEFAULT_CURRENCY, cleanDept);
        users[userCount++] = admin;
        return admin;
    }

    /**
     * Finds a user by exact ID (case-insensitive), or returns null if not found.
     */
    public User findUserById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        String target = id.trim();
        for (int i = 0; i < userCount; i++) {
            if (users[i] != null && users[i].getUserId().equalsIgnoreCase(target)) {
                return users[i];
            }
        }
        return null;
    }

    public User[] getUsers() {
        return users;
    }

    public int getUserCount() {
        return userCount;
    }

    /**
     * Updates the user count after FileService loads users from users.txt,
     * and advances ID counters so new registrations never collide with loaded IDs.
     */
    public void setLoadedCount(int count) {
        if (count < 0) {
            this.userCount = 0;
        } else if (count > users.length) {
            this.userCount = users.length;
        } else {
            this.userCount = count;
        }
        syncIdSequences();
    }

    /**
     * Checks whether any existing StudentUser already uses the given name (case-insensitive).
     */
    private boolean isDuplicateStudentName(String name) {
        for (int i = 0; i < userCount; i++) {
            if (users[i] instanceof StudentUser) {
                StudentUser existing = (StudentUser) users[i];
                if (existing.getName() != null && existing.getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Doubles the users array manually using a for loop when full.
     */
    private void ensureCapacity() {
        if (userCount < users.length) {
            return;
        }
        User[] bigger = new User[users.length * 2];
        for (int i = 0; i < userCount; i++) {
            bigger[i] = users[i];
        }
        users = bigger;
    }

    /**
     * Scans loaded users so nextStudentSeq and nextAdminSeq stay ahead of existing IDs.
     */
    private void syncIdSequences() {
        int maxStudent = 0;
        int maxAdmin = 0;
        for (int i = 0; i < userCount; i++) {
            if (users[i] == null || users[i].getUserId() == null) {
                continue;
            }
            String id = users[i].getUserId().trim().toUpperCase();
            if (id.length() > 1) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (id.startsWith("U") && num > maxStudent) {
                        maxStudent = num;
                    } else if (id.startsWith("A") && num > maxAdmin) {
                        maxAdmin = num;
                    }
                } catch (NumberFormatException ignored) {
                    // Skip non-standard IDs
                }
            }
        }
        nextStudentSeq = maxStudent + 1;
        nextAdminSeq = maxAdmin + 1;
    }
}