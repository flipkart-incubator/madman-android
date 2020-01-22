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

import org.junit.Test

class DefaultBackOffPolicyTest {
    @Test
    fun testPolicy() {
        val backOffPolicy = DefaultBackOffPolicy()

        // default values
        assert(backOffPolicy.getMaxRetries() == 3)
        assert(backOffPolicy.canRetry())
        assert(backOffPolicy.getBackOffMultiplier() == 1F)

        val backOff = backOffPolicy.backOff()

        assert(backOff == 5000L)
        assert(backOffPolicy.canRetry())
    }
}
