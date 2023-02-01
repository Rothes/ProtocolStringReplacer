package me.rothes.protocolstringreplacer.utils;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static boolean createFile(@NotNull File file) {
        File fileParent = file.getParentFile();
        if(!fileParent.exists() && !fileParent.mkdirs()){
            return false;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void copyDirectoryOrFile(@NotNull File source, @NotNull File destination) throws IOException {
        if (!source.exists()) {
            return;
        }
        if (source.isDirectory()) {
            copyDirectory(source, destination);
        } else {
            copyFile(source, destination);
        }
    }

    public static void copyDirectory(@NotNull File sourceDirectory, @NotNull File destinationDirectory) throws IOException {
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdir();
        }
        for (String p : sourceDirectory.list()) {
            copyDirectoryOrFile(new File(sourceDirectory, p), new File(destinationDirectory, p));
        }
    }

    public static void copyFile(@NotNull File sourceFile, @NotNull File destinationFile) throws IOException {
        Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @NotNull
    public static List<File> getFolderFiles(@NotNull File folder, boolean deep, String suffix) {
        List<File> result = new ArrayList<>();
        if (folder.exists()) {
            File[] files = folder.listFiles();
            for (File file : files) {
                if (file.isFile() && (suffix == null || checkFileSuffix(file, suffix))) {
                    result.add(file);
                } else if (deep && file.isDirectory()) {
                    result.addAll(getFolderFiles(file, deep, suffix));
                }
            }
        }
        return result;
    }

    public static boolean checkFileSuffix(@NotNull File file, @NotNull String suffix) {
        Validate.notNull(file, "File cannot be null");
        return checkFileSuffix(file.getName(), suffix);
    }

    public static boolean checkFileSuffix(@NotNull String fileName, @NotNull String suffix) {
        Validate.notNull(fileName, "FileName cannot be null");
        int length = fileName.length();
        int suffixLength = suffix.length();
        if (length > suffixLength) {
            String sub = fileName.substring(length - suffixLength, length);
            return sub.equalsIgnoreCase(suffix);
        }
        return false;
    }

}
