package com.github;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Utils {

    private final static Logger LOGGER = Logger.getLogger(Download.class.getName());

    /**
     * prints error message to user and exits program on invalid parameters
     * @param errorMessage message to display to the user
     */
    public static void exitOnInvalidParameter(String errorMessage) {
        LOGGER.log(Level.SEVERE, errorMessage + " See multiget.jar --help for more information.");
        System.exit(-1);
    }

    /**
     * prints error message to user and exits program on invalid parameters
     * @param errorMessage message to display to the user
     * @param t exception that was thrown
     */
    public static void exitOnInvalidParameter(String errorMessage, Throwable t) {
        LOGGER.log(Level.SEVERE, errorMessage + " See multiget.jar --help for more information.", t);
        System.exit(-1);
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

    /**
     * prints error message to user and exits program on fatal errors
     * @param errorMessage message to display to the user
     * @param t exception that was thrown
     */
    public static void exitOnFatalError(String errorMessage, Throwable t) {
        LOGGER.log(Level.SEVERE, errorMessage, t);
        System.exit(1);
    }

}
