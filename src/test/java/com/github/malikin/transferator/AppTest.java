package com.github.malikin.transferator;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

import com.github.malikin.transferator.dto.Account;
import com.github.malikin.transferator.dto.Balance;
import com.github.malikin.transferator.dto.Transaction;
import io.restassured.http.ContentType;
import org.jooby.test.JoobyRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class AppTest {

    private static final Double BANK_ACCOUNT_ID = 1D;

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

        given()
                .accept(ContentType.JSON)
                .when()
                .get("/account?name=TestAccountNotExist")
                .then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    public void getNonExistAccountBalanceTest() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/account/100/balance")
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

    @Test
    public void createDoubleAccountTest() {
        String account = "{\"name\":\"TestAccountDouble\"}";

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

        given()
                .contentType(ContentType.JSON)
                .body(account)
                .accept(ContentType.JSON)
                .when()
                .post("/account")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void createAccountAndFindByIdTest() {
        String account = "{\"name\":\"TestAccountFindById\"}";

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

        Account accountCreated = get("/account?name=TestAccountFindById").as(Account.class);

        given()
                .accept(ContentType.JSON)
                .when()
                .get(String.format("/account/%d", accountCreated.getId()))
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void createAccountAndCheckEmptyBalanceTest() {
        String account = "{\"name\":\"TestAccountEmptyBalance\"}";

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

        Account accountCreated = get("/account?name=TestAccountEmptyBalance").as(Account.class);

        Balance balance = get(String.format("/account/%d/balance", accountCreated.getId())).as(Balance.class);

        assertEquals(0D, balance.getAmount(), 0.01);
    }

    @Test
    public void createAccountIncreaseBalanceCheckTransactionLogTest() {
        String account = "{\"name\":\"TestAccountWithBalance\"}";

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

        Account accountCreated = get("/account?name=TestAccountWithBalance").as(Account.class);

        Balance balance = get(String.format("/account/%d/balance", accountCreated.getId())).as(Balance.class);

        assertEquals("Balance is empty", 0D, balance.getAmount(), 0.001);

        String transferOperation = "{\"senderId\": " + BANK_ACCOUNT_ID + ", \"recipientId\": " + accountCreated.getId()  + ", \"amount\": 100 }";

        given()
                .contentType(ContentType.JSON)
                .body(transferOperation)
                .accept(ContentType.JSON)
                .when()
                .post("/transaction")
                .then()
                .assertThat()
                .statusCode(201);

        balance = get(String.format("/account/%d/balance", accountCreated.getId())).as(Balance.class);

        assertEquals("Bank presented 100 coins", 100D, balance.getAmount(), 0.001);

        given()
                .accept(ContentType.JSON)
                .when()
                .get(String.format("/account/%d/transactions", accountCreated.getId()))
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON);

        List<Transaction> transactions = Arrays.asList(get(String.format("/account/%d/transactions", accountCreated.getId())).as(Transaction[].class));

        assertEquals("Should be two records about one operation", 2, transactions.size());

        List<Transaction> transactionsByUuid = Arrays.asList(get(String.format("/transaction/%s", transactions.get(0).getOperationUuid().toString())).as(Transaction[].class));

        assertEquals("Should be two records about one operation", 2, transactionsByUuid.size());
    }

    @Test
    public void createAccountMakeTransferFromEmptyBalanceTest() {
        String account = "{\"name\":\"TestAccountWithEmptyBalance\"}";

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

        Account accountCreated = get("/account?name=TestAccountWithEmptyBalance").as(Account.class);

        Balance balance = get(String.format("/account/%d/balance", accountCreated.getId())).as(Balance.class);

        assertEquals("Balance is empty", 0D, balance.getAmount(), 0.001);

        String transferOperation = "{\"recipientId\": " + BANK_ACCOUNT_ID + ", \"senderId\": " + accountCreated.getId()  + ", \"amount\": 100 }";

        given()
                .contentType(ContentType.JSON)
                .body(transferOperation)
                .accept(ContentType.JSON)
                .when()
                .post("/transaction")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void makeTransferWithZeroAmountTest() {
        String transferOperation = "{\"senderId\": " + BANK_ACCOUNT_ID + ", \"recipientId\": 2, \"amount\": 0 }";

        given()
                .contentType(ContentType.JSON)
                .body(transferOperation)
                .accept(ContentType.JSON)
                .when()
                .post("/transaction")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void makeTransferNonExistSenderTest() {
        String transferOperation = "{\"senderId\": 1000, \"recipientId\": 1, \"amount\": 100 }";

        given()
                .contentType(ContentType.JSON)
                .body(transferOperation)
                .accept(ContentType.JSON)
                .when()
                .post("/transaction")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void makeTransferNonExistRecipientTest() {
        String transferOperation = "{\"senderId\": 1, \"recipientId\": 1000, \"amount\": 100 }";

        given()
                .contentType(ContentType.JSON)
                .body(transferOperation)
                .accept(ContentType.JSON)
                .when()
                .post("/transaction")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void getNonExistTransactionByOperationUuidTest() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/transaction/0000-0000-0000")
                .then()
                .assertThat()
                .statusCode(404);
    }
}
