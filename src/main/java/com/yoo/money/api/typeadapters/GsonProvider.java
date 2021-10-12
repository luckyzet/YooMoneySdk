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

package com.yoo.money.api.typeadapters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yoo.money.api.time.DateTime;
import com.yoo.money.api.time.YearMonth;

import java.lang.reflect.Type;

/**
 * Provides a single GSON instance to serialize / deserialize any object within this SDK.
 */
public final class GsonProvider {

    private static final GsonBuilder BUILDER = new GsonBuilder();
    static {
        BUILDER.registerTypeAdapter(DateTime.class, new DateTimeTypeAdapter());
        BUILDER.registerTypeAdapter(YearMonth.class, new YearMonthTypeAdapter());
    }

    private static Gson gson = BUILDER.create();
    private static boolean hasNewTypeAdapter = false;

    /**
     * Gets actual instance of GSON. If necessary rebuilds it to add new type adapters.
     *
     * @return instance of GSON
     */
    public static synchronized Gson getGson() {
        if (hasNewTypeAdapter) {
            gson = BUILDER.create();
            hasNewTypeAdapter = false;
        }
        return gson;
    }

    /**
     * Registers type adapter to use with GSON instance.
     *
     * @param type type for which the type adapter is registered
     * @param typeAdapter type adapter
     */
    @SuppressWarnings("WeakerAccess")
    public static synchronized void registerTypeAdapter(Type type, Object typeAdapter) {
        BUILDER.registerTypeAdapter(type, typeAdapter);
        hasNewTypeAdapter = true;
    }
}
