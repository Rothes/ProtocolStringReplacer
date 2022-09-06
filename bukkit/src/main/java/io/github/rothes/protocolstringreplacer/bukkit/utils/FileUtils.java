package io.github.rothes.protocolstringreplacer.bukkit.utils;

import org.apache.commons.lang.Validate;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static boolean createFile(@Nonnull File file) {
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

    @Nonnull
    public static List<File> getFolderFiles(@Nonnull File folder, boolean deep, String suffix) {
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

    public static boolean checkFileSuffix(@Nonnull File file, @Nonnull String suffix) {
        Validate.notNull(file, "File cannot be null");
        return checkFileSuffix(file.getName(), suffix);
    }

    public static boolean checkFileSuffix(@Nonnull String fileName, @Nonnull String suffix) {
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
