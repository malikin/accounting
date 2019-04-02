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

import java.math.BigDecimal;
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
        final String accountPostBody = "{\"name\":\"TestAccount\"}";

        given()
                .contentType(ContentType.JSON)
                .body(accountPostBody)
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
        final String accountPostBody = "{\"name\":\"TestAccountDouble\"}";

        given()
                .contentType(ContentType.JSON)
                .body(accountPostBody)
                .accept(ContentType.JSON)
                .when()
                .post("/account")
                .then()
                .assertThat()
                .statusCode(201)
                .contentType(ContentType.JSON);

        given()
                .contentType(ContentType.JSON)
                .body(accountPostBody)
                .accept(ContentType.JSON)
                .when()
                .post("/account")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void createAccountAndFindByIdTest() {
        final String accountPostBody = "{\"name\":\"TestAccountFindById\"}";

        given()
                .contentType(ContentType.JSON)
                .body(accountPostBody)
                .accept(ContentType.JSON)
                .when()
                .post("/account")
                .then()
                .assertThat()
                .statusCode(201)
                .contentType(ContentType.JSON);

        final Account accountCreated = get("/account?name=TestAccountFindById").as(Account.class);

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
        final String accountPostBody = "{\"name\":\"TestAccountEmptyBalance\"}";

        given()
                .contentType(ContentType.JSON)
                .body(accountPostBody)
                .accept(ContentType.JSON)
                .when()
                .post("/account")
                .then()
                .assertThat()
                .statusCode(201)
                .contentType(ContentType.JSON);

        final Account accountCreated = get("/account?name=TestAccountEmptyBalance").as(Account.class);

        final Balance balance = get(String.format("/account/%d/balance", accountCreated.getId())).as(Balance.class);

        assertEquals("Balance is empty", 0, balance.getAmount().compareTo(BigDecimal.ZERO));
    }

    @Test
    public void createAccountIncreaseBalanceCheckTransactionLogTest() {
        final String accountPostBody = "{\"name\":\"TestAccountWithBalance\"}";

        given()
                .contentType(ContentType.JSON)
                .body(accountPostBody)
                .accept(ContentType.JSON)
                .when()
                .post("/account")
                .then()
                .assertThat()
                .statusCode(201)
                .contentType(ContentType.JSON);

        final Account accountCreated = get("/account?name=TestAccountWithBalance").as(Account.class);

        final Balance balance = get(String.format("/account/%d/balance", accountCreated.getId())).as(Balance.class);

        assertEquals("Balance is empty", 0, balance.getAmount().compareTo(BigDecimal.ZERO));

        final String transferOperationFromBank = "{\"senderId\": " + BANK_ACCOUNT_ID + ", \"recipientId\": " + accountCreated.getId()  + ", \"amount\": 100 }";

        given()
                .contentType(ContentType.JSON)
                .body(transferOperationFromBank)
                .accept(ContentType.JSON)
                .when()
                .post("/transaction")
                .then()
                .assertThat()
                .statusCode(201);

        final Balance balanceAfterUpdate = get(String.format("/account/%d/balance", accountCreated.getId())).as(Balance.class);

        assertEquals("Bank presented 100 coins", 0, balanceAfterUpdate.getAmount().compareTo(BigDecimal.valueOf(100)));

        given()
                .accept(ContentType.JSON)
                .when()
                .get(String.format("/account/%d/transactions", accountCreated.getId()))
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON);

        final List<Transaction> transactions = Arrays.asList(get(String.format("/account/%d/transactions", accountCreated.getId())).as(Transaction[].class));

        assertEquals("Should be two records about one operation", 2, transactions.size());

        final List<Transaction> transactionsByUuid = Arrays.asList(get(String.format("/transaction/%s", transactions.get(0).getOperationUuid().toString())).as(Transaction[].class));

        assertEquals("Should be two records about one operation", 2, transactionsByUuid.size());
    }

    @Test
    public void createTwoAccountsAndMakeTransfersBetweenThemTest() {
        final String senderAccountPostBody = "{\"name\":\"TestsenderAccount\"}";

        given()
                .contentType(ContentType.JSON)
                .body(senderAccountPostBody)
                .accept(ContentType.JSON)
                .when()
                .post("/account")
                .then()
                .assertThat()
                .statusCode(201)
                .contentType(ContentType.JSON);

        final Account senderAccount = get("/account?name=TestsenderAccount").as(Account.class);

        final Balance senderBalance = get(String.format("/account/%d/balance", senderAccount.getId())).as(Balance.class);

        assertEquals("Balance is empty", 0, senderBalance.getAmount().compareTo(BigDecimal.ZERO));

        final String transferOperationFromBankToSender = "{\"senderId\": " + BANK_ACCOUNT_ID + ", \"recipientId\": " + senderAccount.getId()  + ", \"amount\": 100 }";

        given()
                .contentType(ContentType.JSON)
                .body(transferOperationFromBankToSender)
                .accept(ContentType.JSON)
                .when()
                .post("/transaction")
                .then()
                .assertThat()
                .statusCode(201);

        final Balance senderBalanceAfterBankPresent = get(String.format("/account/%d/balance", senderAccount.getId())).as(Balance.class);

        assertEquals("Bank presented 100 coins to sender", 0, senderBalanceAfterBankPresent.getAmount().compareTo(BigDecimal.valueOf(100)));

        final String recipientAccountPostBody = "{\"name\":\"TestrecipientAccount\"}";

        given()
                .contentType(ContentType.JSON)
                .body(recipientAccountPostBody)
                .accept(ContentType.JSON)
                .when()
                .post("/account")
                .then()
                .assertThat()
                .statusCode(201)
                .contentType(ContentType.JSON);

        final Account recipientAccount = get("/account?name=TestrecipientAccount").as(Account.class);

        final Balance recipientBalance = get(String.format("/account/%d/balance", recipientAccount.getId())).as(Balance.class);

        assertEquals("Recipient balance is empty", 0, recipientBalance.getAmount().compareTo(BigDecimal.ZERO));

        String transferOperationFromBankToRecipient = "{\"senderId\": " + BANK_ACCOUNT_ID + ", \"recipientId\": " + recipientAccount.getId()  + ", \"amount\": 100 }";

        given()
                .contentType(ContentType.JSON)
                .body(transferOperationFromBankToRecipient)
                .accept(ContentType.JSON)
                .when()
                .post("/transaction")
                .then()
                .assertThat()
                .statusCode(201);

        final Balance recipientBalanceAfterBankPresent = get(String.format("/account/%d/balance", senderAccount.getId())).as(Balance.class);

        assertEquals("Bank presented 100 coins to recipient", 0, recipientBalanceAfterBankPresent.getAmount().compareTo(BigDecimal.valueOf(100D)));

        String transferOperationFromSenderToRecipient = "{\"senderId\": " + senderAccount.getId() + ", \"recipientId\": " + recipientAccount.getId()  + ", \"amount\": 50 }";

        given()
                .contentType(ContentType.JSON)
                .body(transferOperationFromSenderToRecipient)
                .accept(ContentType.JSON)
                .when()
                .post("/transaction")
                .then()
                .assertThat()
                .statusCode(201);

        final Balance senderBalanceAfterTransfer = get(String.format("/account/%d/balance", senderAccount.getId())).as(Balance.class);
        final Balance recipientBalanceAfterTransfer = get(String.format("/account/%d/balance", recipientAccount.getId())).as(Balance.class);

        assertEquals("Sender balance after transfer", 0, senderBalanceAfterTransfer.getAmount().compareTo(BigDecimal.valueOf(50)));
        assertEquals("Recipient balance after transfer", 0, recipientBalanceAfterTransfer.getAmount().compareTo(BigDecimal.valueOf(150)));

        final List<Transaction> senderTransactions = Arrays.asList(get(String.format("/account/%d/transactions", senderAccount.getId())).as(Transaction[].class));

        assertEquals("Should be four records about two operation", 4, senderTransactions.size());

//        final Double senderBalanceFromTransactions = senderTransactions.stream()
//                .filter(e -> e.getRecipientId().equals(senderAccount.getId()))
//                .mapToDouble(Transaction::getAmount).sum();
//
//        assertEquals("Balance and sum from transactions should be equal", senderBalanceAfterTransfer.getAmount(), senderBalanceFromTransactions);
//
//        final List<Transaction> recipientTransactions = Arrays.asList(get(String.format("/account/%d/transactions", recipientAccount.getId())).as(Transaction[].class));
//
//        assertEquals("Should be two records about one operation", 4, recipientTransactions.size());
//
//        final Double recipientBalanceFromTransactions = recipientTransactions.stream()
//                .filter(e -> e.getRecipientId().equals(recipientAccount.getId()))
//                .mapToDouble(Transaction::getAmount).sum();
//
//        assertEquals("Balance and sum from transactions should be equal", recipientBalanceAfterTransfer.getAmount(), recipientBalanceFromTransactions);
    }

    @Test
    public void createAccountMakeTransferFromEmptyBalanceTest() {
        final String accountPostBody = "{\"name\":\"TestAccountWithEmptyBalance\"}";

        given()
                .contentType(ContentType.JSON)
                .body(accountPostBody)
                .accept(ContentType.JSON)
                .when()
                .post("/account")
                .then()
                .assertThat()
                .statusCode(201)
                .contentType(ContentType.JSON);

        final Account accountCreated = get("/account?name=TestAccountWithEmptyBalance").as(Account.class);

        final Balance balance = get(String.format("/account/%d/balance", accountCreated.getId())).as(Balance.class);

        assertEquals("Balance is empty", 0, balance.getAmount().compareTo(BigDecimal.ZERO));

        final String transferOperationPostBody = "{\"recipientId\": " + BANK_ACCOUNT_ID + ", \"senderId\": " + accountCreated.getId()  + ", \"amount\": 100 }";

        given()
                .contentType(ContentType.JSON)
                .body(transferOperationPostBody)
                .accept(ContentType.JSON)
                .when()
                .post("/transaction")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void makeTransferWithZeroAmountTest() {
        final String transferOperationPostBody = "{\"senderId\": " + BANK_ACCOUNT_ID + ", \"recipientId\": 2, \"amount\": 0 }";

        given()
                .contentType(ContentType.JSON)
                .body(transferOperationPostBody)
                .accept(ContentType.JSON)
                .when()
                .post("/transaction")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void makeTransferNonExistSenderTest() {
        final String transferOperationPostBody = "{\"senderId\": 1000, \"recipientId\": 1, \"amount\": 100 }";

        given()
                .contentType(ContentType.JSON)
                .body(transferOperationPostBody)
                .accept(ContentType.JSON)
                .when()
                .post("/transaction")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void makeTransferNonExistRecipientTest() {
        final String transferOperationPostBody = "{\"senderId\": 1, \"recipientId\": 1000, \"amount\": 100 }";

        given()
                .contentType(ContentType.JSON)
                .body(transferOperationPostBody)
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
