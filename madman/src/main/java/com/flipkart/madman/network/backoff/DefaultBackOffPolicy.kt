/*
 * Copyright (C) 2020 Flipkart Internet Pvt Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.madman.network.backoff

/**
 * Default implementation of [BackOffPolicy]
 */
class DefaultBackOffPolicy : BackOffPolicy {
    private var currentRetryCount: Int = 0
    private var currentTimeoutMs: Long = 2500

    /**
     * Backoff time to retry the batch for 5XX Server errors.
     *
     * @return new timeOut
     */
    override fun backOff(): Long {
        currentRetryCount++
        currentTimeoutMs =
            (currentTimeoutMs + currentTimeoutMs * getBackOffMultiplier()).toLong()
        return currentTimeoutMs
    }

    override fun getMaxRetries(): Int {
        return 3
    }

    override fun getBackOffMultiplier(): Float {
        return 1F
    }

    override fun canRetry(): Boolean {
        return currentRetryCount < getMaxRetries()
    }
}
