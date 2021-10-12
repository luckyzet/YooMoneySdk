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

package com.yoo.money.api.model.showcase.components.containers;

import com.yoo.money.api.model.showcase.components.Component;
import com.yoo.money.api.model.showcase.components.Parameter;
import com.yoo.money.api.model.showcase.components.uicontrols.Select;
import com.yoo.money.api.util.Enums;

import java.util.Map;

import static com.yoo.money.api.util.Common.checkNotNull;

/**
 * A {@link Group} is implementation of a {@link Component} that can contain only {@link Component}
 * instances.
 */
public class Group extends Container<Component> {

    /**
     * {@link Layout}.
     */
    public final Layout layout;

    @SuppressWarnings("WeakerAccess")
    protected Group(Builder builder) {
        super(builder);
        layout = checkNotNull(builder.layout, "layout");
    }

    /**
     * Fills specified map with values from group's controls.
     *
     * @param map map to fill
     * @param group group to traverse
     */
    public static void fillMapWithValues(Map<String, String> map, Group group) {
        for (Component component : group.items) {
            if (component instanceof Group) {
                fillMapWithValues(map, (Group) component);
            } else if (component instanceof Parameter) {
                Parameter parameter = (Parameter) component;
                map.put(parameter.getName(), parameter.getValue());
                if (component instanceof Select) {
                    Select.Option option = ((Select) component).getSelectedOption();
                    if (option != null && option.group != null) {
                        fillMapWithValues(map, option.group);
                    }
                }
            }
        }
    }

    /**
     * Validates contained components across constraints.
     *
     * @return {@code true} if group is valid and {@code false} otherwise.
     */
    @Override
    public boolean isValid() {
        for (Component component : items) {
            if (!component.isValid()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Group group = (Group) o;

        return layout == group.layout;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + layout.hashCode();
        return result;
    }

    /**
     * Possible options that specifies arrangement of contained {@link Component}s.
     */
    public enum Layout implements Enums.WithCode<Layout> {

        /**
         * Vertical arrangement.
         */
        VERTICAL("VBox"),

        /**
         * Horizontal arrangement.
         */
        HORIZONTAL("HBox");

        public final String code;

        Layout(String code) {
            this.code = code;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public Layout[] getValues() {
            return values();
        }

        public static Layout parse(String code) {
            return Enums.parse(VERTICAL, VERTICAL, code);
        }
    }

    /**
     * {@link Group} builder.
     */
    public static class Builder extends Container.Builder<Component> {

        Layout layout = Layout.VERTICAL;

        @Override
        public Group create() {
            return new Group(this);
        }

        @SuppressWarnings("UnusedReturnValue")
        public Builder setLayout(Layout layout) {
            this.layout = layout;
            return this;
        }
    }
}
