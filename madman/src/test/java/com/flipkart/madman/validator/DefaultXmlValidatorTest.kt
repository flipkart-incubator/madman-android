package com.flipkart.madman.validator

import com.flipkart.madman.component.model.vmap.AdBreak
import com.flipkart.madman.testutils.VMAPUtil
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test for [DefaultXmlValidator]
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class DefaultXmlValidatorTest {

    /**
     * validate vmap
     */
    @Test
    fun validateVMAP() {
        val validator = DefaultXmlValidator()
        var vmapData = VMAPUtil.createVMAP("1.0", listOf(VMAPUtil.createAdBreak("LINEAR", "start")))
        var result = validator.validateVMAP(vmapData)

        // assert the vmap is valid
        assert(result.isValid())
        assert(result.getMessage() == null)

        vmapData = VMAPUtil.createVMAP("1.0", null)
        result = validator.validateVMAP(vmapData)

        // assert the vmap is in-valid
        assert(!result.isValid())
        assert(result.getMessage() == DefaultXmlValidator.NO_AD_BREAK_ERROR_MSG)

        vmapData = VMAPUtil.createVMAP(null, null)
        result = validator.validateVMAP(vmapData)

        // assert the vmap is in-valid
        assert(!result.isValid())
        assert(result.getMessage() == DefaultXmlValidator.INVALID_VMAP_ERROR_MSG)

        vmapData = VMAPUtil.createVMAP(null, listOf(VMAPUtil.createAdBreak("LINEAR", "start")))
        result = validator.validateVMAP(vmapData)

        // assert the vmap is in-valid
        assert(!result.isValid())
        assert(result.getMessage() == DefaultXmlValidator.VMAP_VERSION_ERROR_MSG)
    }

    /**
     * Validate [AdBreak] for all combinations
     */
    @Test
    fun validateAdBreak() {
        val validator = DefaultXmlValidator()
        var result = validator.validAdBreak(VMAPUtil.createAdBreak(null, null))

        // assert the ad break is in valid
        assert(!result.isValid())
        assert(result.getMessage() != null)

        result = validator.validAdBreak(VMAPUtil.createAdBreak(null, "start"))

        // assert the ad break is in valid
        assert(!result.isValid())
        assert(result.getMessage() != null)

        result = validator.validAdBreak(VMAPUtil.createAdBreak(AdBreak.BreakTypes.DISPLAY, "start"))

        // assert the ad break is valid
        assert(result.isValid())
        assert(result.getMessage() == null)

        result = validator.validAdBreak(VMAPUtil.createAdBreak("dummy", "start"))

        // assert the ad break is in valid
        assert(!result.isValid())
        assert(result.getMessage() != null)

        result = validator.validAdBreak(VMAPUtil.createAdBreak(AdBreak.BreakTypes.DISPLAY, null))

        // assert the ad break is in valid
        assert(!result.isValid())
        assert(result.getMessage() != null)
    }
}
