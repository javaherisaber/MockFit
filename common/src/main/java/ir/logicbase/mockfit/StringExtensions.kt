@file:Suppress("HardCodedStringLiteral", "unused")

package ir.logicbase.mockfit

/**
 * Remove query parameters from given url, for example in this url : `users?q=hello&page=2`
 *
 * result will be `users`
 */
public fun String.removeQueryParams(): String = this.replace("\\?.*".toRegex(), "")

/**
 * Replace resource path from given url, for example in this url : `users/2/form` or `user/cb7d8a83-0b25-4cca-911b-49a1974f1193/form`
 *
 * result will be `users/{#}/form`
 */
public fun String.replaceUrlDynamicPath(): String = this.replace("[\\da-f]{8}-[\\da-f]{4}-4[\\da-f]{3}-[89ab][\\da-f]{3}-[\\da-f]{12}|\\b\\d+((,\\d)+)?".toRegex(), "{#}")

/**
 * Replace resource path from given url, for example in this url : `users/{id}/form`
 *
 * result will be `users/{#}/form
 */
public fun String.replaceEndpointDynamicPath(): String = this.replace("\\{(.*?)\\}".toRegex(), "{#}")