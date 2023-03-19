package ir.logicbase.mockfit

/**
 * Annotation meta to mark retrofit endpoints
 *
 * @property response path to mock file
 * @property includeQueries are the queries to separating same paths with different responses based on query values.
 * @property excludeQueries are the queries to separating same paths with different responses based on query values.
 * For example: ```["limit={#}"]``` or ```["limit"]``` for just including keys not value. For separate them with value you have to pass value as well like ```["limit=20"]```
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
public annotation class Mock(
    val response: String,
    val includeQueries: Array<String> = [],
    val excludeQueries: Array<String> = [],
)