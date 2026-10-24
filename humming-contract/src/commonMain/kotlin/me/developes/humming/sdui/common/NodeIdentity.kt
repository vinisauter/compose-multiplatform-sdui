package me.developes.humming.sdui.common

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Generated IDs are local identities; explicit payload IDs retain precedence. */
@OptIn(ExperimentalUuidApi::class)
internal fun generateNodeIdentity(): String = Uuid.random().toString()
