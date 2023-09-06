package com.fsre.streamerapplication.streamer.util;

import com.fsre.streamerapplication.streamer.exception.SubscriberException;
import com.mongodb.MongoTimeoutException;
import lombok.SneakyThrows;
import org.bson.Document;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class SubscriberHelpers {

    /**
     * A Subscriber that stores the publishers results and provides a latch so can block on completion.
     *
     * @param <T> The publishers result type
     */
    public static class ObservableSubscriber<T> implements Subscriber<T> {
        private final List<T> received;
        private final List<Throwable> errors;
        private final CountDownLatch latch;
        private AtomicReference<Subscription> subscription;
        private volatile boolean completed;

        ObservableSubscriber() {
            this.received = new ArrayList<>();
            this.errors = new ArrayList<>();
            this.latch = new CountDownLatch(1);
        }

        @Override
        public void onSubscribe(final Subscription s) {
            subscription = new AtomicReference<>(s);
        }

        @Override
        public void onNext(final T t) {
            received.add(t);
        }

        @Override
        public void onError(final Throwable t) {
            errors.add(t);
            onComplete();
        }

        @Override
        public void onComplete() {
            completed = true;
            latch.countDown();
        }

        public Subscription getSubscription() {
            return subscription.get();
        }

        public List<T> getReceived() {
            return received;
        }

        public Throwable getError() {
            if (!errors.isEmpty()) {
                return errors.get(0);
            }
            return null;
        }

        public boolean isCompleted() {
            return completed;
        }

        public List<T> get(final long timeout, final TimeUnit unit){
            return await(timeout, unit).getReceived();
        }

        public ObservableSubscriber<T> await() {
            return await(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        }

        @SneakyThrows
        public ObservableSubscriber<T> await(final long timeout, final TimeUnit unit) {
            subscription.get().request(Integer.MAX_VALUE);
            if (!latch.await(timeout, unit)) {
                throw new MongoTimeoutException("Publisher onComplete timed out");
            }
            if (!errors.isEmpty()) {
                throw new SubscriberException(errors.get(0).getMessage());
            }
            return this;
        }
    }

    /**
     * A Subscriber that immediately requests Integer.MAX_VALUE onSubscribe
     *
     * @param <T> The publishers result type
     */
    public static class OperationSubscriber<T> extends ObservableSubscriber<T> {

        protected final Logger logger = LoggerFactory.getLogger(OperationSubscriber.class);

        @Override
        public void onSubscribe(final Subscription s) {
            super.onSubscribe(s);
            s.request(Integer.MAX_VALUE);
        }
    }

       /**
     * A Subscriber that prints the json version of each document
     */
    public static class PrintDocumentSubscriber extends OperationSubscriber<Document> {

        @Override
        public void onNext(final Document document) {

            logger.info("PRINT SUBSCRIBER: {}", document);
            super.onNext(document);
        }
    }

    /**
     * A Subscriber that doesnt do anything
     *
     * @param <T> The publishers result type
     */
    public static class NullSubscriber<T> extends OperationSubscriber<T> {


        public void onNext(final Document document) {
            //Doesnt do anything because it is nullSubscriber
        }
    }

    /**
     * A base Subscriber that has some default settings
     *
     */
    public abstract static class BaseSubscriber<T> implements Subscriber<T> {

        private AtomicReference<Subscription> subscription;
        private  volatile boolean completed;
        protected final Logger logger = LoggerFactory.getLogger(BaseSubscriber.class);

        @Override
        public void onSubscribe(final Subscription s) {
            subscription = new AtomicReference<>(s);
            subscription.get().request(Integer.MAX_VALUE);
            logger.info("Subscribing");
        }

        @Override
        public void onComplete() {
            completed = true;
            logger.info("Completed");
        }

        @Override
        public void onError(final Throwable t) {
            logger.error("Error from change stream service: ", t);
            onComplete();
        }

        public boolean isCompleted() {
            return completed;
        }
        public Subscription getSubscription() {
            return subscription.get();
        }

    }

    private SubscriberHelpers() {
    }
}
