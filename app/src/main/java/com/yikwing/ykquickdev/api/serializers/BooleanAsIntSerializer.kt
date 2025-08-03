package com.yikwing.ykquickdev.api.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Boolean to Int
 * */
object BooleanAsIntSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Boolean", PrimitiveKind.INT)

    override fun serialize(
        encoder: Encoder,
        value: Boolean,
    ) {
        encoder.encodeInt(if (value) 1 else 0)
    }

    override fun deserialize(decoder: Decoder): Boolean = decoder.decodeInt() != 0
}
