### File Integrity Monitor (FIM) - Java Implementation

This Java program provides functionality to monitor files and detect changes such as modifications, deletions, and new file creations. Below is the breakdown of the code with the required functionalities.

#### **Project Overview:**
The program offers two main functions:
1. **Collect a new baseline** of file hashes.
2. **Monitor files using an existing baseline** and alert the user if any changes are detected.

---

### **Code Walkthrough**

#### **Main Method**
The `main` method presents a user interface where the user can:
- Choose to collect a new baseline (`A`).
- Begin monitoring files using an existing baseline (`B`).

It also handles invalid input and calls the respective methods based on the user’s choice.

```java
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
```

---

#### **File Hash Calculation**
The `calculateFileHash` method generates a SHA-512 hash for the given file. The method reads the file's contents, calculates the hash, and returns it as a string.

```java
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
```

---

#### **Baseline Management**
1. **Erase Existing Baseline**:
   The `eraseBaselineIfExists` method deletes the `baseline.txt` file if it already exists. This ensures that when collecting a new baseline, the old file does not interfere.
   
   ```java
   private static void eraseBaselineIfExists() throws IOException {
       File baseline = new File(BASELINE_FILE);
       if (baseline.exists()) {
           baseline.delete();
           System.out.println("Existing baseline deleted.");
       }
   }
   ```

2. **Collect New Baseline**:
   The `collectBaseline` method scans the target folder, calculates the SHA-512 hash for each file, and writes the file path and hash to `baseline.txt`.
   
   ```java
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
   ```

---

#### **File Monitoring**
The `monitorFiles` method continuously checks the target folder for changes in the files:
- **File Creation**: If a file exists in the folder but not in the baseline, it alerts the user that the file has been created.
- **File Modification**: If a file’s hash differs from the baseline, it alerts the user that the file has been modified.
- **File Deletion**: If a file listed in the baseline is deleted from the folder, the program alerts the user.
   
```java
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
```

---

### **Improvements & Enhancements**
- **Recursive File Monitoring**: To handle files in subdirectories, you can use `Files.walk(Paths.get(path))` to recursively monitor all files.
- **Error Handling**: Add proper error checks to handle cases such as the absence of the target folder or baseline file.
- **Notification System**: Implement notifications via email or SMS using libraries like JavaMail API or Twilio API.

---

This code provides a foundational **File Integrity Monitoring** system and can be enhanced for more complex monitoring requirements.
