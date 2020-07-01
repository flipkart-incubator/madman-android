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
package com.flipkart.madman.component.enums

/**
 * List of all ad events sent to the client.
 */
enum class AdEventType {
    STARTED,
    PAUSED,
    RESUMED,
    LOADED,
    CONTENT_PAUSE_REQUESTED,
    CONTENT_RESUME_REQUESTED,
    PROGRESS,
    TAPPED,
    FIRST_QUARTILE,
    MIDPOINT,
    THIRD_QUARTILE,
    COMPLETED,
    SKIPPED,
    AD_BREAK_STARTED,
    AD_BREAK_ENDED,
    AD_BREAK_READY,
    CLICKED,
    ALL_AD_COMPLETED
}
