import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

public class FileHashMonitor {
    private static final String BASELINE_FILE = "baseline.txt";
    private static final String TARGET_FOLDER = "Files";

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nWhat would you like to do?");
        System.out.println("    A) Collect new Baseline?");
        System.out.println("    B) Begin monitoring files with saved Baseline?");
        System.out.print("\nPlease enter 'A' or 'B': ");
        String response = scanner.next().toUpperCase();
        scanner.close();

        if (response.equals("A")) {
            eraseBaselineIfExists();
            collectBaseline();
        } else if (response.equals("B")) {
            monitorFiles();
        } else {
            System.out.println("Invalid choice. Exiting.");
        }
    }

    // Method to calculate file hash using SHA-512
    private static String calculateFileHash(String filepath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] fileBytes = Files.readAllBytes(Paths.get(filepath));
        byte[] hashBytes = digest.digest(fileBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Method to delete the existing baseline file
    private static void eraseBaselineIfExists() throws IOException {
        File baseline = new File(BASELINE_FILE);
        if (baseline.exists()) {
            baseline.delete();
            System.out.println("Existing baseline deleted.");
        }
    }

    // Method to create a baseline of file hashes
    private static void collectBaseline() throws Exception {
        FileWriter writer = new FileWriter(BASELINE_FILE, true);
        File folder = new File(TARGET_FOLDER);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Target folder does not exist.");
            return;
        }
        
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            String hash = calculateFileHash(file.getAbsolutePath());
            writer.write(file.getAbsolutePath() + "|" + hash + "\n");
        }
        writer.close();
        System.out.println("Baseline collection completed.");
    }

    // Method to monitor files and detect changes
    private static void monitorFiles() throws Exception {
        Map<String, String> fileHashMap = new HashMap<>();

        // Load baseline file hashes into a dictionary
        File baseline = new File(BASELINE_FILE);
        if (!baseline.exists()) {
            System.out.println("Baseline file not found. Exiting.");
            return;
        }
        
        BufferedReader reader = new BufferedReader(new FileReader(BASELINE_FILE));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\\|");
            if (parts.length == 2) {
                fileHashMap.put(parts[0], parts[1]);
            }
        }
        reader.close();

        System.out.println("Monitoring files...");
        while (true) {
            Thread.sleep(1000);
            File folder = new File(TARGET_FOLDER);
            if (!folder.exists() || !folder.isDirectory()) continue;

            for (File file : Objects.requireNonNull(folder.listFiles())) {
                String hash = calculateFileHash(file.getAbsolutePath());
                
                if (!fileHashMap.containsKey(file.getAbsolutePath())) {
                    System.out.println(file.getAbsolutePath() + " has been created!");
                } else if (!fileHashMap.get(file.getAbsolutePath()).equals(hash)) {
                    System.out.println(file.getAbsolutePath() + " has changed!");
                }
            }

            // Check if any baseline files have been deleted
            for (String path : fileHashMap.keySet()) {
                if (!new File(path).exists()) {
                    System.out.println(path + " has been deleted!");
                }
            }
        }
    }
}
