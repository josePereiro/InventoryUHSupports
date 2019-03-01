package TXTtoCSVConverter.RegexTools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTools {

    private static String numberLine =
            "Area de Responsabilidad:" +
                    "        " +
                    "01002-073 - ADMINISTRACION LOCAL 284/285" +
                    " \n";

    private static Pattern areaPattern = Pattern.compile("(Area de Responsabilidad:)([ ]+)([0-9]+-?[0-9]+)( - )(?<area>[[^ ]+[ ]+]*[^ ]+)");


    public static void main(String[] args) {


    }

}
