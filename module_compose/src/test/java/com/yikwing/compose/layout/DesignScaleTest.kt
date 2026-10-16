package com.yikwing.compose.layout

import androidx.compose.ui.unit.Density
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.closeTo
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.sameInstance
import org.junit.Test

class DesignScaleTest {
    @Test
    fun `scale equals width ratio below the cap`() {
        assertThat(calculateDesignScale(375f).toDouble(), closeTo(1.0, 1e-6))
        assertThat(calculateDesignScale(320f).toDouble(), closeTo(320.0 / 375, 1e-6))
    }

    @Test
    fun `scale is capped at max`() {
        assertThat(calculateDesignScale(600f).toDouble(), closeTo(1.4, 1e-6))
        assertThat(calculateDesignScale(1200f).toDouble(), closeTo(1.4, 1e-6))
    }

    @Test
    fun `scale never decreases as width grows`() {
        var previous = 0f
        for (width in 200..1400) {
            val scale = calculateDesignScale(width.toFloat())
            assertThat("width=$width", scale, greaterThanOrEqualTo(previous))
            previous = scale
        }
    }

    @Test
    fun `font scale within limit keeps original density`() {
        val density = Density(density = 3f, fontScale = 1.15f)
        assertThat(capFontScale(density), sameInstance(density))
    }

    @Test
    fun `font scale above limit is capped`() {
        val capped = capFontScale(Density(density = 3f, fontScale = 2f))
        assertThat(capped.fontScale.toDouble(), closeTo(1.3, 1e-6))
        assertThat(capped.density.toDouble(), closeTo(3.0, 1e-6))
    }
}
