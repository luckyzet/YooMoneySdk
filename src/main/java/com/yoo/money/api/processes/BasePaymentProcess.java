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

package com.yoo.money.api.processes;

import com.yoo.money.api.methods.payment.BaseProcessPayment;
import com.yoo.money.api.methods.payment.BaseRequestPayment;
import com.yoo.money.api.net.ApiRequest;
import com.yoo.money.api.net.clients.ApiClient;
import com.yoo.money.api.util.Threads;

import static com.yoo.money.api.util.Common.checkNotNull;

/**
 * Base implementation for all payment processes.
 *
 * @author Slava Yasevich (support@yoomoney.ru)
 */
public abstract class BasePaymentProcess<RP extends BaseRequestPayment,
        PP extends BaseProcessPayment> implements IPaymentProcess {

    /**
     * Provides parameters for requests.
     */
    final ParameterProvider parameterProvider;

    private final ApiClient client;

    private RP requestPayment;
    private PP processPayment;
    private State state;

    /**
     * Constructor.
     *
     * @param client client to use for the process
     * @param parameterProvider parameter's provider
     */
    @SuppressWarnings("WeakerAccess")
    public BasePaymentProcess(ApiClient client, ParameterProvider parameterProvider) {
        this.client = checkNotNull(client, "client");
        this.parameterProvider = checkNotNull(parameterProvider, "parameterProvider");
        this.state = State.CREATED;
    }

    @Override
    public final boolean proceed() throws Exception {
        switch (state) {
            case CREATED:
                executeRequestPayment();
                break;
            case STARTED:
                executeProcessPayment();
                break;
            case PROCESSING:
                executeRepeatProcessPayment();
                break;
        }

        return isCompleted();
    }

    @Override
    public final boolean repeat() throws Exception {
        switch (state) {
            case STARTED:
                executeRequestPayment();
                break;
            case PROCESSING:
                executeProcessPayment();
                break;
            case COMPLETED:
                executeRepeatProcessPayment();
                break;
        }

        return isCompleted();
    }

    @Override
    public final void reset() {
        this.requestPayment = null;
        this.processPayment = null;
        this.state = State.CREATED;
    }

    /**
     * @return saved state for payment process
     */
    public SavedState<RP, PP> getSavedState() {
        return createSavedState(requestPayment, processPayment, state);
    }

    /**
     * Restores payment process from a saved state.
     *
     * @param savedState saved state
     */
    public final void restoreSavedState(SavedState<RP, PP> savedState) {
        checkNotNull(savedState, "saved state");
        this.requestPayment = savedState.getRequestPayment();
        this.processPayment = savedState.getProcessPayment();
        this.state = savedState.getState();
    }

    @Override
    public final BaseRequestPayment getRequestPayment() {
        return requestPayment;
    }

    @Override
    public final BaseProcessPayment getProcessPayment() {
        return processPayment;
    }

    /**
     * Sets access token to a session if required.
     *
     * @param accessToken access token
     */
    @SuppressWarnings("WeakerAccess")
    public final void setAccessToken(String accessToken) {
        client.setAccessToken(accessToken);
    }

    /**
     * Creates request payment method.
     *
     * @return method request
     */
    protected abstract ApiRequest<RP> createRequestPayment();

    /**
     * Creates process payment method.
     *
     * @return method request
     */
    protected abstract ApiRequest<PP> createProcessPayment();

    /**
     * Creates repeat process payment method.
     *
     * @return method request
     */
    protected abstract ApiRequest<PP> createRepeatProcessPayment();

    protected abstract SavedState<RP, PP> createSavedState(RP requestPayment, PP processPayment, State state);

    private void executeRequestPayment() throws Exception {
        requestPayment = execute(createRequestPayment());
        state = State.STARTED;
    }

    private void executeProcessPayment() throws Exception {
        executeProcessPayment(createProcessPayment());
    }

    private void executeRepeatProcessPayment() throws Exception {
        executeProcessPayment(createRepeatProcessPayment());
    }

    private void executeProcessPayment(final ApiRequest<PP> request) throws Exception {
        BaseProcessPayment.Status previousStatus = processPayment == null ? null :
                processPayment.status;
        processPayment = execute(request);

        switch (processPayment.status) {
            case EXT_AUTH_REQUIRED:
                if (previousStatus != BaseProcessPayment.Status.EXT_AUTH_REQUIRED) {
                    state = State.PROCESSING;
                    return;
                }
            case IN_PROGRESS:
                state = State.PROCESSING;
                Threads.sleep(processPayment.nextRetry);
                executeProcessPayment(request);
                return;
        }

        state = State.COMPLETED;
    }

    private <T> T execute(ApiRequest<T> apiRequest) throws Exception {
        return client.execute(apiRequest);
    }

    private boolean isCompleted() {
        return state == State.COMPLETED;
    }

    /**
     * State of payment process
     */
    enum State {
        /**
         * Indicates that payment process is created.
         */
        CREATED,
        /**
         * Indicates that payment process is started.
         */
        STARTED,
        /**
         * Indicates that payment process is in progress (not completed).
         */
        PROCESSING,
        /**
         * Indicates that payment process is completed.
         */
        COMPLETED
    }

    /**
     * Saved state of payment process.
     */
    public static class SavedState <RP extends BaseRequestPayment, PP extends BaseProcessPayment> {

        private final RP requestPayment;
        private final PP processPayment;
        private final State state;

        /**
         * Constructor.
         *
         * @param requestPayment request payment
         * @param processPayment process payment
         * @param flags flags of saved state
         */
        @SuppressWarnings("WeakerAccess")
        public SavedState(RP requestPayment, PP processPayment, int flags) {
            this(requestPayment, processPayment, parseState(flags));
        }

        /**
         * Constructor.
         *
         * @param requestPayment request payment
         * @param processPayment process payment
         * @param state state
         */
        @SuppressWarnings("WeakerAccess")
        protected SavedState(RP requestPayment, PP processPayment, State state) {
            this.state = checkNotNull(state, "state");

            switch (state) {
                case CREATED:
                    this.requestPayment = null;
                    this.processPayment = null;
                    break;
                case STARTED:
                    this.requestPayment = checkNotNull(requestPayment, "requestPayment");
                    this.processPayment = null;
                    break;
                case PROCESSING:
                case COMPLETED:
                    this.requestPayment = checkNotNull(requestPayment, "requestPayment");
                    this.processPayment = checkNotNull(processPayment, "processPayment");
                    break;
                default:
                    throw new IllegalArgumentException("unknown state: " + state);
            }
        }

        /**
         * @return request payment
         */
        public final RP getRequestPayment() {
            return requestPayment;
        }

        /**
         * @return process payment
         */
        public final PP getProcessPayment() {
            return processPayment;
        }

        /**
         * @return flags of payment process
         */
        public int getFlags() {
            return state.ordinal();
        }

        private static State parseState(int flags) {
            State[] values = State.values();
            int index = flags % 10;
            if (index >= values.length) {
                throw new IllegalArgumentException("invalid flags: " + flags);
            }
            return values[index];
        }

        /**
         * @return state
         */
        State getState() {
            return state;
        }
    }

    /**
     * @return state of payment process
     */
    final State getState() {
        return state;
    }
}
