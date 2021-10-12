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

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.yoo.money.api.model.AccountStatus;
import com.yoo.money.api.model.AccountType;
import com.yoo.money.api.model.Avatar;
import com.yoo.money.api.model.BalanceDetails;
import com.yoo.money.api.model.Card;
import com.yoo.money.api.model.Currency;
import com.yoo.money.api.model.Identifiable;
import com.yoo.money.api.net.FirstApiRequest;
import com.yoo.money.api.net.providers.HostsProvider;
import com.yoo.money.api.typeadapters.model.BonusBalanceTypeAdapter;
import com.yoo.money.api.typeadapters.model.NumericCurrencyTypeAdapter;

import java.math.BigDecimal;
import java.util.List;

import static com.yoo.money.api.util.Common.checkNotEmpty;
import static com.yoo.money.api.util.Common.checkNotNull;
import static java.util.Collections.unmodifiableList;

/**
 * Information of user account.
 *
 * @author Roman Tsirulnikov (support@yoomoney.ru)
 */
public class AccountInfo implements Identifiable {

    /**
     * account number
     */
    @SerializedName("account")
    public final String account;

    /**
     * current balance
     */
    @SerializedName("balance")
    public final BigDecimal balance;

    /**
     * account's currency
     */
    @SerializedName("currency")
    @JsonAdapter(NumericCurrencyTypeAdapter.class)
    public final Currency currency;

    /**
     * account's status
     */
    @SuppressWarnings("WeakerAccess")
    @SerializedName("account_status")
    public final AccountStatus accountStatus;

    /**
     * account's type
     */
    @SuppressWarnings("WeakerAccess")
    @SerializedName("account_type")
    public final AccountType accountType;

    /**
     * avatar
     */
    @SuppressWarnings("WeakerAccess")
    @SerializedName("avatar")
    public final Avatar avatar;

    /**
     * balance details
     */
    @SuppressWarnings("WeakerAccess")
    @SerializedName("balance_details")
    public final BalanceDetails balanceDetails;

    /**
     * list of linked cards
     */
    @SuppressWarnings("WeakerAccess")
    @SerializedName("cards_linked")
    public final List<Card> linkedCards;

    /**
     * Bonus balance.
     */
    @SuppressWarnings("WeakerAccess")
    @SerializedName("bonus_balance")
    @JsonAdapter(BonusBalanceTypeAdapter.class)
    public final BigDecimal bonusBalance;

    @SuppressWarnings("WeakerAccess")
    protected AccountInfo(Builder builder) {
        account = checkNotEmpty(builder.account, "account");
        balance = checkNotNull(builder.balance, "balance");
        currency = checkNotNull(builder.currency, "currency");
        accountStatus = checkNotNull(builder.accountStatus, "accountStatus");
        accountType = checkNotNull(builder.accountType, "accountType");
        avatar = builder.avatar;
        balanceDetails = checkNotNull(builder.balanceDetails, "balanceDetails");
        linkedCards = builder.linkedCards != null ? unmodifiableList(builder.linkedCards) : null;
        bonusBalance = builder.bonusBalance;
    }

    @Override
    public String getId() {
        return account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountInfo that = (AccountInfo) o;

        if (account != null ? !account.equals(that.account) : that.account != null) return false;
        if (balance != null ? !balance.equals(that.balance) : that.balance != null) return false;
        if (currency != that.currency) return false;
        if (accountStatus != that.accountStatus) return false;
        if (accountType != that.accountType) return false;
        if (avatar != null ? !avatar.equals(that.avatar) : that.avatar != null) return false;
        if (balanceDetails != null ? !balanceDetails.equals(that.balanceDetails) : that.balanceDetails != null)
            return false;
        //noinspection SimplifiableIfStatement
        if (linkedCards != null ? !linkedCards.equals(that.linkedCards) : that.linkedCards != null)
            return false;
        return bonusBalance != null ? bonusBalance.equals(that.bonusBalance) : that.bonusBalance == null;
    }

    @Override
    public int hashCode() {
        int result = account != null ? account.hashCode() : 0;
        result = 31 * result + (balance != null ? balance.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (accountStatus != null ? accountStatus.hashCode() : 0);
        result = 31 * result + (accountType != null ? accountType.hashCode() : 0);
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        result = 31 * result + (balanceDetails != null ? balanceDetails.hashCode() : 0);
        result = 31 * result + (linkedCards != null ? linkedCards.hashCode() : 0);
        result = 31 * result + (bonusBalance != null ? bonusBalance.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AccountInfo{" +
                "account='" + account + '\'' +
                ", balance=" + balance +
                ", currency=" + currency +
                ", accountStatus=" + accountStatus +
                ", accountType=" + accountType +
                ", avatar=" + avatar +
                ", balanceDetails=" + balanceDetails +
                ", linkedCards=" + linkedCards +
                ", bonusBalance=" + bonusBalance +
                '}';
    }

    /**
     * Creates {@link AccountInfo} instance.
     */
    public static class Builder {

        String account;
        BigDecimal balance = BigDecimal.ZERO;
        Currency currency = Currency.RUB;
        AccountStatus accountStatus = AccountStatus.ANONYMOUS;
        AccountType accountType = AccountType.PERSONAL;
        Avatar avatar;
        BalanceDetails balanceDetails = BalanceDetails.ZERO;
        List<Card> linkedCards;
        BigDecimal bonusBalance;

        /**
         * @param account account's number
         * @return itself
         */
        public Builder setAccount(String account) {
            this.account = account;
            return this;
        }

        /**
         * @param balance current balance
         * @return itself
         */
        public Builder setBalance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        /**
         * @param currency account's currency
         * @return itself
         */
        public Builder setCurrency(Currency currency) {
            this.currency = currency;
            return this;
        }

        /**
         * @param accountStatus account's status
         * @return itself
         */
        public Builder setAccountStatus(AccountStatus accountStatus) {
            this.accountStatus = accountStatus;
            return this;
        }

        /**
         * @param accountType account's type
         * @return itself
         */
        public Builder setAccountType(AccountType accountType) {
            this.accountType = accountType;
            return this;
        }

        /**
         * @param avatar avatar
         * @return itself
         */
        public Builder setAvatar(Avatar avatar) {
            this.avatar = avatar;
            return this;
        }

        /**
         * @param balanceDetails balance details
         * @return itself
         */
        public Builder setBalanceDetails(BalanceDetails balanceDetails) {
            this.balanceDetails = balanceDetails;
            return this;
        }

        /**
         * @param linkedCards list of linked cards
         * @return itself
         */
        public Builder setLinkedCards(List<Card> linkedCards) {
            this.linkedCards = linkedCards;
            return this;
        }

        /**
         * @param bonusBalance user's bonus balance
         * @return itself
         */
        public Builder setBonusBalance(BigDecimal bonusBalance) {
            this.bonusBalance = bonusBalance;
            return this;
        }

        /**
         * @return {@link AccountInfo} instance
         */
        public AccountInfo create() {
            return new AccountInfo(this);
        }
    }

    /**
     * Requests for {@link AccountInfo}.
     * <p/>
     * Authorized session required.
     */
    public static final class Request extends FirstApiRequest<AccountInfo> {

        public Request() {
            super(AccountInfo.class);
        }

        @Override
        protected String requestUrlBase(HostsProvider hostsProvider) {
            return hostsProvider.getMoneyApi() + "/account-info";
        }
    }
}
