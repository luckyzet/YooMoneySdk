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

package com.yoo.money.api.methods;

import com.google.gson.annotations.SerializedName;
import com.yoo.money.api.model.Error;
import com.yoo.money.api.model.showcase.ShowcaseReference;
import com.yoo.money.api.net.DocumentApiRequest;
import com.yoo.money.api.net.providers.HostsProvider;

import java.util.Collections;
import java.util.List;

import static com.yoo.money.api.util.Common.checkNotEmpty;
import static com.yoo.money.api.util.Common.checkNotNull;

/**
 * This class wraps result of showcase searching provided by response of {@link Request} call.
 */
public class ShowcaseSearch {

    /**
     * Error code. May be {@code null}.
     */
    @SerializedName("error")
    public final Error error;

    /**
     * List of {@link ShowcaseReference}.
     */
    @SerializedName("result")
    public final List<ShowcaseReference> result;

    /**
     * Next page marker.
     */
    @SerializedName("nextPage")
    public final String nextPage;

    private ShowcaseSearch(Error error, List<ShowcaseReference> result, String nextPage) {
        this.error = error;
        this.result = result != null ? Collections.unmodifiableList(checkNotNull(result, "result")) : null;
        this.nextPage = nextPage;
    }

    /**
     * Constructs successful search call.
     *
     * @param result   obtained items
     * @param nextPage next page marker
     */
    public static ShowcaseSearch success(List<ShowcaseReference> result, String nextPage) {
        return new ShowcaseSearch(null, result, nextPage);
    }

    /**
     * Constructs failed instance.
     *
     * @param error reason
     */
    public static ShowcaseSearch failure(Error error) {
        return new ShowcaseSearch(checkNotNull(error, "error"), Collections.<ShowcaseReference>emptyList(), null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShowcaseSearch that = (ShowcaseSearch) o;

        if (error != that.error) return false;
        if (result != null ? !result.equals(that.result) : that.result != null) return false;
        return nextPage != null ? nextPage.equals(that.nextPage) : that.nextPage == null;
    }

    @Override
    public int hashCode() {
        int result1 = error != null ? error.hashCode() : 0;
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        result1 = 31 * result1 + (nextPage != null ? nextPage.hashCode() : 0);
        return result1;
    }

    @Override
    public String toString() {
        return "ShowcaseSearch{" +
                "error=" + error +
                ", result=" + result +
                ", nextPage='" + nextPage + '\'' +
                '}';
    }

    public static class Request extends DocumentApiRequest<ShowcaseSearch> {

        /**
         * Constructor.
         *
         * @param query   search terms
         * @param records number of records to requests from remote server
         */
        public Request(String query, int records) {
            super(ShowcaseSearch.class);
            addParameter("query", checkNotEmpty(query, "query"));
            addParameter("records", records);
        }

        @Override
        protected String requestUrlBase(HostsProvider hostsProvider) {
            return hostsProvider.getMoneyApi() + "/showcase-search";
        }
    }
}
