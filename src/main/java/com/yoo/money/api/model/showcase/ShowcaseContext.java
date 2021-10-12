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

package com.yoo.money.api.model.showcase;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.yoo.money.api.exceptions.ResourceNotFoundException;
import com.yoo.money.api.methods.payment.RequestExternalPayment;
import com.yoo.money.api.methods.payment.RequestPayment;
import com.yoo.money.api.net.ApiRequest;
import com.yoo.money.api.net.BaseApiRequest;
import com.yoo.money.api.net.HttpClientResponse;
import com.yoo.money.api.net.providers.HostsProvider;
import com.yoo.money.api.time.DateTime;
import com.yoo.money.api.typeadapters.BaseTypeAdapter;
import com.yoo.money.api.typeadapters.JsonUtils;
import com.yoo.money.api.typeadapters.model.showcase.ShowcaseTypeAdapter;
import com.yoo.money.api.util.HttpHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Map;
import java.util.Stack;

import static com.yoo.money.api.util.Common.checkNotEmpty;
import static com.yoo.money.api.util.Common.checkNotNull;
import static com.yoo.money.api.util.Responses.processError;

/**
 * This class handles {@link Showcase} submit steps.
 *
 * @author Slava Yasevich (support@yoomoney.ru)
 */
public final class ShowcaseContext {

    /**
     * Processed steps so far.
     */
    private final Stack<Step> history;

    /**
     * {@link DateTime} of last showcase changes on remote server. Useful for caching.
     */
    private final DateTime lastModified;

    /**
     * Current step.
     */
    private Step currentStep;

    /**
     * Complete bundle of payment parameters. It's empty until the last step is reached.
     */
    private Map<String, String> params = Collections.emptyMap();

    /**
     * Current state (response code).
     */
    private State state = State.UNKNOWN;

    ShowcaseContext(State state) {
        this(null, null, DateTime.now());
        this.state = state;
    }

    ShowcaseContext(Showcase showcase, String submitUrl, DateTime lastModified) {
        this.history = new Stack<>();
        this.currentStep = new Step(showcase, submitUrl);
        this.lastModified = lastModified;
    }

    /**
     * Constructor.
     *
     * @param history      previous steps
     * @param lastModified {@link DateTime} of last showcase changes on remote server
     * @param currentStep  current step
     * @param params       payment parameters
     * @param state        status code of current (last) operation
     */
    public ShowcaseContext(Stack<Step> history, DateTime lastModified, Step currentStep,
                           Map<String, String> params, State state) {

        this.history = checkNotNull(history, "history");
        this.lastModified = checkNotNull(lastModified, "lastModified");
        this.params = checkNotNull(params, "params");
        this.currentStep = currentStep;
        this.state = state;
    }

    /**
     * @return request to move on the next state.
     */
    public ApiRequest<ShowcaseContext> createRequest() {
        return new Request(this, lastModified);
    }

    /**
     * Pops previous step from history setting it as a current step.
     * <p/>
     * It will remove params if context has state {@code COMPLETED} and the state will be reset to
     * {@code HAS_NEXT_STEP}.
     * <p/>
     * If history is empty nothing will be done.
     *
     * @return previous step
     */
    @SuppressWarnings("UnusedReturnValue")
    public Step popStep() {
        if (!params.isEmpty()) {
            params = Collections.emptyMap();
            state = State.HAS_NEXT_STEP;
        } else if (!history.isEmpty()) {
            currentStep = history.pop();
        }
        return currentStep;
    }

    /**
     * @return size of processed steps
     */
    public int getHistorySize() {
        return history.size();
    }

    /**
     * @return reached steps
     */
    public Stack<Step> getHistory() {
        return history;
    }

    /**
     * @return current step
     */
    public Step getCurrentStep() {
        return currentStep;
    }

    void setCurrentStep(Step currentStep) {
        this.currentStep = currentStep;
    }

    /**
     * @return {@link DateTime} of last showcase changes on remote server
     */
    public DateTime getLastModified() {
        return lastModified;
    }

    /**
     * Collection of payment parameters which should be used in
     * {@link RequestPayment} or
     * {@link RequestExternalPayment}.
     *
     * @return payment parameters in case of last step or empty map otherwise
     */
    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "ShowcaseContext{" +
                "history=" + history +
                ", lastModified=" + lastModified +
                ", currentStep=" + currentStep +
                ", params=" + params +
                ", state=" + state +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShowcaseContext that = (ShowcaseContext) o;

