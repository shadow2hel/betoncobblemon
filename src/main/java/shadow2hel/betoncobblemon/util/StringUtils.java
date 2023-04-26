package shadow2hel.betoncobblemon.util;

public final class StringUtils {

    public static int NextParamIndex(String input) {
        int openingCounter = 0;
        int closingCounter = 0;
        int separatorCounter = 0;
        int currentIndex = 0;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '[')
                openingCounter++;
            if (input.charAt(i) == ']')
                closingCounter++;
            if (input.charAt(i) == ',')
                separatorCounter++;
            if (separatorCounter > 0 && openingCounter == 0) {
                break;
            }
            if (closingCounter > 0 && closingCounter == openingCounter) {
                break;
            }
            currentIndex++;
        }
        if (input.length() == 0 || input.equals("],") || input.equals(",") || input.equals("]"))
            return -1;
        return currentIndex;
    }
}
