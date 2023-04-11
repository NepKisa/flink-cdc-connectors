/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.connector.oracle.logminer.processor.memory;

import io.debezium.connector.oracle.Scn;
import io.debezium.connector.oracle.logminer.RocksDbCache;
import io.debezium.connector.oracle.logminer.events.LogMinerEvent;
import io.debezium.connector.oracle.logminer.processor.AbstractTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A concrete implementation of a {@link AbstractTransaction} for the JVM heap memory processor.
 *
 * @author Chris Cranford
 */
public class MemoryTransaction extends AbstractTransaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryTransaction.class);

    private int numberOfEvents;
    private List<LogMinerEvent> events;

    public MemoryTransaction(String transactionId, Scn startScn, Instant changeTime, String userName) {
        super(transactionId, startScn, changeTime, userName);
        this.events = new ArrayList<LogMinerEvent>(){
            @Override
            public int size() {
                return RocksDbCache.getCounter().get(transactionId) == null ? 0 : RocksDbCache.getCounter().get(transactionId);
            }

            @Override
            public boolean isEmpty() {
                return !(size() > 0);
            }

            @Override
            public LogMinerEvent get(int index) {
                return (LogMinerEvent) (RocksDbCache.get(transactionId + index));
            }

            @Override
            public boolean add(LogMinerEvent logMinerEvent) {
                try {
                    RocksDbCache.put(transactionId + size(), logMinerEvent);
                    RocksDbCache.getCounter().put(transactionId, size() + 1);
                    return true;
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return false;
            }

            @Override
            public LogMinerEvent remove(int index) {
                LogMinerEvent logMinerEvent = get(index);
                RocksDbCache.delete(transactionId + index);
                return logMinerEvent;
            }

            @Override
            public boolean removeIf(Predicate<? super LogMinerEvent> filter) {
                Objects.requireNonNull(filter);
                boolean removed = false;
                while (size() > 0) {
                    LogMinerEvent each = get(0);
                    if (filter.test(each)) {
                        remove(0);
                        removed = true;
                    }
                }
                return removed;
            }

        };
        start();
    }

    @Override
    public int getNumberOfEvents() {
        return numberOfEvents;
    }

    @Override
    public int getNextEventId() {
        return numberOfEvents++;
    }

    @Override
    public void start() {
        numberOfEvents = 0;
    }

    public List<LogMinerEvent> getEvents() {
        return events;
    }

    public boolean removeEventWithRowId(String rowId) {
        return events.removeIf(event -> {
            if (event.getRowId().equals(rowId)) {
                LOGGER.trace("Undo applied for event {}.", event);
                return true;
            }
            return false;
        });
    }

    @Override
    public String toString() {
        return "MemoryTransaction{" +
                "numberOfEvents=" + numberOfEvents +
                "} " + super.toString();
    }
}
