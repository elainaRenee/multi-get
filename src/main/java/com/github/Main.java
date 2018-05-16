package com.github;

import org.apache.commons.cli.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static com.github.Utils.exit;

public class Main {

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

        // useful instructions for help option
        String header = "Download a part of a file in chunks\n\n";
        String footer = "\nPlease report issues at https://github.com/elainaRenee/multi-get/issues";

        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            // parse command line arguments
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            // print help message on error
            System.out.println(e.getMessage());
            formatter.printHelp("java -jar multiget [OPTIONS] <URL>", header, options, footer);
            System.exit(1);
            return;
        }

        // get URL
        List<String> unparsedArgs = cmd.getArgList();
        if (unparsedArgs.size() < 1) {
            // TODO: need error logging
            exit("Please enter a value for URL.");
            return;
        } else if (unparsedArgs.size() > 1) {
            // TODO: need error logging
            exit("Please enter only one value for URL.");
            return;
        }

        URL downloadUrl;
        try {
            downloadUrl = new URL(unparsedArgs.get(0));
        } catch (MalformedURLException e) {
            // TODO: need error logging
            System.out.println(e.getMessage());
            exit("Please enter a valid URL.");
            return;
        }

        // get command line options
        int chunks = 0, chunkSize = 0;
        String outputFile = null;
        boolean parallel = false;

        try {
            if (cmd.hasOption("chunks")) {
                chunks = ((Number)cmd.getParsedOptionValue("chunks")).intValue();
            }

            if(cmd.hasOption("chunkSize")) {
                chunkSize = ((Number)cmd.getParsedOptionValue("chunkSize")).intValue();
            }

            if (cmd.hasOption("parallel")) {
                parallel = true;
            }

            if (cmd.hasOption("file")) {
                outputFile = cmd.getParsedOptionValue("file").toString();
            }
        } catch (ParseException e) {
            // TODO: need error logging
            System.out.println(e.getMessage());
            exit("One or more arguments have invalid options.");
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
