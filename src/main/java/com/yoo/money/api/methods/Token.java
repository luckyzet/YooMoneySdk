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
import com.yoo.money.api.net.FirstApiRequest;
import com.yoo.money.api.net.providers.HostsProvider;

import static com.yoo.money.api.util.Common.checkNotEmpty;

/**
 * Access token.
 *
 * @author Slava Yasevich (support@yoomoney.ru)
 */
public class Token {

    @SerializedName("access_token")
    public final String accessToken;
    @SerializedName("error")
    public final Error error;

    /**
     * Constructor.
     *
     * @param accessToken access token itself
     * @param error       error code
     */
    public Token(String accessToken, Error error) {
        this.accessToken = accessToken;
        this.error = error;
    }

    @Override
    public String toString() {
        return "Token{" +
                "accessToken='" + accessToken + '\'' +
                ", error=" + error +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Token token = (Token) o;

        return !(accessToken != null ? !accessToken.equals(token.accessToken) :
                token.accessToken != null) && error == token.error;
    }

    @Override
    public int hashCode() {
        int result = accessToken != null ? accessToken.hashCode() : 0;
        result = 31 * result + (error != null ? error.hashCode() : 0);
        return result;
    }

    /**
     * Request for access token.
     */
    public static class Request extends FirstApiRequest<Token> {

        /**
         * Constructor.
         *
         * @param code        temporary code
         * @param clientId    application's client id
         * @param redirectUri redirect uri
         */
        public Request(String code, String clientId, String redirectUri) {
            this(code, clientId, redirectUri, null);
        }

        /**
         * Constructor.
         *
         * @param code         temporary code
         * @param clientId     application's client id
         * @param redirectUri  redirect uri
         * @param clientSecret a secret word for verifying application's authenticity.
         */
        public Request(String code, String clientId, String redirectUri, String clientSecret) {
            super(Token.class);
            addParameter("code", checkNotEmpty(code, "code"));
            addParameter("client_id", checkNotEmpty(clientId, "clientId"));
            addParameter("grant_type", "authorization_code");
            addParameter("redirect_uri", redirectUri);
            addParameter("client_secret", clientSecret);
        }

        @Override
        protected String requestUrlBase(HostsProvider hostsProvider) {
            return hostsProvider.getMoney() + "/oauth/token";
        }
    }

    /**
     * Revokes access token.
     */
    public static final class Revoke extends FirstApiRequest<Revoke> {

        /**
         * Revoke only one token.
         */
        public Revoke() {
            this(false);
        }

        /**
         * Revoke token.
         *
         * @param revokeAll if {@code true} all bound tokens will be also revoked
         */
        public Revoke(boolean revokeAll) {
            super(Revoke.class);
            addParameter("revoke-all", revokeAll);
        }

        @Override
        protected String requestUrlBase(HostsProvider hostsProvider) {
            return hostsProvider.getMoneyApi() + "/revoke";
        }
    }
}
