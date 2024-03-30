// package com.playtech.assignment;

    
// import org.junit.Test;
//    import static org.junit.Assert.*;
// public class TransactionProcessorSampleTest {
    

//         // The code successfully reads the users.csv file and creates a list of User objects.
//         @Test
//         public void test_readUsers() throws IOException {
//             // Arrange
//             String filePath = "path/to/users.csv";
//             List<User> expectedUsers = new ArrayList<>();
//             expectedUsers.add(new User(1, "user1", 100.0, "country1", FrozenStatus.ACTIVE_USER, 10.0, 1000.0, 5.0, 500.0));
//             expectedUsers.add(new User(2, "user2", 200.0, "country2", FrozenStatus.FROZEN_USER, 20.0, 2000.0, 10.0, 1000.0));
        
//             // Act
//             List<User> actualUsers = TransactionProcessorSample.readUsers(Paths.get(filePath));
        
//             // Assert
//             assertEquals(expectedUsers, actualUsers);
//         }
    
//         // The users.csv file is empty.
//         @Test
//         public void test_readUsers_emptyFile() throws IOException {
//             // Arrange
//             String filePath = "path/to/empty_users.csv";
//             List<User> expectedUsers = new ArrayList<>();
        
//             // Act
//             List<User> actualUsers = TransactionProcessorSample.readUsers(Paths.get(filePath));
        
//             // Assert
//             assertEquals(expectedUsers, actualUsers);
//         }
//     }
// }


// import com.playtech.assignment.User.FrozenStatus;

