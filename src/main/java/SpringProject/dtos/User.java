package SpringProject.dtos;

/******************************************
 * @Author: Julie Olamijuwon
 * @StudentID: D00215779
 * @Date:   October 2025
 ******************************************/

import lombok.*;

//import java.time.LocalDate;
import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder



public class User implements Comparable<User>{
    //*CREATE TABLE users (
    //    userId     INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    //    username   VARCHAR(50) NOT NULL UNIQUE,
    //    password   VARCHAR(60) NOT NULL,
    //    email      VARCHAR(100) NOT NULL UNIQUE,
    //    userType   INT(11) DEFAULT 1
    //);

    // Annotate all fields that cannot be null with NonNull
    // Don't include any auto-generating primary key fields as these may not be known when the object is created

    private int userId;
    @EqualsAndHashCode.Include
    private  String username;
    @NonNull
    private  String password;
    @NonNull
    private  String email;
    //@NonNull
   // @NonBlank
    private int userType;

    /**
     * Returns a formatted, human-readable representation of the user.
     *
     * @return a formatted  String containing user information
     */
    public String format() {
        String formattedText = userId + ": " + username
                + "\n\t" + password + ", " + email
                + "\n\t " + userType;


        return formattedText;
    }
    /**
     * Performs a deep comparison between two User objects.
     * This method compares all business-relevant fields except  userId.
     *
     * @param u1 the first user to compare
     * @param u2 the second user to compare
     * @return  true if the users are logically equal,  false otherwise
     */

    public static boolean deepEquals(User u1, User u2){
        return Objects.equals(u1.username, u2.username)
                && Objects.equals(u1.password, u2.password)
                && Objects.equals(u1.email, u2.email)
                && Objects.equals(u1.userType, u2.userType);


    }
    /**
     * Compares this user with another user based on  userId.
     *
     * @param u the user to compare against
     * @return a negative integer, zero, or a positive integer as this user's
     *         ID is less than, equal to, or greater than the specified user's ID
     */
    @Override
    public int compareTo(User u) {
        if (userId < u.userId) {
            return -1;
        } else if (userId < u.userId) {
            return 1;
        }
        return 0;
    }
}
