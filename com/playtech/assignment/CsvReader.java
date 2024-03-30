package com.playtech.assignment;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.playtech.assignment.User.FrozenStatus;
import java.util.function.Function;

import java.util.HashMap;
import java.util.Optional;
import java.util.TreeMap;




public class CsvReader {
     public static void main(String[] args) {

        try {
            List<User> userdata= readUser();
            userdata.stream()
            .limit(10)
            .forEach(System.out::println);
            System.out.println(userdata);
        }
        catch (IOException err) {
            err.printStackTrace();
        }


        try {
            List<Transaction> transactions = readTransactions();
       transactions.stream()
       .limit(10)
       .forEach(System.out::println);
            System.out.println(transactions);


        } catch (IOException err) {
            err.printStackTrace();
        }

        try {
            List<BinMapping> binMappings = readBinMapping();
            binMappings.stream()
                       .limit(10)
                       .forEach(System.out::println);
        } catch (IOException err) {
            err.printStackTrace();
        }


        // List<Event> events = processTransactions(userdata, transactions, binMappings);
    }

 
    private static List<Event> processTransactions(final List<User> users, final List<Transaction> transactions, final List<BinMapping> binMappings) {
            Map<Integer, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));
            
            Set<String> transactionIds = new HashSet<>();
            Set<String> successfulDeposits = new HashSet<>();
            Map<String, Integer> accountUserMap = new HashMap<>();
            List<Event> events = new ArrayList<>();

             // Pre-process binMappings for efficient search
        TreeMap<Long, BinMapping> binMappingMap = binMappings.stream()
        .collect(Collectors.toMap(BinMapping::getRangeFrom, Function.identity(),
                (existing, replacement) -> existing, TreeMap::new));

    
        for (Transaction transaction : transactions) {
            try {
                if (!transactionIds.add(transaction.getTransactionId())) {
                    events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Transaction ID is not unique"));
                    continue;
                }
    
                User user = userMap.get(transaction.getUserId());
                if (user == null || user.isFrozen()) {
                    events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "User does not exist or is frozen"));
                    continue;
                }

        // Validate payment method
        if (transaction.getMethod() == Transaction.Method.TRANSFER) {
            if (!isValidIban(transaction.getAccountNumber())) {
                events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Invalid IBAN for transfer"));
                continue;
            }
        } else if (transaction.getMethod() == Transaction.Method.CARD) {
            Optional<BinMapping> binMapping = findBinMapping(binMappingMap, transaction.getAccountNumber());
            private String toAlpha3(String country) {
                // Implement the logic to convert country to alpha3 code
                // ...
                return alpha3Code;
            }

            if (!binMapping.isPresent() || binMapping.get().getType() != BinMapping.Type.DC) {
                events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Invalid or non-debit card used"));
                continue;
            }
            // Additional logic to check card country matches user's country
            String userCountryAlpha3 = toAlpha3(user.getCountry());
            String cardCountryAlpha3 = toAlpha3(binMapping.get().getCountry());
            if (!cardCountryAlpha3.equals(userCountryAlpha3)) {
                events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Card country does not match user's country"));
                continue;
            }
        // Further validation for deposit/withdrawal limits
        // Example for deposit limit check
        if (transaction.getType() == Transaction.Type.DEPOSIT && (amount < user.getDepositMin() || amount > user.getDepositMax())) {
            events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Transaction amount outside deposit limits"));
            continue;
        }

                // 


                if (transaction.getMethod() == Transaction.Method.TRANSFER) {
                    if (!isValidIban(transaction.getAccountNumber())) {
                        events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Invalid IBAN for transfer"));
                        continue;
                    }
                // } else if (transaction.getMethod() == Transaction.Method.CARD) {
                //     Optional<BinMapping> binMapping = findBinMapping(binMappingMap, transaction.getAccountNumber());
                //     if (!binMapping.isPresent() || !binMapping.get().getType().equals(BinMapping.Type.DC)) {
                //         events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Invalid or non-debit card used"));
                //         continue;
                //     }
                //     // Additional logic to check card country matches user's country
                // } else {
                    events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Unsupported payment method"));
                    continue;
                }
                // Assuming you are inside the loop iterating over transactions

// Validate transaction amount against deposit/withdrawal limits
if (transaction.getType() == Transaction.Type.DEPOSIT) {
    if (transaction.getAmount() < user.getDepositMin() || transaction.getAmount() > user.getDepositMax()) {
        events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Deposit amount out of allowed range"));
        continue;
    }
} else if (transaction.getType() == Transaction.Type.WITHDRAW) {
    if (transaction.getAmount() < user.getWithdrawMin() || transaction.getAmount() > user.getWithdrawMax()) {
        events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Withdrawal amount out of allowed range"));
        continue;
    }
    // Additionally, check if the user has sufficient balance for withdrawal
    if (user.getBalance() < transaction.getAmount()) {
        events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Insufficient balance for withdrawal"));
        continue;
    }
}
     
                // Further validation for deposit/withdrawal limits
        //    /     Optional<BinMapping> binMapping = findBinMapping(binMappingMap, transaction.getAccountNumber());
        //         if (!binMapping.isPresent()) {
        //             events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "No matching bin mapping found"));
        //             continue;
        //         }
            } catch (NumberFormatException e) {
                System.err.println("Error parsing account number for transaction ID " + transaction.getTransactionId() + ": " + e.getMessage());
                events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Invalid account number format"));
            } catch (Exception e) {
                System.err.println("Unexpected error processing transaction ID " + transaction.getTransactionId() + ": " + e.getMessage());
            }
        }
    
        return events;
    }


    public static <T> List<T> readFromCSV(String filePath, Function<String[], T> mappingFunction) throws IOException {
        try (Stream<String> stream = Files.lines(Path.of(filePath))) {
            return stream
                    .skip(1) // Assuming the first line is a header
                    .map(line -> line.split(","))
                    .map(mappingFunction)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Now you can use this method to read your CSV files like this:

    private static List<BinMapping> readBinMapping() throws IOException {
        return readFromCSV("com/playtech/assignment/test-data/manual test data 75% validations/input/bins.csv", arr -> {
            String name = arr[0];
            long rangeFrom = Long.parseLong(arr[1]);
            long rangeTo = Long.parseLong(arr[2]);
            BinMapping.Type type = BinMapping.Type.valueOf(arr[3].toUpperCase());
            String country = arr[4];
            return new BinMapping(name, rangeFrom, rangeTo, type, country);
        });
    }

    private static List<Transaction> readTransactions() throws IOException {
        return readFromCSV("com/playtech/assignment/test-data/manual test data 75% validations/input/transactions.csv", arr -> {
            String transactionId = arr[0];
            int userId = Integer.parseInt(arr[1]);
            Transaction.Type type = Transaction.Type.valueOf(arr[2].toUpperCase());
            double amount = Double.parseDouble(arr[3]);
            Transaction.Method method = Transaction.Method.valueOf(arr[4].toUpperCase());
            String accountNumber = arr[5];
            return new Transaction(transactionId, userId, type, amount, method, accountNumber);
        });
    }

    private static List<User> readUser() throws IOException {
        return readFromCSV("com/playtech/assignment/test-data/manual test data 75% validations/input/users.csv", arr -> {
            int userId = Integer.parseInt(arr[0]);
            String username = arr[1];
            double balance = Double.parseDouble(arr[2]);
            String country = arr[3];
            FrozenStatus frozen = FrozenStatus.fromValue(Integer.parseInt(arr[4]));
            double depositMin = Double.parseDouble(arr[5]);
            double depositMax = Double.parseDouble(arr[6]);
            double withdrawMin = Double.parseDouble(arr[7]);
            double withdrawMax = Double.parseDouble(arr[8]);
            return new User(userId, username, balance, country, frozen, depositMin, depositMax, withdrawMin, withdrawMax);
        });
    }

    private static Optional<BinMapping> findBinMapping(TreeMap<Long, BinMapping> binMappingMap, String accountNumber) {
        try {
            long accountNumberLong = Long.parseLong(accountNumber);
            return Optional.ofNullable(binMappingMap.floorEntry(accountNumberLong)).map(Map.Entry::getValue);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
    private static boolean isValidIban(String iban) {
        // Remove non-alphanumeric characters
        iban = iban.replaceAll("[^A-Za-z0-9]", "");
    
        // Move the first four characters to the end
        iban = iban.substring(4) + iban.substring(0, 4);
    
        // Convert letters to digits and perform mod-97 operation
        long total = 0;
        for (int i = 0; i < iban.length(); i++) {
            int value = Character.digit(iban.charAt(i), 36);
            total = (total * 10 + value) % 97;
        }
    
        return total == 1;
    }
     

}
class User {
    private int userId;
    private String username;
    private double balance;
    private String country;
    private FrozenStatus frozen;
    private double depositMin;
    private double depositMax;
    private double withdrawMin;
    private double withdrawMax;

    // Enum for frozen status
    public enum FrozenStatus {
        ACTIVE_USER(0), FROZEN_USER(1);
    
        private final int value;
    
        FrozenStatus(int value) {
            this.value = value;
        }
    
        public int getValue() {
            return value;
        }
    
        public static FrozenStatus fromValue(int value) {
            for (FrozenStatus status : FrozenStatus.values()) {
                if (status.getValue() == value) {
                    return status;
                }
            }
            throw new IllegalArgumentException("No enum constant for value: " + value);
        }
    }

    // Constructor
    public User(int userId, String username, double balance, String country, FrozenStatus frozen2, double depositMin, double depositMax, double withdrawMin, double withdrawMax) {
        this.userId = userId;
        this.username = username;
        this.balance = balance;
        this.country = country;
        this.frozen = frozen2;
        this.depositMin = depositMin;
        this.depositMax = depositMax;
        this.withdrawMin = withdrawMin;
        this.withdrawMax = withdrawMax;
    }


    public boolean isFrozen() {
        return this.frozen == FrozenStatus.FROZEN_USER;
    }
    
    public void deposit(double amount) {
        this.balance += amount;
    }
    
    public void withdraw(double amount) {
        this.balance -= amount;
    }

    // Getters and Setters

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getBalance() {
        return this.balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public FrozenStatus getFrozen() {
        return this.frozen;
    }

    public void setFrozen(FrozenStatus frozen) {
        this.frozen = frozen;
    }

    public double getDepositMin() {
        return this.depositMin;
    }

    public void setDepositMin(double depositMin) {
        this.depositMin = depositMin;
    }

    public double getDepositMax() {
        return this.depositMax;
    }

    public void setDepositMax(double depositMax) {
        this.depositMax = depositMax;
    }

    public double getWithdrawMin() {
        return this.withdrawMin;
    }

    public void setWithdrawMin(double withdrawMin) {
        this.withdrawMin = withdrawMin;
    }

    public double getWithdrawMax() {
        return this.withdrawMax;
    }

    public void setWithdrawMax(double withdrawMax) {
        this.withdrawMax = withdrawMax;
    }

    // ...

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", balance=" + balance +
                ", country='" + country + '\'' +
                ", frozen=" + frozen +
                ", depositMin=" + depositMin +
                ", depositMax=" + depositMax +
                ", withdrawMin=" + withdrawMin +
                ", withdrawMax=" + withdrawMax +
                '}';
    }
}

class Transaction {
    private String transactionId;
    private int userId;
    private Type type; // Use the enum for type
    private double amount;
    private Method method; // Use the enum for method
    private String accountNumber;

    // Enum for type
    public enum Type {
        DEPOSIT, WITHDRAW;
    }

    // Enum for method
    public enum Method {
        CARD, TRANSFER;
    }

    // Constructor
    public Transaction(String transactionId, int userId, Type type, double amount, Method method, String accountNumber) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.method = method;
        this.accountNumber = accountNumber;
    }

    // Getters and Setters

    public String getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public double getAmount() {
        return this.amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Method getMethod() {
        return this.method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    // ...

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", userId=" + userId +
                ", type=" + type + // Use the enum's name() method for string representation
                ", amount=" + amount +
                ", method=" + method + // Use the enum's name() method for string representation
                ", accountNumber='" + accountNumber + '\'' +
                '}';
    }
}

class BinMapping {
    private String name;
    private long rangeFrom;
    private long rangeTo;
    private Type type; // Use the enum for type
    private String country;

    // Enum for type
    public enum Type {
        DC, CC;
    }

    // Constructor
    public BinMapping(String name, long rangeFrom, long rangeTo, Type type, String country) {
        this.name = name;
        this.rangeFrom = rangeFrom;
        this.rangeTo = rangeTo;
        this.type = type;
        this.country = country;
    }

    // Getters and Setters

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getRangeFrom() {
        return this.rangeFrom;
    }

    public void setRangeFrom(long rangeFrom) {
        this.rangeFrom = rangeFrom;
    }

    public long getRangeTo() {
        return this.rangeTo;
    }

    public void setRangeTo(long rangeTo) {
        this.rangeTo = rangeTo;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    // ...

    

    @Override
    public String toString() {
        return "Bin{" +
                "name='" + name + '\'' +
                ", rangeFrom=" + rangeFrom +
                ", rangeTo=" + rangeTo +
                ", type=" + type + // Use the enum's name() method for string representation
                ", country='" + country + '\'' +
                '}';
    }
}
class Event {
   public static final String STATUS_DECLINED = "DECLINED";
    public static final String STATUS_APPROVED = "APPROVED";

    public String transactionId;
    public String status;
    public String message;

    public Event(String transactionId, String status, String message) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "{" +
            " transactionId='" + getTransactionId() + "'" +
            ", status='" + getStatus() + "'" +
            ", message='" + getMessage() + "'" +
            "}";
    }


}

// 

// 
/** 
 * 
 *  No 3rd party libraries are allowed; utilize only what is available in the JDK (Java Development Kit).
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


Try (final FileWriter writer = new FileWriter(filePath.toFile(), false)) {
            writer.append("transaction_id,status,message\n");
            for (final var event : events) {
                writer.append(event.transactionId).append(",").append(event.status).append(",").append(event.message).append("\n");
            }
        }
    }
 * 
 * 
*/


