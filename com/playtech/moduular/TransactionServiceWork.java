package com.playtech.assignment.moduular;

import com.playtech.assignment.moduular.task.model.BinMapping;
import com.playtech.assignment.moduular.task.model.Event;
import com.playtech.assignment.moduular.task.model.Transaction;
import com.playtech.assignment.moduular.task.model.User;
import com.playtech.assignment.moduular.task.service.BinMappingService;
import com.playtech.assignment.moduular.task.service.BinMappingServiceWork;
import com.playtech.assignment.moduular.task.util.Config;
import com.playtech.assignment.moduular.task.util.CsvWriter;
import com.playtech.assignment.moduular.task.util.IbanValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.playtech.assignment.moduular.task.util.CountryCodeConverter.toAlpha3;
import  static com.playtech.assignment.moduular.task.util.CsvReader.readFromCSV;



public class TransactionServiceWork {


    public static void main(String[] args) throws IOException {


        // Find the test_data directory
        // Use Config constants instead of string literals
        String baseDir = Config.BASE_DIR;
        String testDirName = Config.TEST_DIR_NAME; // This is dynamic
        String inputDir = Config.INPUT_DIR;
        String outputDir = Config.OUTPUT_DIR;

        // Find the test_data directory
        try (Stream<Path> paths = Files.walk(Paths.get(baseDir))) {
            Path testDirPath = paths
                    .filter(Files::isDirectory)
                    // Debug: Print each directory found to see what the stream is processing.
                    .peek(path -> System.out.println(STR."Found directory: \{path.toString()}"))
                    .filter(path -> path.getFileName().toString().trim().toLowerCase().contains(testDirName))
                    .findFirst()
                    .orElseThrow(() -> new IOException("Test directory not found"));

            // Construct the paths to the files using Config constants
            Path usersFilePath = testDirPath.resolve(Paths.get(inputDir, "users.csv"));
            Path transactionsFilePath = testDirPath.resolve(Paths.get(inputDir, "transactions.csv"));
            Path binMappingsFilePath = testDirPath.resolve(Paths.get(inputDir, "bins.csv"));
            Path balancesFilePath = testDirPath.resolve(Paths.get(outputDir, "balances.csv"));
            Path eventsFilePath = testDirPath.resolve(Paths.get(outputDir, "events.csv"));

                  List<User> users = TransactionServiceWork.readUsers(usersFilePath);

            // Print the first 10 users
            users.stream().limit(100).forEach(System.out::println);
//  .distinct()


            List<Transaction> transactions = TransactionServiceWork.readTransactions(transactionsFilePath);
            // Print the first 10 transactions
            transactions.stream().limit(10).forEach(System.out::println);


            List<BinMapping> binMappings = TransactionServiceWork.readBinMappings(binMappingsFilePath);

            users.stream().limit(100).forEach(user -> {
                String alpha2Code = user.getCountry();
                String alpha3Code = toAlpha3(alpha2Code); // This converts it to a 3-letter code
                System.out.println("(" + alpha2Code + ", " + alpha3Code + ")");
            });


            // Print the first 10 bin mappings
            binMappings.stream().limit(10).forEach(System.out::println);
            List<Event> events = TransactionServiceWork.processTransactions(users, transactions, binMappings);
            events.stream().limit(100).forEach(System.out::println);

            CsvWriter.writeBalances(balancesFilePath, users);


            CsvWriter.writeEvents(eventsFilePath, events);
        }

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
                        (existing, _) -> existing, TreeMap::new));


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
                    if (!IbanValidator.isValidIban(transaction.getAccountNumber())) {
                        events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Invalid IBAN for transfer"));
                        continue;
                    }
                } else if (transaction.getMethod() == Transaction.Method.CARD) {

                    BinMappingService binMappingService= new BinMappingServiceWork();

                    Optional<BinMapping> binMapping = binMappingService.findBinMapping(binMappingMap, transaction.getAccountNumber());
                    if (binMapping.isEmpty() || binMapping.get().getType() != BinMapping.Type.DC) {
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
                System.err.println(STR."Error parsing account number for transaction ID \{transaction.getTransactionId()}: \{e.getMessage()}");
                events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Invalid account number format"));
            } catch (Exception e) {
                System.err.println(STR."Unexpected error processing transaction ID \{transaction.getTransactionId()}: \{e.getMessage()}");
            }
        }

        return events;
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

    private static List<User> readUsers(final Path filePath) throws IOException {
        return readFromCSV(filePath.toString(), arr -> {
            int userId = Integer.parseInt(arr[0]);
            String username = arr[1];
            double balance = Double.parseDouble(arr[2]);
            String country = arr[3];
            User.FrozenStatus frozen = User.FrozenStatus.fromValue(Integer.parseInt(arr[4]));
            double depositMin = Double.parseDouble(arr[5]);
            double depositMax = Double.parseDouble(arr[6]);
            double withdrawMin = Double.parseDouble(arr[7]);
            double withdrawMax = Double.parseDouble(arr[8]);
            return new User(userId, username, balance, country, frozen, depositMin, depositMax, withdrawMin, withdrawMax);
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


}


