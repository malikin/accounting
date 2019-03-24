[![Build Status](https://travis-ci.org/malikin/transferator.svg?branch=master)](https://travis-ci.org/malikin/transferator)
[![codecov](https://codecov.io/gh/malikin/transferator/branch/master/graph/badge.svg)](https://codecov.io/gh/malikin/transferator)

## TRANSFERATOR

Test application based on jooby framework.
REST API for money transfers between accounts.

Database schema located in application.conf (of course I know about flyway, liquibase etc :)

Main test scenario can be founded in AppTest :: createTwoAccountsAndMakeTransfersBetweenThemTest

#### How to start application (will start on http://localhost:8080/):

```
$ mvn package
$ java -jar target/transferator-1.0-SNAPSHOT.jar
```

#### List of API endpoints and available HTTP methods:

```
POST /account
GET  /account/:id
GET  /account?name="Ivan"
GET  /account/:accountId/balance
GET  /account/:accountId/transactions

POST /transaction
GET  /transaction/:operationUuid
```

#### How to use:

##### Add new account with name "Test" (will be created with 0 balance)

```
curl -X POST \
  http://localhost:8080/account \
  -H 'Accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{"name": "Test"}'
```

##### Get account by Id

```
curl -X GET \
  http://localhost:8080/account/1 \
  -H 'Accept: application/json'
```

##### Get account by name

```
curl -X GET \
  'http://localhost:8080/account?name=Test' \
  -H 'Accept: application/json'
```

##### Get account balance

```
curl -X GET \
  http://localhost:8080/account/1/balance \
  -H 'Accept: application/json'
```

##### Get account transactions

```
curl -X GET \
  http://localhost:8080/account/1/transactions \
  -H 'Accept: application/json'
```

##### Transfer 100 coins from account 1 to account 2

```
curl -X POST \
  http://localhost:8080/transaction \
  -H 'Accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
          "senderId": 1,
          "recipientId": 2,
          "amount": 100
      }'
```

##### Describe transfer operation by operationUUID

```
curl -X GET \
  http://localhost:8080/transaction/8a709f3a-9dba-4704-acaf-e939e9a67da3 \
  -H 'Accept: application/json' 
```