import io.restassured.RestAssured
import io.restassured.matcher.RestAssuredMatchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

/**
 * yggdrasil
 *
 * Created by Erik Håkansson on 2017-04-09.
 * Copyright 2017
 *
 */

class IntegrationTest {

  @Test fun testRest() {
    RestAssured.port = 8080
    RestAssured.get("/rest").then().assertThat().body("test", RestAssuredMatchers.equalToPath("test"))
  }
}
