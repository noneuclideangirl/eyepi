package net.noneuclideangirl.util;

public class Util {
    private Util() {}

    public static boolean isInteger(String str) {
        return str != null && str.matches("[1-9]\\d*");
    }
}
