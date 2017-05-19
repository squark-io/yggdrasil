import io.restassured.RestAssured
import io.restassured.matcher.RestAssuredMatchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.charset.Charset
import org.apache.commons.io.IOUtils

/**
 * yggdrasil
 *
 * Created by Erik Håkansson on 2017-04-09.
 * Copyright 2017
 *
 */

class IntegrationTest {

  @Test fun testRest() {
    RestAssured.port = System.getProperty("io.squark.yggdrasil.port").toInt()
    RestAssured.get("/rest").then().assertThat().body("test", RestAssuredMatchers.equalToPath("test"))
  }

  @Test fun testDelegatedMainClass() {
    val logFile = File("./build/test-results/main.log")
    Assertions.assertNotNull(logFile, logFile.absolutePath)
    val lines = IOUtils.readLines(logFile.inputStream(), Charset.defaultCharset())
    Assertions.assertEquals(1, lines.size,
      lines.joinToString(prefix = "\nLines: {\n", separator = ",\n", postfix = "\n}", transform = { "\t\"$it\"" }))
    Assertions.assertTrue(lines[0].endsWith("INFO test.TestDelegatedMainClass - test.TestDelegatedMainClass loaded"))
  }
}
