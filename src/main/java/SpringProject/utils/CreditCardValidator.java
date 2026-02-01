package SpringProject.utils;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class CreditCardValidator {

    /**
     * Validates information by checking the format of the details.
     */
    private static final String cardNum = "^(\\d{16})$";
    private static final String expireDate = "^(0[1-9]|1[0-2])/\\d{2}$"; // MM/YY
    private static final String cvv = "^\\d{3,4}$";


    /**
     * @param cardNumber the credit card number has to be exactly 16 digits.
     * @param expireD    the expiry date in a MM/YY format, from (01-12) and YY is the last two numbers of a year.
     * @param cvv1       a 3 or 4 digit security code.
     * @return {true} if all three pieces of information match the expected format. {false} otherwise
     */
    public  static boolean validateCreditCardInfo(String cardNumber, String expireD, String cvv1) {

        //  Parse MM/YY
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
        YearMonth enteredDate = YearMonth.parse(expireD, formatter);

        //  Get current month/year
        YearMonth currentDate = YearMonth.now();
        return cardNumber.matches(cardNum) &&
                //expireD.matches(expireDate) &&
                //  Expiry date must be current or future
                !enteredDate.isBefore(currentDate) &&
                cvv1.matches(cvv);
    }
    public static void main(String[] args){
        String cardNum = "1234567890123456";
        String expireD = "06/26";
        String cvv1 = "123";
        System.out.println(validateCreditCardInfo(cardNum, expireD, cvv1)); // true
        System.out.println( cardNum.matches(cardNum)); // true
        System.out.println(expireD.matches(expireDate)); // true
        System.out.println(cvv1.matches(cvv)); // true
    }
}
