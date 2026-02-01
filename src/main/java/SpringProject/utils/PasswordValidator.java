package SpringProject.utils;

public class PasswordValidator {

    private static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$";

    //REGEX above: Include Number/Digit (Compulsary)
    //REGEX below: Include Number/Digit (optional)
    //"^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9])(?=.*\\d)?.{8,}$";*/
    public static boolean isValidPassword(String password) {
        return password != null && password.matches(PASSWORD_REGEX);
    }

    public static void main(String[] args) {
        System.out.println("****** TESTING isValidPassword() ******* ");
        System.out.println(isValidPassword("Test@123")); // true
        System.out.println(isValidPassword("test123"));  // false
        System.out.println(isValidPassword("testtest"));  // false
        System.out.println(isValidPassword("test@test"));  // false
        System.out.println(isValidPassword("123#1234"));  // false
        System.out.println(isValidPassword("Kilo:123"));  // true
        System.out.println(isValidPassword("test%Test"));  // True
        System.out.println(isValidPassword("testTest"));  // False
    }
}
