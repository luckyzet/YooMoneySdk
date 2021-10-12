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

package com.yoo.money.api.methods.wallet;

import com.google.gson.annotations.SerializedName;
import com.yoo.money.api.model.Error;
import com.yoo.money.api.model.Operation;
import com.yoo.money.api.net.FirstApiRequest;
import com.yoo.money.api.net.providers.HostsProvider;
import com.yoo.money.api.time.DateTime;
import com.yoo.money.api.util.Enums;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.yoo.money.api.util.Common.checkNotNull;

/**
 * Operation history.
 * <p/>
 * If successful contains list of operations as well as token to a next record if there are more
 * operations in a user's history.
 */
public class OperationHistory {

    @SerializedName("error")
    public final Error error;
    @SuppressWarnings("WeakerAccess")
    @SerializedName("next_record")
    public final String nextRecord;
    @SerializedName("operations")
    public final List<Operation> operations;

    /**
     * Constructor.
     *
     * @param error error code
     * @param nextRecord nextRecord marker used in subsequent request if needed
     * @param operations list of operations
     */
    public OperationHistory(Error error, String nextRecord, List<Operation> operations) {
        this.error = error;
        this.nextRecord = nextRecord;
        this.operations = operations != null ? Collections.unmodifiableList(operations) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OperationHistory that = (OperationHistory) o;

        if (error != that.error) return false;
        //noinspection SimplifiableIfStatement
        if (nextRecord != null ? !nextRecord.equals(that.nextRecord) : that.nextRecord != null) return false;
        return operations != null ? operations.equals(that.operations) : that.operations == null;
    }

    @Override
    public int hashCode() {
        int result = error != null ? error.hashCode() : 0;
        result = 31 * result + (nextRecord != null ? nextRecord.hashCode() : 0);
        result = 31 * result + (operations != null ? operations.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OperationHistory{" +
                "error=" + error +
                ", nextRecord='" + nextRecord + '\'' +
                ", operations=" + operations +
                '}';
    }

    /**
     * Filter types.
     */
    public enum FilterType implements Enums.WithCode<FilterType> {
        /**
         * Depositions.
         */
        DEPOSITION("deposition"),
        /**
         * Payments.
         */
        PAYMENT("payment"),
        /**
         * Unaccepted incoming transfers (e.g. p2p transfer with protection code).
         */
        INCOMING_TRANSFER_UNACCEPTED("incoming-transfers-unaccepted");

        public final String code;

        FilterType(String code) {
            this.code = code;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public FilterType[] getValues() {
            return values();
        }
    }

    /**
     * Requests for a list of operations in user's history.
     * <p/>
     * Authorized session required.
     */
    public static class Request extends FirstApiRequest<OperationHistory> {

        /**
         * Use builder to create the request.
         */
        Request(Set<FilterType> types, String label, DateTime from, DateTime till, String startRecord, Integer records,
                Boolean details, Boolean includeCurrencyExchanges) {

            super(OperationHistory.class);
            if (from != null && till != null && from.isAfter(till)) {
                throw new IllegalArgumentException("\'from\' should be before \'till\'");
            }
            if (records != null) {
                if (records < 1) {
                    records = 1;
                } else if (records > 100) {
                    records = 100;
                }
            }

            addParameter("type", prepareTypeValue(checkNotNull(types, "types")));
            addParameter("label", label);
            addParameter("from", from);
            addParameter("till", till);
            addParameter("start_record", startRecord);
            addParameter("records", records);
            addParameter("details", details);
            addParameter("include_currency_exchanges", includeCurrencyExchanges);
        }

        @Override
        protected String requestUrlBase(HostsProvider hostsProvider) {
            return hostsProvider.getMoneyApi() + "/operation-history";
        }

        private static String prepareTypeValue(Set<FilterType> types) {
            StringBuilder builder = new StringBuilder();
            Iterator<FilterType> iterator = types.iterator();
            if (iterator.hasNext()) {
                builder.append(iterator.next().code);
                while (iterator.hasNext()) {
                    builder.append(' ')
                            .append(iterator.next().code);
                }
            }
            return builder.toString();
        }

        /**
         * Builder for a {@link Request}.
         */
        public static class Builder {

            private Set<FilterType> types;
            private String label;
            private DateTime from;
            private DateTime till;
            private String startRecord;
            private Integer records;
            private Boolean details;
            private Boolean includeCurrencyExchanges;

            /**
             * Specifies types of operations that respond should contain. Can be omitted if no
             * specific types are required: respond will contain every operation.
             *
             * @param types set of operation types
             */
            public Builder setTypes(Set<FilterType> types) {
                this.types = types;
                return this;
            }

            /**
             * Look up for a specific operations using theirs labels.
             *
             * @param label the label
             */
            public Builder setLabel(String label) {
                this.label = label;
                return this;
            }

            /**
             * Look for operations starting {@code from} specified time.
             *
             * @param from time
             */
            public Builder setFrom(DateTime from) {
                this.from = from;
                return this;
            }

            /**
             * Look for operations {@code till} specified time.
             *
             * @param till time
             */
            public Builder setTill(DateTime till) {
                this.till = till;
                return this;
            }

            /**
             * Marker to subsequent page of operation's list
             *
             * @param startRecord marker
             */
            public Builder setStartRecord(String startRecord) {
                this.startRecord = startRecord;
                return this;
            }

            /**
             * Limit number of records in response.
             *
             * @param records limit
             */
            public Builder setRecords(Integer records) {
                this.records = records;
                return this;
            }

            /**
             * Request operation details. If set to {@code true} list of operations will contain
             * extended operation details.
             *
             * @param details request detailed operations
             */
            public Builder setDetails(Boolean details) {
                this.details = details;
                return this;
            }

            /**
             * Request currency exchanges. If set to {@code true} list of operations will contain
             * currency exchanges. False by default.
             *
             * @param includeCurrencyExchanges request currency exchanges
             */
            public Builder setIncludeCurrencyExchanges(Boolean includeCurrencyExchanges) {
                this.includeCurrencyExchanges = includeCurrencyExchanges;
                return this;
            }

            /**
             * Creates the {@link Request}
             *
             * @return the request
             */
            public Request create() {
                return new Request(types == null ? Collections.<FilterType>emptySet() : types,
                        label, from, till, startRecord, records, details, includeCurrencyExchanges);
            }
        }
    }
}
