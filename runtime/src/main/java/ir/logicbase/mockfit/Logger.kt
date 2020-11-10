package ir.logicbase.mockfit

/**
 * Log messages during intercept operation
 */
public fun interface Logger {
    public fun log(tag: String, message: String)
}