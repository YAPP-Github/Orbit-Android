package com.yapp.common.navigation.extensions

import kotlin.reflect.KClass

fun <T : Any> KClass<T>.toRoute(): String = this.simpleName ?: error("Route name missing")
