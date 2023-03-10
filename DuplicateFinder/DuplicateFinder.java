import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class DuplicateFinder {

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFolder = fileChooser.getSelectedFile();
                deleteDuplicates(selectedFolder);
            }
        });
    }

    public static void deleteDuplicates(File folder) {
        Map<String, File> uniqueFiles = new HashMap<>();
        Map<String, List<File>> duplicates = new HashMap<>();

        walk(folder, uniqueFiles, duplicates);

        if (duplicates.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No duplicate files found in the selected directory");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(duplicates.size()).append(" sets of duplicate files found:\n\n");
        for (List<File> files : duplicates.values()) {
            for (File file : files) {
                sb.append(file.getAbsolutePath()).append("\n");
            }
            sb.append("\n");
        }
        sb.append("Do you want to delete all duplicates?");
        int option = JOptionPane.showConfirmDialog(null, sb.toString(), "Duplicate files found", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            for (List<File> files : duplicates.values()) {
                for (File file : files) {
                    file.delete();
                }
            }
            JOptionPane.showMessageDialog(null, "All duplicate files have been deleted");
        } else {
            JOptionPane.showMessageDialog(null, "No files were deleted");
        }
    }

    private static void walk(File folder, Map<String, File> uniqueFiles, Map<String, List<File>> duplicates) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                walk(file, uniqueFiles, duplicates);
            } else {
                String fileHash = null;
                try {
                    fileHash = getFileHash(file);
                } catch (IOException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                if (fileHash != null) {
                    if (uniqueFiles.containsKey(fileHash)) {
                        duplicates.computeIfAbsent(fileHash, k -> new ArrayList<>()).add(file);
                    } else {
                        uniqueFiles.put(fileHash, file);
                    }
                }
            }
        }
    }

    private static String getFileHash(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
