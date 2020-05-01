package trim;

public class Trim {
    public static String trim(String in) {
        while (in.charAt(in.length() - 1) == '\n') {
            in = in.substring(0, in.length() - 1);
        }
        return in;
    }
}