        return history.equals(that.history) && lastModified.equals(that.lastModified)
                && currentStep.equals(that.currentStep)
                && params.equals(that.params)
                && state == that.state;
    }

    @Override
    public int hashCode() {
        int result = history.hashCode();
        result = 31 * result + lastModified.hashCode();
        result = 31 * result + currentStep.hashCode();
        result = 31 * result + params.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }

    void setParams(InputStream inputStream) {
        params = ParamsTypeAdapter.getInstance()
                .fromJson(inputStream)
                .params;
    }

    /**
     * @return status code of current (last) operation
     */
    public State getState() {
        return state;
    }

    void setState(State state) {
        this.state = state;
    }

    /**
     * Pushes current step to {@code history} using new step as current step.
     *
     * @param newStep new step
     */
    void pushCurrentStep(Step newStep) {
        checkNotNull(newStep, "new step");
        history.push(currentStep);
        currentStep = newStep;
    }

    /**
     * Possible states of {@link ShowcaseContext} instance
     */
    public enum State {
        HAS_NEXT_STEP,
        INVALID_PARAMS,
        COMPLETED,
        NOT_MODIFIED,
        UNKNOWN
    }

    public static final class Step {

        public final Showcase showcase;
        @SuppressWarnings("WeakerAccess")
        public final String submitUrl;

        public Step(Showcase showcase, String submitUrl) {
            this.showcase = showcase;
            this.submitUrl = submitUrl;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Step step = (Step) o;

            return !(showcase != null ? !showcase.equals(step.showcase) : step.showcase != null)
                    && !(submitUrl != null ? !submitUrl.equals(step.submitUrl)
                    : step.submitUrl != null);

        }

        @Override
        public String toString() {
            return "Step{" +
                    "showcase=" + showcase +
                    ", submitUrl='" + submitUrl + '\'' +
                    '}';
        }

        @Override
        public int hashCode() {
            int result = showcase != null ? showcase.hashCode() : 0;
            result = 31 * result + (submitUrl != null ? submitUrl.hashCode() : 0);
            return result;
        }
    }

    private static final class Request extends BaseApiRequest<ShowcaseContext> {

        private final ShowcaseContext context;

        public Request(ShowcaseContext context, DateTime lastModified) {
            this.context = checkNotNull(context, "context");
            checkNotEmpty(context.getCurrentStep().submitUrl, "currentStep.submitUrl");

            addHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModified);
            addParameters(checkNotNull(context.getCurrentStep().showcase, "currentStep.showcase").getPaymentParameters());
        }

        @Override
        public Method getMethod() {
            return Method.POST;
        }

        @Override
        protected String requestUrlBase(HostsProvider hostsProvider) {
            return context.getCurrentStep().submitUrl;
        }

        @Override
        public ShowcaseContext parse(HttpClientResponse response) throws Exception {
            InputStream inputStream = null;

            try {
                int responseCode = response.getCode();
                switch (responseCode) {
                    case HttpURLConnection.HTTP_OK:
                        inputStream = response.getByteStream();
                        context.setParams(inputStream);
                        context.setState(State.COMPLETED);
                        return context;
                    case HttpURLConnection.HTTP_MULT_CHOICE:
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        final String newLocation = response.getHeader(HttpHeaders.LOCATION);

                        inputStream = response.getByteStream();
                        Showcase newShowcase = ShowcaseTypeAdapter.getInstance().fromJson(inputStream);

                        Step step = new Step(newShowcase, newLocation);
                        if (responseCode == HttpURLConnection.HTTP_MULT_CHOICE) {
                            context.pushCurrentStep(step);
                            context.setState(State.HAS_NEXT_STEP);
                        } else {
                            context.setCurrentStep(step);
                            context.setState(State.INVALID_PARAMS);
                        }
                        return context;
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        throw new ResourceNotFoundException(response.getUrl());
                    default:
                        throw new IOException(processError(response));
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
    }

    private static final class Params {
        final Map<String, String> params;

        Params(Map<String, String> params) {
            this.params = Collections.unmodifiableMap(checkNotNull(params, "params"));
        }
    }

    private static final class ParamsTypeAdapter extends BaseTypeAdapter<Params> {

        private static final ParamsTypeAdapter INSTANCE = new ParamsTypeAdapter();

        private ParamsTypeAdapter() {
        }

        public static ParamsTypeAdapter getInstance() {
            return INSTANCE;
        }

        @Override
        public Params deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return new Params(JsonUtils.map(json.getAsJsonObject().getAsJsonObject("params")));
        }

        @Override
        public JsonElement serialize(Params src, Type typeOfSrc, JsonSerializationContext context) {
            throw new UnsupportedOperationException("not implemented");
        }

        @Override
        public Class<Params> getType() {
            return Params.class;
        }
    }
}
