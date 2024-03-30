package com.playtech.assignment;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.playtech.assignment.User.FrozenStatus;


// This template shows input parameters format.
// It is otherwise not mandatory to use, you can write everything from scratch if you wish.
public class TransactionProcessorSample {


    public static void main(final String[] args) throws IOException {
        String baseDir = "com/playtech/assignment";
        String testDirName = "test-data"; // This is dynamic
        String inputDir = "manual test data 75% validations/input";
        String outputDir = "manual test data 75% validations/output example";

        // Find the test_data directory
        try (Stream<Path> paths = Files.walk(Paths.get(baseDir))) {
            Path testDirPath = paths
            .filter(Files::isDirectory)
            // Debug: Print each directory found to see what the stream is processing.
            .peek(path -> System.out.println("Found directory: " + path.toString()))
            .filter(path -> path.getFileName().toString().trim().toLowerCase().contains(testDirName))
            .findFirst()
            .orElseThrow(() -> new IOException("Test directory not found"));

            // Construct the paths to the files
            Path usersFilePath = testDirPath.resolve(Paths.get(inputDir, "users.csv"));
            Path transactionsFilePath = testDirPath.resolve(Paths.get(inputDir, "transactions.csv"));
            Path binMappingsFilePath = testDirPath.resolve(Paths.get(inputDir, "bins.csv"));
            Path balancesFilePath = testDirPath.resolve(Paths.get(outputDir, "balances.csv"));
            Path eventsFilePath = testDirPath.resolve(Paths.get(outputDir, "events.csv"));

    
        List<User> users = TransactionProcessorSample.readUsers(usersFilePath);
        List<Transaction> transactions = TransactionProcessorSample.readTransactions(transactionsFilePath);
        List<BinMapping> binMappings = TransactionProcessorSample.readBinMappings(binMappingsFilePath);

        List<Event> events = TransactionProcessorSample.processTransactions(users, transactions, binMappings);

      
        
         TransactionProcessorSample.writeBalances(balancesFilePath, users);
        TransactionProcessorSample.writeEvents(eventsFilePath, events);}
        catch (IOException e) {
            e.printStackTrace();
        }
    }



  private static List<User> readUsers(final Path filePath) {
    List<User> users = new ArrayList<>();
    try (Stream<String> lines = Files.lines(filePath)) {
        lines.skip(1) // Skip the header line
                .forEach(line -> {
                    String[] userData = line.split(",");
                    int userId = Integer.parseInt(userData[0]);
                    String username = userData[1];
                    double balance = Double.parseDouble(userData[2]);
                    String country = userData[3];
                    FrozenStatus frozen = FrozenStatus.values()[Integer.parseInt(userData[4])];
                    double depositMin = Double.parseDouble(userData[5]);
                    double depositMax = Double.parseDouble(userData[6]);
                    double withdrawMin = Double.parseDouble(userData[7]);
                    double withdrawMax = Double.parseDouble(userData[8]);

                    users.add(new User(userId, username, balance, country, frozen, depositMin, depositMax, withdrawMin, withdrawMax));
                });
    } catch (IOException e) {
        e.printStackTrace();
    }
    return users;
}


 private static List<Transaction> readTransactions(final Path filePath) {
    List<Transaction> transactions = new ArrayList<>();
    try (Stream<String> lines = Files.lines(filePath)) {
        lines.skip(1) // Skip the header line
                .forEach(line -> {
                    String[] transactionData = line.split(",");
                    String transactionId = transactionData[0];
                    int userId = Integer.parseInt(transactionData[1]);
                    Transaction.Type type = Transaction.Type.valueOf(transactionData[2]);
                    double amount = Double.parseDouble(transactionData[3]);
                    Transaction.Method method = Transaction.Method.valueOf(transactionData[4]);
                    String accountNumber = transactionData[5];

                    transactions.add(new Transaction(transactionId, userId, type, amount, method, accountNumber));
                });
    } catch (IOException e) {
        e.printStackTrace();
    }
    return transactions;
}


 private static List<BinMapping> readBinMappings(final Path filePath) {
    List<BinMapping> binMappings = new ArrayList<>();
    try (Stream<String> lines = Files.lines(filePath)) {
        lines.skip(1) // Skip the header line
                .forEach(line -> {
                    String[] binData = line.split(",");
                    String name = binData[0];
                    long rangeFrom = Long.parseLong(binData[1]);
                    long rangeTo = Long.parseLong(binData[2]);
                    BinMapping.Type type = BinMapping.Type.valueOf(binData[3]);
                    String country = binData[4];

                    binMappings.add(new BinMapping(name, rangeFrom, rangeTo, type, country));
                });
    } catch (IOException e) {
        e.printStackTrace();
    }
    return binMappings;
}


    private static List<Event> processTransactions(final List<User> users, final List<Transaction> transactions, final List<BinMapping> binMappings) {
        // ToDo Implementation
        return null;
    }

    private static void writeBalances(final Path filePath, final List<User> users) {
        // ToDo Implementation
    }

    private static void writeEvents(final Path filePath, final List<Event> events) throws IOException {
        try (final FileWriter writer = new FileWriter(filePath.toFile(), false)) {
            writer.append("transaction_id,status,message\n");
            for (final var event : events) {
                writer.append(event.transactionId).append(",").append(event.status).append(",").append(event.message).append("\n");
            }
        }
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
        ACTIVE(0), FROZEN(1);

        private final int value;

        FrozenStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    // Constructor
    public User(int userId, String username, double balance, String country, FrozenStatus frozen, double depositMin, double depositMax, double withdrawMin, double withdrawMax) {
        this.userId = userId;
        this.username = username;
        this.balance = balance;
        this.country = country;
        this.frozen = frozen;
        this.depositMin = depositMin;
        this.depositMax = depositMax;
        this.withdrawMin = withdrawMin;
        this.withdrawMax = withdrawMax;
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
}
