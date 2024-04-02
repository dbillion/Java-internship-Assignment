package com.dayofinal.assignment;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.regex.Pattern;
import com.dayofinal.assignment.User.FrozenStatus;



// This template shows input parameters format.
// It is otherwise not mandatory to use, you can write everything from scratch if you wish.
public class CSVReader {
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

    
        List<User> users = CSVReader.readUsers(usersFilePath);

         // Print the first 10 users
 users.stream().limit(10).forEach(System.out::println);


        List<Transaction> transactions = CSVReader.readTransactions(transactionsFilePath);
         // Print the first 10 transactions
 transactions.stream().limit(10).forEach(System.out::println);


        List<BinMapping> binMappings = CSVReader.readBinMappings(binMappingsFilePath);

   

 // Print the first 10 bin mappings
 binMappings.stream().limit(10).forEach(System.out::println);
        List<Event> events = CSVReader.processTransactions(users, transactions, binMappings);
        events.stream().limit(100).forEach(System.out::println);
      
        
         CSVReader.writeBalances(balancesFilePath, users);

        CSVReader.writeEvents(eventsFilePath, events);}

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
                } else {
                    events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Unsupported payment method"));
                    continue;
                }
    
                // Validate transaction amount
                double amount = transaction.getAmount();
                if (amount <= 0) {
                    events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Invalid transaction amount"));
                    continue;
                }
                // Further validation for deposit/withdrawal limits
                // Example for deposit limit check
                if (transaction.getType() == Transaction.Type.DEPOSIT && (amount < user.getDepositMin() || amount > user.getDepositMax())) {
                    events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Transaction amount outside deposit limits"));
                    continue;
                }
    
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
                    } else {
                        // Deduct the withdrawal amount from the user's balance
                        user.setBalance(user.getBalance() - transaction.getAmount());
                    }
                } else {
                    events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Unsupported transaction type"));
                    continue;
                }
    
                // If all validations pass, process the transaction
                user.setBalance(user.getBalance() + transaction.getAmount());
                events.add(new Event(transaction.getTransactionId(), Event.STATUS_APPROVED, "Transaction approved"));
    
            } catch (NumberFormatException e) {
                System.err.println("Error parsing account number for transaction ID " + transaction.getTransactionId() + ": " + e.getMessage());
                events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Invalid account number format"));
            } catch (Exception e) {
                System.err.println("Unexpected error processing transaction ID " + transaction.getTransactionId() + ": " + e.getMessage());
            }
        } // This closing brace was missing
    
        return events;
    }
    

private static Optional<BinMapping> findBinMapping(TreeMap<Long, BinMapping> binMappingMap, String accountNumber) {
    try {
        long accountNumberLong = Long.parseLong(accountNumber);
        return Optional.ofNullable(binMappingMap.floorEntry(accountNumberLong)).map(Map.Entry::getValue);
    } catch (NumberFormatException e) {
        return Optional.empty();
    }
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

private static List<BinMapping> readBinMappings(final Path filePath) throws IOException {
    return readFromCSV(filePath.toString(), arr -> {
        String name = arr[0];
        long rangeFrom = Long.parseLong(arr[1]);
        long rangeTo = Long.parseLong(arr[2]);
        BinMapping.Type type = BinMapping.Type.valueOf(arr[3].toUpperCase());
        String country = arr[4];
        return new BinMapping(name, rangeFrom, rangeTo, type, country);
    });
}

private static List<Transaction> readTransactions(final Path filePath) throws IOException {
    return readFromCSV(filePath.toString(), arr -> {
        String transactionId = arr[0];
        int userId = Integer.parseInt(arr[1]);
        Transaction.Type type = Transaction.Type.valueOf(arr[2].toUpperCase());
        double amount = Double.parseDouble(arr[3]);
        Transaction.Method method = Transaction.Method.valueOf(arr[4].toUpperCase());
        String accountNumber = arr[5];
        return new Transaction(transactionId, userId, type, amount, method, accountNumber);
    });
}

private static List<User> readUsers(final Path filePath) throws IOException {
    return readFromCSV(filePath.toString(), arr -> {
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

private static final Map<String, String> alpha2ToAlpha3 = new HashMap<String, String>() {{
    put("US", "USA");
    put("GB", "GBR");
    // Add more country codes here...
}};

static String toAlpha3(String alpha2Code) {
    return alpha2ToAlpha3.getOrDefault(alpha2Code, alpha2Code);
}

static String toAlpha2(String alpha3Code) {
    return alpha2ToAlpha3.entrySet().stream()
            .filter(entry -> entry.getValue().equals(alpha3Code))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(alpha3Code);
}


public static boolean isValidIban(String iban) {
    String regex = "^([A-Z]{2}[ \\-]?[0-9]{2})(?=(?:[ \\-]?[A-Za-z0-9]){9,30}$)((?:[ \\-]?[A-Za-z0-9]{3,5}){2,7})([ \\-]?[A-Za-z0-9]{1,3})?$";
    Pattern pattern = Pattern.compile(regex);
    return pattern.matcher(iban).matches();
}


    private static void writeBalances(final Path filePath, final List<User> users) {

        try (final FileWriter writer = new FileWriter(filePath.toFile(), false)) {
            // Write the header line
            writer.append("user_id,balance\n");
            // Iterate over each user and write their ID and balance to the file
            for (final User user : users) {
                writer.append(String.valueOf(user.getUserId()))
                      .append(",")
                      .append(String.format("%.2f", user.getBalance())) // Format balance to ensure two decimal places
                      .append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            
        }
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
// User Interface
interface IUser {
    boolean isFrozen();
    void deposit(double amount);
    void withdraw(double amount);
    // ... other methods
}

// Transaction Interface
interface ITransaction {
    // ... methods
}

// BinMapping Interface
interface IBinMapping {
    // ... methods
}

// Event Interface
interface IEvent {
    // ... methods
}

// User class now implements IUser
class User implements IUser {
    // ... existing code
}

// Transaction class now implements ITransaction
class Transaction implements ITransaction {
    // ... existing code
}

// BinMapping class now implements IBinMapping
class BinMapping implements IBinMapping {
    // ... existing code
}

// Event class now implements IEvent
class Event implements IEvent {
    // ... existing code
}

