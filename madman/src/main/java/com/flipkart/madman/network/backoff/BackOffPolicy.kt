/*
 *
 *  * Copyright (C) 2019 Flipkart Internet Pvt Ltd
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package com.flipkart.madman.network.backoff

/**
 * Back off policy
 */
interface BackOffPolicy {
    /**
     * Maximum retries allowed
     */
    fun getMaxRetries(): Int

    /**
     * The back off multiplier which is used to back off the timer with the
     * given multiplier
     */
    fun getBackOffMultiplier(): Float

    /**
     * back off, returns the next retry time
     */
    fun backOff(): Long

    /**
     * check if retry is possible
     */
    fun canRetry(): Boolean
}
