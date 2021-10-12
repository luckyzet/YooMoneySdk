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

package com.yoo.money.api.authorization;

import com.yoo.money.api.net.ParametersBuffer;

import java.util.Map;

/**
 * Basic implementation of {@link AuthorizationParameters}.
 */
final class AuthorizationParametersImpl implements AuthorizationParameters {

    private final Map<String, String> parameters;

    /**
     * Map of authorization parameters.
     *
     * @param parameters key-value pairs
     */
    AuthorizationParametersImpl(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public void add(String name, String value) {
        parameters.put(name, value);
    }

    @Override
    public byte[] build() {
        return new ParametersBuffer()
                .setParameters(parameters)
                .prepareBytes();
    }

    @Override
    public String buildString() {
        StringBuilder builder = new StringBuilder();
        parameters.forEach((key, value) -> builder.append(key).append("=").append(value).append("&"));
        String result = builder.toString();
        if(result.endsWith("&")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
