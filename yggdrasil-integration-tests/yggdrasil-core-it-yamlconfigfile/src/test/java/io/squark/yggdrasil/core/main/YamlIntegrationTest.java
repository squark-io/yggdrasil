package io.squark.yggdrasil.core.main;


import io.squark.yggdrasil.core.api.exception.PropertyLoadException;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-04-26.
 * Copyright 2016
 */
public class YamlIntegrationTest {
    private static PrintStream console = System.out;
    private static ByteArrayOutputStream out = new ByteArrayOutputStream() {
        @Override
        public synchronized void write(byte[] b, int off, int len) {
            super.write(b, off, len);
            console.write(b, off, len);
        }

        @Override
        public void write(byte[] b) throws IOException {
            super.write(b);
            console.write(b);
        }
    };

    @BeforeSuite
    public static void setUp() {
        System.setOut(new PrintStream(out));
    }

    @Test
    public void testYaml() throws PropertyLoadException {
        YggdrasilConfiguration configuration = Yggdrasil.getConfiguration();
        Assert.assertNotNull(configuration);
        Assert.assertTrue(out.toString().contains("Found YAML configuration"));
    }

    @AfterSuite
    public static void tearDown() throws IOException {
        out.close();
    }
}
