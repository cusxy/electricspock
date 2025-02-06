package ru.cusxy.mgga.electricspock.internal.utils

import java.lang.reflect.Field

/**
 * hack - get a security-protected field in Java 17
 *
 * required "--add-opens java.base/java.lang=ALL-UNNAMED" jvm arg.
 */
@Suppress("UNCHECKED_CAST")
internal fun findSecurityProtectedField(clazz: Class<*>, name: String): Field? {
    val getDeclaredFields0 = Class::class.java.getDeclaredMethod("getDeclaredFields0", Boolean::class.java)
    getDeclaredFields0.isAccessible = true
    val fields = getDeclaredFields0.invoke(clazz, false) as Array<Field>
    return fields.find { field -> field.name == name }
}