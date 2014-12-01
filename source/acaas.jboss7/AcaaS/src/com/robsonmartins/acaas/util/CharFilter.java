package com.robsonmartins.acaas.util;

import java.util.regex.Matcher;  
import java.util.regex.Pattern;  

/**
 * Realiza processamento de caracteres em strings. 
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public class CharFilter {  

	/** Caracteres sem acentuacao. */
    private final static String[] REPLACES = { "a", "e", "i", "o", "u", "c", "n",
    	                                       "A", "E", "I", "O", "U", "C", "N"};
    /** Patterns para substituir acentuacao. */
    private static Pattern[] PATTERNS = null;  
      
    static {  
        PATTERNS = new Pattern[REPLACES.length];  
        PATTERNS[0] = Pattern.compile("[\u00E1\u00E0\u00E3\u00E2\u00E4\u00AA]", Pattern.UNICODE_CASE);  
        PATTERNS[1] = Pattern.compile("[\u00E9\u00E8\u00EA\u1EBD\u00EB]", Pattern.UNICODE_CASE);  
        PATTERNS[2] = Pattern.compile("[\u00ED\u00EC\u00EE\u0129\u00EF]", Pattern.UNICODE_CASE);  
        PATTERNS[3] = Pattern.compile("[\u00F3\u00F2\u00F4\u00F5\u00F6\u00BA\u00B0]", Pattern.UNICODE_CASE);  
        PATTERNS[4] = Pattern.compile("[\u00FA\u00F9\u00FB\u0169\u00FC]", Pattern.UNICODE_CASE);  
        PATTERNS[5] = Pattern.compile("[\u00E7\u0109]", Pattern.UNICODE_CASE);  
        PATTERNS[6] = Pattern.compile("[\u0144\u01F9\u00F1]", Pattern.UNICODE_CASE);  
        PATTERNS[7] = Pattern.compile("[\u00C1\u00C0\u00C2\u00C3\u00C4]", Pattern.UNICODE_CASE);  
        PATTERNS[8] = Pattern.compile("[\u00C9\u00C8\u00CA\u1EBC\u00CB]", Pattern.UNICODE_CASE);  
        PATTERNS[9] = Pattern.compile("[\u00CD\u00CC\u00CE\u0128\u00CF]", Pattern.UNICODE_CASE);  
        PATTERNS[10] = Pattern.compile("[\u00D3\u00D2\u00D4\u00D5\u00D6]", Pattern.UNICODE_CASE);  
        PATTERNS[11] = Pattern.compile("[\u00DA\u00D9\u00DB\u0168\u00DC]", Pattern.UNICODE_CASE);  
        PATTERNS[12] = Pattern.compile("[\u00C7\u0108]", Pattern.UNICODE_CASE);  
        PATTERNS[13] = Pattern.compile("[\u0143\u01F8\u00D1]", Pattern.UNICODE_CASE);
    }  

    /**
     * Remove acentuacao em uma string.
     * @param text String de origem.
     * @return Texto sem acentuacao.
     */
    public static String removeAccents(String text) {  
        String result = text;  
        for (int i = 0; i < PATTERNS.length; i++) {  
            Matcher matcher = PATTERNS[i].matcher(result);  
            result = matcher.replaceAll(REPLACES[i]);  
        }  
        return result;  
    }  

    /**
     * Codifica uma string contendo uma URL.
     * @param input String de origem.
     * @return Texto processado, utilizando notacao segura para URL. 
     */
    public static String encodeURLParam(String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (isUnsafe(ch)) {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            } else {
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }

    /**
     * Converte um caracter para notacao hexadecimal.
     * @param ch Caracter.
     * @return Caracter hexadecimal.
     */
    private static char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    /**
     * Retorna se um caracter e' inseguro para uma URL.
     * @param ch Caracter.
     * @return True se caracter for inseguro.
     */
    private static boolean isUnsafe(char ch) {
        if (ch > 128 || ch < 0)
            return true;
        return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
    }
    
}
