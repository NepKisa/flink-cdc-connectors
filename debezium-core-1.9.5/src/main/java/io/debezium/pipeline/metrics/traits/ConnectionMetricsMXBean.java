/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.pipeline.metrics.traits;

/**
 * Exposes connection metrics.
 */
public interface ConnectionMetricsMXBean {

    boolean isConnected();
}
