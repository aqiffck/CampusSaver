package campussaver.util;

import java.util.Scanner;

/**
 * Safe console input helper. Always reads full lines via nextLine() to avoid
 * Scanner bugs.
 */
public class InputHelper {

    /** Reads an integer within [min, max], looping until valid. */
    public static int readInt(Scanner input, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = input.nextLine().trim();
            try {
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.println("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    /**
     * Reads a positive money amount (> 0 cents) using Money.parse(), looping until
     * valid.
     */
    public static long readMoney(Scanner input, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = input.nextLine();
            long cents = Money.parse(line);
            if (cents > 0) {
                return cents;
            }
            System.out.println("Invalid amount. Enter a positive value up to 9999999.99 (e.g., 12.50).");
        }
    }

    /** Reads a non-empty trimmed string, looping until valid. */
    public static String readNonEmpty(Scanner input, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = input.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("Input cannot be empty. Please try again.");
        }
    }

    /**
     * Reads Y/N (case-insensitive), returning true for Y/YES and false for N/NO.
     */
    public static boolean readYesNo(Scanner input, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = input.nextLine().trim().toLowerCase();
            if (line.equals("y") || line.equals("yes")) {
                return true;
            }
            if (line.equals("n") || line.equals("no")) {
                return false;
            }
            System.out.println("Please enter Y or N.");
        }
    }
}