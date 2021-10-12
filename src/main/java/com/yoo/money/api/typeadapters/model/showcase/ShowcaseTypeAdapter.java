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

package com.yoo.money.api.typeadapters.model.showcase;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.reflect.TypeToken;
import com.yoo.money.api.model.AllowedMoneySource;
import com.yoo.money.api.model.showcase.Showcase;
import com.yoo.money.api.model.showcase.Showcase.Error;
import com.yoo.money.api.model.showcase.ShowcaseReference;
import com.yoo.money.api.model.showcase.components.containers.Group;
import com.yoo.money.api.typeadapters.BaseTypeAdapter;
import com.yoo.money.api.typeadapters.model.showcase.container.GroupTypeAdapter;
import com.yoo.money.api.typeadapters.model.showcase.container.GroupTypeAdapter.ListDelegate;

import java.lang.reflect.Type;
import java.util.List;

import static com.yoo.money.api.typeadapters.JsonUtils.getNotNullMap;
import static com.yoo.money.api.typeadapters.JsonUtils.getString;
import static com.yoo.money.api.typeadapters.JsonUtils.toJsonObject;

/**
 * Type adapter for {@link Showcase}.
 *
 * @author Anton Ermak (support@yoomoney.ru)
 */
public final class ShowcaseTypeAdapter extends BaseTypeAdapter<Showcase> {

    private static final ShowcaseTypeAdapter INSTANCE = new ShowcaseTypeAdapter();

    private static final String MEMBER_ERROR = "error";
    private static final String MEMBER_FORM = "form";
    private static final String MEMBER_HIDDEN_FIELDS = "hidden_fields";
    private static final String MEMBER_MONEY_SOURCE = "money_source";
    private static final String MEMBER_TITLE = "title";
    private static final String MEMBER_BONUS = "bonus_points";

    private ShowcaseTypeAdapter() {
        //noinspection ResultOfMethodCallIgnored
        GroupTypeAdapter.getInstance();
    }

    /**
     * @return instance of this class
     */
    public static ShowcaseTypeAdapter getInstance() {
        return INSTANCE;
    }

    @Override
    public Showcase deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject object = json.getAsJsonObject();

        Group form = null;
        JsonArray array = object.getAsJsonArray(MEMBER_FORM);
        if (array != null) {
            form = ListDelegate.deserialize(array, context);
        }

        List<AllowedMoneySource> moneySources = context.deserialize(object.get(MEMBER_MONEY_SOURCE),
                new TypeToken<List<AllowedMoneySource>>() {}.getType());
        List<Error> errors = ErrorTypeAdapter.getInstance().fromJson(object.getAsJsonArray(MEMBER_ERROR));

        List<ShowcaseReference.BonusOperationType> bonusPoints = context.deserialize(
                object.get(MEMBER_BONUS),
                new TypeToken<List<ShowcaseReference.BonusOperationType>>() {}.getType()
        );

        return new Showcase.Builder()
                .setTitle(getString(object, MEMBER_TITLE))
                .setHiddenFields(getNotNullMap(object, MEMBER_HIDDEN_FIELDS))
                .setForm(form)
                .setMoneySources(toEmptyListIfNull(moneySources))
                .setErrors(toEmptyListIfNull(errors))
                .setBonusPoints(toEmptyListIfNull(bonusPoints))
                .create();
    }

    @Override
    public JsonElement serialize(Showcase src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject objects = new JsonObject();
        objects.addProperty(MEMBER_TITLE, src.title);
        objects.add(MEMBER_MONEY_SOURCE, context.serialize(src.moneySources));
        if (!src.errors.isEmpty()) {
            objects.add(MEMBER_ERROR, ErrorTypeAdapter.getInstance().toJsonArray(src.errors));
        }
        if (src.form != null) {
            objects.add(MEMBER_FORM, ListDelegate.serialize(src.form, context));
        }
        objects.add(MEMBER_HIDDEN_FIELDS, toJsonObject(src.hiddenFields));
        objects.add(MEMBER_BONUS, context.serialize(src.bonusPoints));
        return objects;
    }

    @Override
    public Class<Showcase> getType() {
        return Showcase.class;
    }

    private static final class ErrorTypeAdapter extends BaseTypeAdapter<Error> {

        private static final ErrorTypeAdapter INSTANCE = new ErrorTypeAdapter();

        private static final String MEMBER_ALERT = "alert";
        private static final String MEMBER_NAME = "name";

        public static ErrorTypeAdapter getInstance() {
            return INSTANCE;
        }

        @Override
        public Error deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {

            JsonObject jsonObject = json.getAsJsonObject();
            return new Error(getString(jsonObject, MEMBER_NAME), getString(jsonObject, MEMBER_ALERT));
        }

        @Override
        public JsonElement serialize(Error src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(MEMBER_NAME, src.name);
            jsonObject.addProperty(MEMBER_ALERT, src.alert);
            return jsonObject;
        }

        @Override
        public Class<Error> getType() {
            return Error.class;
        }
    }
}
