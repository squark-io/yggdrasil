package io.squark.dynamicjar.test.frameworkprovider.resteasy;


import io.squark.dynamicjar.frameworkprovider.ResteasyFrameworkProvider;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-04-21.
 * Copyright 2016
 */
public class ResteasyFrameworkProviderIntegrationTest {

    private static ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
    private static boolean hasStarted = false;

    @BeforeSuite
    @Parameters({"jacoco.argLine", "targetArtifact"})
    public static void setUp(String jacocoArgLine, String targetArtifact) throws Exception {
        Executor executor = new DefaultExecutor();

        System.out.println(targetArtifact);
        CommandLine commandLine = CommandLine.parse("java -Ddynamicjar.logLevel=DEBUG " + jacocoArgLine + " -jar " + targetArtifact);
        executor.setExitValue(0);
        LogOutputStream logOutputStream = new LogOutputStream() {
            @Override
            protected void processLine(String line, int logLevel) {
                if (line
                    .contains(ResteasyFrameworkProvider.class.getSimpleName() + " initialized.")) {
                    hasStarted = true;
                }
                System.out.println(line);
            }
        };
        executor.setStreamHandler(new PumpStreamHandler(logOutputStream));
        executor.setWatchdog(watchdog);
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        executor.execute(commandLine, resultHandler);
        long time = 0L;
        while (!hasStarted) {
            if (time % 2000 == 0) System.out
                .println("[" + ResteasyFrameworkProviderIntegrationTest.class.getSimpleName() +
                         "] Waiting for initialization");
            if (time >= 120000L) {
                throw new Exception("Failed to initialize.");
            }
            Thread.sleep(200);
            time += 200;
        }
    }

    @AfterSuite(alwaysRun = true)
    public static void tearDown() {
        System.out.println("[" + ResteasyFrameworkProviderIntegrationTest.class.getSimpleName() +
                           "] Tearing down.");
        watchdog.destroyProcess();
    }

    @Test
    public void testIntegrationTest() {
        given().port(8877).get("/").then().assertThat().body("get(0)", equalTo("test this string"));
    }

}