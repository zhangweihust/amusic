
package com.android.media.lyric.player;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LyricUtil {

    /**
     * String --> InputStream
     * @param str
     * @return
     */
    public static InputStream String2InputStream(String str) {
        ByteArrayInputStream stream = new ByteArrayInputStream(str.getBytes());
        return stream;
    }

    /**
     * InputStream --> String
     * @param is
     * @return
     * @throws IOException
     */
    public static String inputStream2String(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = in.readLine()) != null) {
            buffer.append(line);
            buffer.append("\n"); // 特别注意每一句的换行符号不能去掉。
        }
        return buffer.toString();
    }
}
