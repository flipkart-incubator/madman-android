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
package com.flipkart.madman.component.model.vast

import com.flipkart.madman.component.model.vast.media.BaseAdMedia

class Creative {
    /** creative id **/
    var id: String? = null

    /** creative sequence **/
    var sequence: String? = null

    /** ad media ie could be linear or companion **/
    var adMedia: BaseAdMedia? = null

    companion object {
        const val CREATIVE_XML_TAG = "Creative"
        const val LINEAR_XML_TAG = "Linear"
        const val ID_XML_ATTR = "id"
        const val SEQUENCE_XML_ATTR = "sequence"
    }
}
