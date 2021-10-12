/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 NBCO YooMoney LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.yoo.money.api.net;

import com.yoo.money.api.util.Strings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

import static com.yoo.money.api.util.Common.checkNotNull;

/**
 * Buffers request parameters and creates request body for different methods. It also encodes keys
 * and values if needed using UTF-8 charset.
 *
 * @author Slava Yasevich (support@yoomoney.ru)
 */
public final class ParametersBuffer {

    private static final String UTF8_NAME = "UTF-8";
    static final Charset UTF8_CHARSET = Charset.forName(UTF8_NAME);

    private Map<String, String> params = Collections.emptyMap();

    /**
     * Encodes string value to UTF-8 byte array.
     *
     * @param value value to encode
     * @return UTF-8 byte array
     */
    public static byte[] encodeUtf8(String value) {
        try {
            return encode(value).getBytes(UTF8_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets parameters.
     *
     * @param params key-value pairs of parameters (not null)
     * @return itself
     */
    public ParametersBuffer setParameters(Map<String, String> params) {
        this.params = checkNotNull(params, "params");
        return this;
    }

    /**
     * Prepares part of url for get request.
     * <p>
     * For example, there are two parameters provided:
     * <p>
     * {@code Map<String, String> params = new HashMap<>();}<br/>
     * {@code params.put("key1", "value1");}<br/>
     * {@code params.put("key2", "value2");}
     * <p>
     * Then the method will return a string "?key1=value1&key2=value2".
     *
     * @return url parameters
     */
    public String prepareGet() {
        GetBuffer buffer = new GetBuffer();
        iterate(buffer);
        return buffer.toString();
    }

    /**
     * Prepares parameters for a request as bytes.
     * <p>
     * For example, there are two parameters provided:
     * <p>
     * {@code Map<String, String> params = new HashMap<>();}<br/>
     * {@code params.put("key1", "value1");}<br/>
     * {@code params.put("key2", "value2");}
     * <p>
     * Then the method will return byte array containing "?key1=value1&key2=value2".
     *
     * @return byte array of parameters
     */
    public byte[] prepareBytes() {
        PostBuffer buffer = new PostBuffer();
        iterate(buffer);
        return buffer.getBytes();
    }

    static String encode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, UTF8_CHARSET.name());
    }

    private void iterate(Buffer buffer) {
        for (Map.Entry<String, String> param : params.entrySet()) {
            String key = param.getKey();
            if (Strings.isNullOrEmpty(key)) {
                continue; // ignore empty keys
            }

            String value = param.getValue();
            if (Strings.isNullOrEmpty(value)) {
                continue;
            }

            buffer.nextParameter(key, value);
        }
    }

    private interface Buffer {
        void nextParameter(String key, String value);
    }

    private static final class GetBuffer implements Buffer {

        public final StringBuilder builder = new StringBuilder();

        GetBuffer() {
        }

        @Override
        public void nextParameter(String key, String value) {
            try {
                builder.append(builder.length() == 0 ? '?' : '&')
                        .append(encode(key))
                        .append('=')
                        .append(encode(value));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String toString() {
            return builder.toString();
        }
    }

    private static final class PostBuffer implements Buffer {

        private static final byte[] AMPERSAND = "&".getBytes(UTF8_CHARSET);
        private static final byte[] EQUALS_SIGN = "=".getBytes(UTF8_CHARSET);

        private final ByteArrayOutputStream stream = new ByteArrayOutputStream();

        PostBuffer() {
        }

        @Override
        public void nextParameter(String key, String value) {
            try {
                if (stream.size() > 0) {
                    stream.write(AMPERSAND);
                }
                stream.write(encodeUtf8(key));
                stream.write(EQUALS_SIGN);
                stream.write(encodeUtf8(value));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        byte[] getBytes() {
            return stream.toByteArray();
        }
    }
}
