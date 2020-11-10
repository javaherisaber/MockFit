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
 * @property requestPathToJsonMap generated map in MockFitConfig file
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
    private val requestPathToJsonMap: Map<String, String>,
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

        // example requestPath -> [DELETE] shops/{#}/social-accounts/{#}
        val requestMethod = request.method().toUpperCase()
        val requestPath = "[${request.method().toUpperCase()}] $route"

        var canProceedWithMock = apiEnableMock // can we use mock or proceed with network api

        // check if requestPath is included
        for (item in apiIncludeIntoMock) {
            if (item.replaceEndpointDynamicPath() == requestPath) {
                canProceedWithMock = true
                break
            }
        }

        // check if requestPath is excluded
        for (item in apiExcludeFromMock) {
            if (item.replaceEndpointDynamicPath() == requestPath) {
                canProceedWithMock = false
                break
            }
        }

        if (canProceedWithMock) {
            val json = getMockJsonOrNull(requestPath)
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
     * @param request api request
     * @return json in string object
     */
    @Throws(IOException::class)
    private fun getMockJsonOrNull(request: String): String? {
        val fileName = requestPathToJsonMap[request] ?: return null
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
