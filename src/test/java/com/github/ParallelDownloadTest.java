package com.github;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParallelDownloadTest {

    @Test
    public void compareParallelDownloads() throws IOException {
        // download in one part
        String args[] = new String[]{"http://47c3b616.bwtest-aws.pravala.com/384MB.jar", "-p", "-c", "1", "-s", "4", "-f", "full.jar"};
        Main.main(args);

        // download in multiple parts
        String argz[] = new String[]{"http://47c3b616.bwtest-aws.pravala.com/384MB.jar", "-p", "-f", "parts.jar"};
        Main.main(argz);

        byte[] f1 = Files.readAllBytes(Paths.get("full.jar"));
        byte[] f2 = Files.readAllBytes(Paths.get("parts.jar"));
        assertTrue(Arrays.equals(f1, f2));
    }
}
