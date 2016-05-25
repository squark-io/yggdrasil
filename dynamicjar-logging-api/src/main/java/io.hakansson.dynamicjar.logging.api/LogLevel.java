package io.hakansson.dynamicjar.logging.api;

/**
 *
 */

/**
 * Standard Logging Levels as an enumeration for use internally. This enum is used as a parameter in any public APIs.
 * Taken from https://git-wip-us.apache.org/repos/asf?p=logging-log4j2.git;a=blob;
 * f=log4j-api/src/main/java/org/apache/logging/log4j/spi/StandardLevel.java;h=8a10e2d7a6183b436bde78542305862a2e94d200;hb=HEAD
 * (2016-05-09)
 */
public enum LogLevel {

    /**
     * No events will be logged.
     */
    OFF(0),

    /**
     * A severe error that will prevent the application from continuing.
     */
    FATAL(100),

    /**
     * An error in the application, possibly recoverable.
     */
    ERROR(200),

    /**
     * An event that might possible lead to an error.
     */
    WARN(300),

    /**
     * An event for informational purposes.
     */
    INFO(400),

    /**
     * A general debugging event.
     */
    DEBUG(500),

    /**
     * A fine-grained debug message, typically capturing the flow through the application.
     */
    TRACE(600),

    /**
     * All events should be logged.
     */
    ALL(Integer.MAX_VALUE);

    private final int intLevel;

    LogLevel(final int val) {
        intLevel = val;
    }

    /**
     * Returns the integer value of the Level.
     *
     * @return the integer value of the Level.
     */
    public int intLevel() {
        return intLevel;
    }

}
