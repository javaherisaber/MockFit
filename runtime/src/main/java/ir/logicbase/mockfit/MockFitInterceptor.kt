package ir.logicbase.mockfit

import okhttp3.*
import java.io.IOException

/**
 * Provide network response from json mock in local device
 * You should remove `Base Url` and `Query parameters` from [apiIncludeIntoMock] or [apiExcludeFromMock]
 * If your request contains `Path parameters` eg. {product_id}, replace it with {#}
 *
 * @property bodyFactory interface to access InputStream for file operations
 * @property logger interface to log messages
 * @property baseUrl url of your remote data source
 * @property requestPathToMockPathRule generated map in MockFitConfig file
 * @property mockFilesPath path to access mock files
 * @property mockFitEnable master setting to enable or disable this interceptor
 * @property apiEnableMock if enabled it will proceed triggering mock response
 * @property apiIncludeIntoMock include specific endpoint into mock
 * @property apiExcludeFromMock exclude specific endpoint from being mocked
 * @property apiResponseLatency simulate latency while sending response to outer layer
 */
@Suppress("HardCodedStringLiteral")
public class MockFitInterceptor constructor(
    private val bodyFactory: BodyFactory,
    private val logger: Logger,
    private val baseUrl: String,
    private val requestPathToMockPathRule: Array<MockPathRule>,
    private val mockFilesPath: String = "",
    private val mockFitEnable: Boolean = true,
    private val apiEnableMock: Boolean = true,
    private val apiIncludeIntoMock: Array<String> = arrayOf(),
    private val apiExcludeFromMock: Array<String> = arrayOf(),
    private val apiResponseLatency: Long = API_DEFAULT_LATENCY
) : Interceptor {

    @Suppress("DefaultLocale")
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!mockFitEnable) {
            return chain.proceed(request)
        }
        val url = request.url().toString()
        val route = url.replace(baseUrl, "")  // remove base url
            .removeQueryParams()
            .replaceUrlDynamicPath() // replace dynamic path (eg. 3,5,7) with {#}

        // for separate same route with different query and response
        val queries = request.url().query()?.split("&") ?: listOf()

        // example requestPath -> [DELETE] shops/{#}/social-accounts/{#}
        val requestMethod = request.method().toUpperCase()
        val apiMockPathRule = MockPathRule(request.method().toUpperCase(), route, "", arrayOf(), arrayOf())

        var canProceedWithMock = apiEnableMock // can we use mock or proceed with network api

        // check if requestPath is included
        for (item in apiIncludeIntoMock) {
            val mockPath = item.replaceEndpointDynamicPath()
            if (mockPath == apiMockPathRule.route) {
                canProceedWithMock = true
                break
            }
        }

        // check if requestPath is excluded
        for (item in apiExcludeFromMock) {
            if (item.replaceEndpointDynamicPath() == apiMockPathRule.route) {
                canProceedWithMock = false
                break
            }
        }

        if (canProceedWithMock) {
            // same path but query is different
            val currentApiPathRule = requestPathToMockPathRule.lastOrNull {
                val removeValueForExcludedQueries = it.excludeQueries.any { it.contains("{#}")}
                val removeValueForIncludedQueries = it.includeQueries.any { it.contains("{#}")}

                val eligibleQueries = queries.filter { query ->
                    if (removeValueForExcludedQueries)
                        !it.excludeQueries.contains(query.replaceUrlDynamicPath())
                    else
                        !it.excludeQueries.contains(query)
                }

                it.method == apiMockPathRule.method &&
                it.route == apiMockPathRule.route &&
                it.includeQueries.isNotEmpty() && it.includeQueries.all { query ->
                    eligibleQueries.contains(query)
                    if (removeValueForIncludedQueries)
                        eligibleQueries.contains(query.replaceUrlDynamicPath())
                    else
                        eligibleQueries.contains(query)
                }
            }

            val json = getMockJsonOrNull(currentApiPathRule)
            return json?.let {
                // json is found
                logger.log(TAG, "--> Mocking [$requestMethod] $url")
                val contentType = MediaType.parse(RESPONSE_MEDIA_TYPE)
                val responseBody = ResponseBody.create(contentType, it)
                Thread.sleep(apiResponseLatency)
                Response.Builder()
                    .body(responseBody)
                    .request(request)
                    .message(RESPONSE_MESSAGE)
                    .protocol(Protocol.HTTP_1_1)
                    .code(RESPONSE_CODE)
                    .build()
            } ?: run {
                // no json found, proceed request from remote data source
                chain.proceed(request)
            }
        } else {
            return chain.proceed(request)
        }
    }

    /**
     * read mock json or if exception occur's return null
     *
     * @param rule of api path
     * @return json in string object
     */
    @Throws(IOException::class)
    private fun getMockJsonOrNull(rule: MockPathRule?): String? {
        if (rule == null) return null

        val eligible = requestPathToMockPathRule.firstOrNull() { it == rule }
        val fileName = eligible?.responseFile ?: return null
        val path = if (mockFilesPath.isEmpty()) fileName else "$mockFilesPath/$fileName"
        return bodyFactory.create(path)
            .bufferedReader()
            .use {
                it.readText()
            }
    }

    private companion object {
        private const val TAG = "MockFit"
        private const val API_DEFAULT_LATENCY = 150L

        private const val RESPONSE_MEDIA_TYPE = "application/json"
        private const val RESPONSE_MESSAGE = "OK"
        private const val RESPONSE_CODE = 200
    }
}
