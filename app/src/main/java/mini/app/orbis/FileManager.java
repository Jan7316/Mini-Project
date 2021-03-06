package mini.app.orbis;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Jan on 17/11/2016.
 */

public class FileManager {

    private static File[] references;
    private static File[] orbisFolderFiles;

    public static void addReferences(Context context, File... files) {
        ArrayList<File> referencesList = new ArrayList<>();
        for(File file : references) {
            referencesList.add(file);
        }
        for(File file : files) {
            if(!referencesList.contains(file)) {
                referencesList.add(file);
            }
        }
        references = toFileArray(referencesList.toArray());
        saveCurrentFileReferences(context);
    }

    public static void removeReferences(Context context, File... files) {
        ArrayList<File> referencesList = new ArrayList<>();
        for(File file : references) {
            referencesList.add(file);
        }
        for(File file : files) {
            referencesList.remove(file);
        }
        references = toFileArray(referencesList.toArray());
        saveCurrentFileReferences(context);
    }

    public static void deleteOrbisFolderImage(File file) {
        file.delete();
    }

    public static void deleteItems(Context context, int[] indices) {
        File[] filesToBeDeleted = new File[indices.length];
        File[] allFiles = getFiles(context);
        int iteratedTrough = 0;
        for(int index : indices) {
            filesToBeDeleted[iteratedTrough] = allFiles[index];
            iteratedTrough++;
        }
        for(File file : filesToBeDeleted) {
            if(Util.contains(getOrbisFolderFiles(context), file)) {
                deleteOrbisFolderImage(file);
            } if(Util.contains(getReferencedFiles(context), file)) {
                removeReferences(context, file);
            }
        }
        updateFolderFiles(context);
    }

    public static void updateFolderFiles(Context context) {
        initialiseFolderFiles(context);
    }

    public static void saveCurrentFileReferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(GlobalVars.FILE_REFERENCES_PREFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("references", arrayToCSV(references));
        editor.apply();
    }

    private static String arrayToCSV(@NonNull File[] array) {
        StringBuilder stringBuilder = new StringBuilder("");
        for(int i=0;i<array.length;i++) {
            stringBuilder.append(array[i].getAbsolutePath());
            if(i < (array.length - 1)) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }

    public static File[] getFiles(Context context) {
        File[] referenced = getReferencedFiles(context);
        File[] orbisFolder = getOrbisFolderFiles(context);
        File[] combined = new File[referenced.length + orbisFolder.length];
        System.arraycopy(orbisFolder, 0, combined, 0, orbisFolder.length);
        System.arraycopy(referenced, 0, combined, orbisFolder.length, referenced.length);
        Arrays.sort(combined, new Comparator<File>() {
            @Override
            public int compare(File a, File b) { // Sort the files so that newer files (higher number of lastModified) are at the front of the array
                long alm = a.lastModified();
                long blm = b.lastModified();
                return alm < blm ? 1 : (alm > blm ? -1 : (a.getName().compareTo(b.getName())));
            }
        });
        return combined;
    }

    public static File[] getOrbisFolderFiles(Context context) {
        if(orbisFolderFiles == null) {
            initialise(context);
        }
        return orbisFolderFiles;
    }

    public static File[] getReferencedFiles(Context context) {
        if(references == null) {
            initialise(context);
        }
        return references;
    }

    public static void initialise(Context context) { // TODO upon initialisation, check whether the files exist and remove them if they do not
        SharedPreferences sharedPreferences = context.getSharedPreferences(GlobalVars.FILE_REFERENCES_PREFERENCE_FILE, Context.MODE_PRIVATE);
        String strCSV = sharedPreferences.getString("references","");
        csvToArray(strCSV);

        initialiseFolderFiles(context);
    }

    private static void initialiseFolderFiles(Context context) {
        String path = Environment.getExternalStorageDirectory().toString() + "/Orbis";
        File directory = new File(path);
        if(directory.exists()) {
            File[] files = directory.listFiles();
            if(files == null) {
                orbisFolderFiles = new File[0];
            } else if(files.length == 0) {
                orbisFolderFiles = new File[0];
            } else {
                ArrayList<File> imageFiles = new ArrayList<>();
                for(File file : files) {
                    if (isImageExtension(file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".") + 1, file.getAbsolutePath().length()))) {
                        imageFiles.add(file);
                    }
                }
                orbisFolderFiles = toFileArray(imageFiles.toArray());
                // TODO note: the entire selectedItems handling will remain in the gallery activity
            }
        } else {
            directory.mkdir();
            orbisFolderFiles = new File[0];
        }
    }

    private static void csvToArray(@NonNull String csv) {
        if(csv.length() == 0) {
            references = new File[0];
        } else if(csv.contains(",")) {
            String[] parts = csv.split(",");
            references = new File[parts.length];
            for(int i=0;i<parts.length;i++) {
                references[i] = new File(parts[i]);
            }
            checkReferences();
        } else {
            references = new File[1];
            references[0] = new File(csv);
            checkReferences();
        }
    }

    /**
     * Check all file references to remove those that point to non-existent files
     */
    private static void checkReferences() {
        ArrayList<File> fileList = new ArrayList<>(Arrays.asList(references));
        for(int i=0;i<fileList.size();i++) {
            if(!fileList.get(i).exists()) {
               fileList.remove(i);
            }
        }
        references = toFileArray(fileList.toArray());
    }

    private static boolean isImageExtension(String ext) {
        for(String e : GlobalVars.ACCEPTED_FILE_FORMATS) {
            if(e.equalsIgnoreCase(ext))
                return true;
        }
        return false;
    }

    private static File[] toFileArray(Object[] array) {
        File[] a = new File[array.length];
        for(int i=0;i<array.length;i++) {
            a[i] = (File) array[i];
        }
        return a;
    }
}
