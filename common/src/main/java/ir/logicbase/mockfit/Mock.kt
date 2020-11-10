package ir.logicbase.mockfit

/**
 * Annotation meta to mark retrofit endpoints
 *
 * @property response path to mock file
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
public annotation class Mock(
    val response: String
)