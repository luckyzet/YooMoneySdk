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

package com.yoo.money.api.model.showcase.components.uicontrols;

import java.math.BigDecimal;

/**
 * Numeric control.
 *
 * @author Aleksandr Ershov (support@yoomoney.ru)
 */
public class Number extends ParameterControl {

    /**
     * Minimum acceptable number.
     */
    public final BigDecimal min;

    /**
     * Maximum acceptable number.
     */
    public final BigDecimal max;

    /**
     * Step scale factor. The default is {@link BigDecimal#ONE}.
     */
    public final BigDecimal step;

    protected Number(Builder builder) {
        super(builder);
        if (builder.min != null && builder.max != null && builder.min.compareTo(builder.max) > 0) {
            throw new IllegalArgumentException("min > max");
        }
        min = builder.min;
        max = builder.max;
        step = builder.step;
    }

    @Override
    public boolean isValid(String value) {
        return super.isValid(value) && isValidInner(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Number number = (Number) o;

        return !(min != null ? !min.equals(number.min) : number.min != null) && !(max != null ? !max
                .equals(number.max) : number.max != null) && step.equals(number.step);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (min != null ? min.hashCode() : 0);
        result = 31 * result + (max != null ? max.hashCode() : 0);
        result = 31 * result + step.hashCode();
        return result;
    }

    private boolean isValidInner(String value) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        try {
            BigDecimal v = new BigDecimal(value);
            BigDecimal quotient = v.divideToIntegralValue(step);
            return (min == null || v.compareTo(min) >= 0) &&
                    (max == null || v.compareTo(max) <= 0) &&
                    v.compareTo(quotient.multiply(step)) == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * {@link Number} builder.
     */
    public static class Builder extends ParameterControl.Builder {

        protected BigDecimal min;
        protected BigDecimal max;
        protected BigDecimal step = BigDecimal.ONE;

        public Builder() {
            super();
        }

        protected Builder(BigDecimal min, BigDecimal max, BigDecimal step) {
            super();
            this.min = min;
            this.max = max;
            this.step = step;
        }

        @Override
        public Number create() {
            return new Number(this);
        }

        public Builder setMin(BigDecimal min) {
            this.min = min;
            return this;
        }

        public Builder setMax(BigDecimal max) {
            this.max = max;
            return this;
        }

        public final Builder setStep(BigDecimal step) {
            if (step != null) {
                this.step = step;
            }
            return this;
        }
    }
}
