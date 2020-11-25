@file:Suppress("HardCodedStringLiteral", "unused")

package ir.logicbase.mockfit

/**
 * Remove query parameters from given url, for example in this url : `users?q=hello&page=2`
 *
 * result will be `users`
 */
public fun String.removeQueryParams(): String = this.replace("\\?.*".toRegex(), "")

/**
 * Replace resource path from given url, for example in this url : `users/2/form`
 *
 * result will be `users/{#}/form`
 */
public fun String.replaceUrlDynamicPath(): String = this.replace("\\b\\d+((,\\d)+)?".toRegex(), "{#}")

/**
 * Replace resource path from given url, for example in this url : `users/{id}/form`
 *
 * result will be `users/{#}/form
 */
public fun String.replaceEndpointDynamicPath(): String = this.replace("\\{(.*?)\\}".toRegex(), "{#}")