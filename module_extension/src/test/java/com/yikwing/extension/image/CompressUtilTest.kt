package com.yikwing.extension.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class CompressUtilTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val context = mockk<Context>()
    private val uri = mockk<Uri>()
    private val bitmap = mockk<Bitmap>(relaxed = true)
    private val compressedBytes = byteArrayOf(1, 2, 3)

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        // Keep real file I/O while replacing Android bitmap decoding/encoding.
        mockkStatic(::getCompressedBitmapByResolution)
        every { getCompressedBitmapByResolution(context, uri) } returns bitmap
        every { compressBitmap(bitmap, 100) } returns compressedBytes
    }

    @After
    fun teardown() {
        unmockkStatic(::getCompressedBitmapByResolution)
        unmockkStatic(Log::class)
    }

    @Test
    fun `successful compression should write bytes and recycle the bitmap`() {
        val output = File(temporaryFolder.root, "image.webp")

        assertThat(compressImageFromUri(context, uri, output.path, 100), `is`(true))

        assertArrayEquals(compressedBytes, output.readBytes())
        verify(exactly = 1) { bitmap.recycle() }
    }

    @Test
    fun `save should return false when the destination cannot be opened`() {
        val output = File(temporaryFolder.root, "missing-parent/image.webp")

        assertThat(saveCompressedImage(output.path, compressedBytes), `is`(false))
        assertThat(output.exists(), `is`(false))
    }

    @Test
    fun `compression should report save failure and recycle the bitmap`() {
        val output = File(temporaryFolder.root, "missing-parent/image.webp")

        assertThat(compressImageFromUri(context, uri, output.path, 100), `is`(false))

        verify(exactly = 1) { bitmap.recycle() }
    }

    @Test
    fun `encoding failure should still recycle the bitmap`() {
        val cause = IllegalStateException("encoding failed")
        every { compressBitmap(bitmap, 100) } throws cause
        val output = File(temporaryFolder.root, "image.webp")

        val thrown =
            assertThrows(IllegalStateException::class.java) {
                compressImageFromUri(context, uri, output.path, 100)
            }

        assertThat(thrown, sameInstance(cause))
        assertThat(output.exists(), `is`(false))
        verify(exactly = 1) { bitmap.recycle() }
    }
}
