package io.syndesis.integration.runtime.util;

public class StringHelpers {
    public static Object sanitizeForURI(Object token){
        if (token == null) {
            return null;
        }
        if(!(token instanceof String)){
            return token;
        }
        String string = (String) token;

        // new_line is an illegal character in URIs. Camel does not sanitize them at Framework level, so we do it explicitely here.
        return string.replaceAll("\n", "%0A");
    }
}
