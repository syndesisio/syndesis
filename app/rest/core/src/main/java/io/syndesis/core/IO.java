package io.syndesis.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by chirino on 1/9/18.
 */
public class IO {

    public static byte[] readAllBytes(InputStream is) throws IOException {
        if( is == null ) {
            return null;
        }
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024 * 4];
            for (int len; (len = is.read(buffer)) != -1; ) { //NOPMD
                os.write(buffer, 0, len);
            }
            os.flush();
            return os.toByteArray();
        }
    }

    public static String utf8(byte[] data) {
        if( data ==null ) {
            return null;
        }
        return new String(data, StandardCharsets.UTF_8);
    }

}
