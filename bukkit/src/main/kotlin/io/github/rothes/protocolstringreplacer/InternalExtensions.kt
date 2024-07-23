package io.github.rothes.protocolstringreplacer

import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.reflect.StructureModifier

internal inline fun <reified T> PacketContainer.modifier() = this.modifier.withType<T>(T::class.java)

internal inline operator fun <reified T> StructureModifier<T>.get(field: Int) = this.read(field)

internal inline operator fun <reified T> StructureModifier<T>.set(field: Int, value: T) = this.write(field, value)