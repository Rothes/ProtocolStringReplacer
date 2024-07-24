package io.github.rothes.protocolstringreplacer

import com.comphenix.protocol.wrappers.WrappedChatComponent

internal val plugin = ProtocolStringReplacer.getInstance()
internal fun componentToJson(any: Any) = WrappedChatComponent.fromHandle(any).json
internal fun jsonToComponent(json: String) = WrappedChatComponent.fromJson(json).handle