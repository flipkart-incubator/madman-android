package com.flipkart.madman.renderer

import android.view.ViewGroup
import com.flipkart.madman.renderer.callback.ViewClickListener
import com.flipkart.madman.renderer.player.AdPlayer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class DefaultAdRendererTest {
    @Mock
    private lateinit var mockViewClickListener: ViewClickListener

    @Mock
    private lateinit var mockPlayer: AdPlayer

    @Mock
    private lateinit var mockContainer: ViewGroup

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    /**
     * Test to verify registered view click listeners
     */
    @Test
    fun testRegisteredViewClickListeners() {
        val defaultAdRenderer =
            DefaultAdRenderer.Builder()
                .setPlayer(mockPlayer)
                .setContainer(mockContainer)
                .build(null)

        defaultAdRenderer.registerViewClickListener(mockViewClickListener)
        assert(defaultAdRenderer.viewClickListeners.size == 1)

        defaultAdRenderer.unregisterViewClickListener(mockViewClickListener)
        assert(defaultAdRenderer.viewClickListeners.isEmpty())

        defaultAdRenderer.registerViewClickListener(mockViewClickListener)
        defaultAdRenderer.registerViewClickListener(mockViewClickListener)
        assert(defaultAdRenderer.viewClickListeners.size == 2)

        defaultAdRenderer.destroy()
        assert(defaultAdRenderer.viewClickListeners.isEmpty())
    }
}
