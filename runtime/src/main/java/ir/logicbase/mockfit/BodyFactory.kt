package ir.logicbase.mockfit

import java.io.IOException
import java.io.InputStream

/**
 * interface to access InputStream for file operations
 */
public fun interface BodyFactory {
    @Throws(IOException::class)
    public fun create(input: String): InputStream
}