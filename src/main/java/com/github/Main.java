package com.github;

import org.apache.commons.cli.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.github.Utils.exitOnInvalidParameter;

/* takes command line arguments and beings the download process */

public class Main {

    private final static Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        // command line options
        Options options = new Options();

        // add option for number of chunks
        Option chunksOption = new Option("c", "chunks", true, "number of chunks to download");
        chunksOption.setRequired(false);
        chunksOption.setType(Number.class);
        options.addOption(chunksOption);

        // add option for chunk size
        Option sizeOption = new Option("s", "size", true, "size of the chunks to download");
        sizeOption.setRequired(false);
        sizeOption.setType(Number.class);
        options.addOption(sizeOption);

        // add option for name of the file to output
        Option fileOption = new Option("f", "file", true, "name of the file to output");
        fileOption.setRequired(false);
        fileOption.setType(String.class);
        options.addOption(fileOption);

        // add option for parallel downloading
        Option parallelOption = new Option("p", "parallel", false, "download the chunks in parallel");
        parallelOption.setRequired(false);
        options.addOption(parallelOption);

        // add option for help
        Option helpOption = new Option("h", "help", false, "print help message");
        helpOption.setRequired(false);
        options.addOption(helpOption);

        // useful instructions for help option
        String header = "Download part of a file in chunks\n\n";
        String footer = "\nPlease report issues at https://github.com/elainaRenee/multi-get/issues";

        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            // parse command line arguments
            cmd = parser.parse(options, args);
            // print help message if help argument is passed
            if (cmd.hasOption("help")) {
                formatter.printHelp("java -jar multiget.jar [OPTIONS] <URL>", header, options, footer);
                return;
            }
        } catch (ParseException e) {
            // print help message on error
            exitOnInvalidParameter("Error in parsing command line arguments.");
            return;
        }

        LOGGER.log(Level.INFO, "Validating command line arguments");

        // get URL
        List<String> unparsedArgs = cmd.getArgList();
        if (unparsedArgs.size() < 1) {
            exitOnInvalidParameter("Please enter a value for URL.");
            return;
        } else if (unparsedArgs.size() > 1) {
            exitOnInvalidParameter("Please enter a valid URL.");
            return;
        }

        // check to make sure URL is valid
        URL downloadUrl;
        try {
            downloadUrl = new URL(unparsedArgs.get(0));
            LOGGER.log(Level.INFO, "Download URL: " + downloadUrl);
        } catch (MalformedURLException e) {
            exitOnInvalidParameter("Please enter a valid URL.", e);
            return;
        }

        // get command line options
        int chunks = 0, chunkSize = 0;
        String outputFile = null;
        boolean parallel = false;

        try {
            if (cmd.hasOption("chunks")) {
                chunks = ((Number)cmd.getParsedOptionValue("chunks")).intValue();
                LOGGER.log(Level.INFO, "Chunks: " + chunks);
            }

            if(cmd.hasOption("size")) {
                chunkSize = ((Number)cmd.getParsedOptionValue("size")).intValue();
                LOGGER.log(Level.INFO, "Chunk Size: " + chunkSize);
            }

            if (cmd.hasOption("parallel")) {
                parallel = true;
                LOGGER.log(Level.INFO, "Parallel: " + parallel);
            }

            if (cmd.hasOption("file")) {
                outputFile = cmd.getParsedOptionValue("file").toString();
                LOGGER.log(Level.INFO, "Output File: " + outputFile);
            }
        } catch (ParseException e) {
            exitOnInvalidParameter("One or more arguments have invalid options.", e);
            return;
        }

        // build download with options
        Download download = new Download.DownloadBuilder(downloadUrl)
                                    .chunks(chunks)
                                    .chunkSize(chunkSize)
                                    .parallel(parallel)
                                    .outputFile(outputFile)
                                    .build();

        // start download
        download.start();
    }

}
