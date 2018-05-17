package com.github;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DownloadTest {

    @Test
    public void compareDownloads() throws IOException {
        // download in one part
        String args[] = new String[]{"http://47c3b616.bwtest-aws.pravala.com/384MB.jar", "-c", "1", "-s", "4", "-f", "full.jar"};
        Main.main(args);

        // download in multiple parts
        String argz[] = new String[]{"http://47c3b616.bwtest-aws.pravala.com/384MB.jar", "-c", "4", "-s", "1", "-f", "parts.jar"};
        Main.main(argz);

        byte[] f1 = Files.readAllBytes(Paths.get("full.jar"));
        byte[] f2 = Files.readAllBytes(Paths.get("parts.jar"));
        assertTrue(Arrays.equals(f1, f2));
    }
}
