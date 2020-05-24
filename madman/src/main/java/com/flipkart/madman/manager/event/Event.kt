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
package com.flipkart.madman.manager.event

/**
 * Internal ad event types
 */
enum class Event {
    CONTENT_RESUME,
    CONTENT_PAUSE,
    LOAD_AD,
    PLAY_AD,
    RESUME_AD,
    PAUSE_AD,
    FIRST_QUARTILE,
    MIDPOINT,
    THIRD_QUARTILE,
    AD_LOADED,
    AD_STARTED,
    AD_PROGRESS,
    AD_COMPLETED,
    AD_STOPPED,
    AD_SKIPPED,
    AD_TAPPED,
    AD_CTA_CLICKED,
    AD_PAUSED,
    AD_RESUMED,
    AD_BREAK_LOADED,
    AD_BREAK_STARTED,
    AD_BREAK_ENDED,
    ALL_AD_COMPLETED,
}
