public class Utils {
    public static boolean isInt(String string) {
        if (string.equals("")) {
            return false;
        }
        if (string.matches("[0-9]{1,8}"))
            return true;
        else return false;
    }
}
