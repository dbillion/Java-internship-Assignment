# Java Developer Test Assignment

## Intro

First Bank of Teldrassil is an old bank that neglected server maintenance and had their banking database blow up. Luckily an ancient automated backup system still worked. The only issue is that the backup is in the format of simple CSV
files logging all transactions. The bank needs this data to be processed and to reconstruct all of their customers balances from the logs. This is where you come in, the bank have offered a reward to all expert developers across the land of
Kalimdor for writing a program to process these files and rescue the balances of the people of Kalimdor.

## Restrictions

- No 3rd party libraries are allowed; utilize only what is available in the JDK (Java Development Kit).
- Java version up to 21 LTS should be used.


## Task

Implement a program that processes a list of transactions from a file (see "inputs" section). After processing, the program should generate two outputs:

1. A list of userIDs with their corresponding new balance.
2. A list of outcomes (events) for each transaction (see "output" section for more details).

Input/output files are simplified CSV and don't have escaped characters, quote marks or commas within column

Example input and output files will be provided, but be prepared for additional tests to ensure the robustness of your solution. Feel free to test it with more than just the sample data.

## Inputs

Path to the input and output files must be taken from arguments (main method parameters) in the following order:
Users, Transactions, BinMappings, Balances, Events.
You can see example in provided template (TransactionProcessorSample.java).

### Users

- CSV file with fewer than 1000 records containing the following columns:
  - `user_id` - used in transactions to refer to the user.
  - `username` - not used for any validations; a readable name may help with debugging.
  - `balance` - user's current balance amount.
  - `country` - **two**-letter country code, ISO 3166-1 alpha-2.
  - `frozen` - 0 for an active user, 1 for frozen.
  - `deposit_min` - amount, minimum allowed deposit, inclusive.
  - `deposit_max` - amount, maximum allowed deposit, inclusive.
  - `withdraw_min` - amount, minimum allowed withdrawal, inclusive.
  - `withdraw_max` - amount, maximum allowed withdrawal, inclusive.

### Transactions

- CSV file with fewer than 10 000 000 records containing the following columns:
  - `transaction_id` - ID of the transaction.
  - `user_id` - ID of the user.
  - `type` - transaction type (allowed values are DEPOSIT or WITHDRAW).
  - `amount` - amount.
  - `method` - payment method (allowed values are CARD or TRANSFER).
  - `account_number` - depends on method; card number for method=CARD, or IBAN for method=TRANSFER.

### Bin Mapping

- Use this to validate card country and distinguish debit cards.
- Columns:
  - `name` - issuing bank name.
  - `range_from` - the lowest possible card number (first 10 digits of card number) that would be identified within this card range, inclusive.
  - `range_to` - the highest possible card number (first 10 digits of card number) that would be identified within this card range, inclusive.
  - `type` - **DC** for debit and **CC** for credit cards.
  - `country` - **three**-letter country code, ISO 3166-1 alpha-3.

For example, card with number 5168831234567890 corresponds to this entry:\
AS LHV PANK,5168830000,5168839999,DC,EST\
This means that the card is a debit card issued by LHV in Estonia  

## Processing Requirements

The processing should include the following steps:

- Validate that the transaction ID is unique (not used before).
- Verify that the user exists and is not frozen (users are loaded from a file, see "inputs").
- Validate payment method:
  - For **TRANSFER** payment methods, validate the transfer account number's check digit validity (see details here [International Bank Account Number](https://en.wikipedia.org/wiki/International_Bank_Account_Number))
  - For **CARD** payment methods, only allow debit cards; validate that card type=DC (see bin mapping part of "inputs" section for details)
  - Other types must be declined
- Confirm that the country of the card or account used for the transaction matches the user's country
- Validate that the amount is a valid (positive) number and within deposit/withdraw limits.
- For withdrawals, validate that the user has a sufficient balance for a withdrawal.
- Allow withdrawals only with the same payment account that has previously been successfully used for deposit (declined deposits with an account do not make it eligible for withdrawals; at least one approved deposit is needed).
- Transaction type that isn't deposit or withdrawal should be declined
- Users cannot share iban/card; payment account used by one user can no longer be used by another (Example Scenario for this validation provided below).
- In case of unexpected errors with processing transactions, skip the transaction. Do not interrupt processing of the remaining transactions

Transactions that fail any of the validations should be declined (i.e., the user's balance remains unchanged), and the decline reason should be saved in the events file.

**Example Scenario:**

- User A uses account X for deposit. Deposit gets approved.
- User A uses account Y for deposit. Deposit gets declined due to some validation.
- User B uses account X for deposit. Deposit gets declined because this account was already used by user A.
- User B uses account Y for deposit. Deposit gets approved because nobody successfully used this account yet.
- User A uses account X for deposit. Deposit gets approved because account X belongs to this user.

**Money amounts in the input and output are expected to be in the following format:**

- The amount consists of a whole number part (before the decimal point) and a decimal part (after the decimal point).
- The whole number part can be any integer (positive, negative, or zero).
- The decimal part always contains exactly two digits after the decimal point.
- The total length of the money amount (including both whole and decimal parts) can be up to 20 digits.

**Note:** `stdout`/`stderr` can contain anything as it will not affect our automated tests.

## Outputs

### balances.csv

- CSV file with 2 columns:
  - `user_id` - ID of a user.
  - `balance` - balance of the user after all transactions have been processed.

### events.csv

- CSV file with the result of processing individual transactions. Columns:
  - `transaction_id` - ID of a transaction.
  - `status` - either APPROVED or DECLINED.
  - `message` - additional information such as decline reason. Use OK for approved.

The files should normally contain the same amount of lines as the corresponding input files (one line for every user/transaction).

------

**How to submit your assignment:**

- We expect zip file that contains all source code files (.java).
- You can also include any testing input files you created.
- Email link to the solution back to us by 04.04.2023 as a reply to the original email the assignment was sent from. Don't mind it's from notifications@smartrecruiters.com, we will get it.
- Don't send solution as attachment to email. Send it to us via link to Google Drive or some other online file sharing platform.

Feel free to reach out if you need any further clarification. We encourage all submissions, even if they aren't fully completed. Good luck!
