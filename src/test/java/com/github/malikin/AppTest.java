package com.github.malikin;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

import io.restassured.http.ContentType;
import org.jooby.test.JoobyRule;
import org.jooby.test.MockRouter;
import org.junit.ClassRule;
import org.junit.Test;

public class AppTest {

    /**
     * One app/server for all the test of this class. If you want to start/stop a new server per test,
     * remove the static modifier and replace the {@link ClassRule} annotation with {@link Rule}.
     */
    @ClassRule
    public static JoobyRule app = new JoobyRule(new App());

    @Test
    public void getAllAccountsTest() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/account")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    public void getNonExistAccountTest() {

        given()
                .accept(ContentType.JSON)
                .when()
                .get("/account/100")
                .then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    public void createAccountTest() {
        String account = "{\"name\":\"TestAccount\"}";

        given()
                .contentType(ContentType.JSON)
                .body(account)
                .accept(ContentType.JSON)
                .when()
                .post("/account")
                .then()
                .assertThat()
                .statusCode(201)
                .contentType(ContentType.JSON);
    }

//
//    @Test
//    public void unitTest() throws Throwable {
//        String result = new MockRouter(new App())
//                .get("/");
//
//        assertEquals("Hello World!", result);
//    }
}
