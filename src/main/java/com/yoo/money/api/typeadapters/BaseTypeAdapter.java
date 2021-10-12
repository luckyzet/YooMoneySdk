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

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.yoo.money.api.typeadapters.GsonProvider.getGson;
import static com.yoo.money.api.typeadapters.GsonProvider.registerTypeAdapter;

/**
 * Base class for type adapters.
 *
 * @author Slava Yasevich (support@yoomoney.ru)
 */
public abstract class BaseTypeAdapter<T> implements TypeAdapter<T>, JsonSerializer<T>, JsonDeserializer<T> {

    public BaseTypeAdapter() {
        registerTypeAdapter(getType(), this);
    }

    @Override
    public final T fromJson(String json) {
        return getGson().fromJson(json, getType());
    }

    @Override
    public T fromJson(InputStream inputStream) {
        return getGson().fromJson(new InputStreamReader(inputStream, Charset.forName("UTF-8")), getType());
    }

    @Override
    public final T fromJson(JsonElement element) {
        return getGson().fromJson(element, getType());
    }

    @Override
    public final List<T> fromJson(JsonArray array) {
        if (array == null) {
            return null;
        }
        List<T> items = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            items.add(fromJson(element));
        }
        return items;
    }

    @Override
    public final String toJson(T value) {
        return getGson().toJson(value);
    }

    @Override
    public final JsonElement toJsonTree(T value) {
        return getGson().toJsonTree(value);
    }

    @Override
    public JsonArray toJsonArray(Collection<T> values) {
        if (values == null) {
            return null;
        }
        JsonArray array = new JsonArray();
        for (T value : values) {
            array.add(toJsonTree(value));
        }
        return array;
    }

    protected static <T> List<T> toEmptyListIfNull(List<T> list) {
        return list == null ? Collections.<T>emptyList() : list;
    }

    protected static JsonArray toNullIfEmpty(JsonArray array) {
        return array.size() == 0 ? null : array;
    }

    protected abstract Class<T> getType();
}
