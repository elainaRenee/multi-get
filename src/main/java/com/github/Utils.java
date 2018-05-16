package com.github;

import java.net.URL;

public final class Utils {

    /**
     * prints error message to user and exits program on fatal errors
     * @param errorMessage message to display to the user
     */
    public static void exit(String errorMessage) {
        System.out.println(errorMessage + " See multiget -help for more information.");
        System.exit(1);
    }

    /**
     * gets a file name from a URL
     * @param downloadUrl url the file is being downloaded from
     * @return name of the file
     */
    public static String getFileName(URL downloadUrl) {
        String file = downloadUrl.getFile();
        return file.substring(file.lastIndexOf('/') + 1);
    }

}
