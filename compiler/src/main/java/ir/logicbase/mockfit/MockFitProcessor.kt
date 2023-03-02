package ir.logicbase.mockfit

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import retrofit2.http.*
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

/**
 * Kapt processor to generate source code based on functions annotated with [Mock] meta
 *
 * We are using Google's [AutoService](https://github.com/google/auto/tree/master/service) to run our annotation processor
 *
 * Also to generate source code we use Square's [KotlinPoet](https://github.com/square/kotlinpoet) library
 */
@Suppress("HardCodedStringLiteral")
@AutoService(Processor::class)
internal class MockFitProcessor : AbstractProcessor() {

    private val requestToJsonPath = mutableListOf<MockPathRule>()

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(
        Mock::class.java.name,
    )

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.RELEASE_8

    override fun process(
        annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment
    ): Boolean {
        roundEnv.getElementsAnnotatedWith(Mock::class.java).forEach { processMock(it) }
        generateSourceCode()
        return false
    }

    private fun processMock(element: Element) {
        var requestMethodCount = 0
        val mockAnnotation = (element.getAnnotation(Mock::class.java))
        val includeQuery = mockAnnotation.includeQueries
        val excludeQuery = mockAnnotation.excludeQueries
        val jsonPath = mockAnnotation.response

        element.getAnnotation(GET::class.java)?.let {
            requestToJsonPath += MockPathRule("GET", it.value, jsonPath, includeQuery, excludeQuery)
            requestMethodCount++
        }
        element.getAnnotation(POST::class.java)?.let {
            requestToJsonPath += MockPathRule("POST", it.value, jsonPath,  includeQuery, excludeQuery)
            requestMethodCount++
        }
        element.getAnnotation(PATCH::class.java)?.let {
            requestToJsonPath += MockPathRule("PATCH", it.value, jsonPath,  includeQuery, excludeQuery)
            requestMethodCount++
        }
        element.getAnnotation(PUT::class.java)?.let {
            requestToJsonPath += MockPathRule("PUT", it.value, jsonPath,  includeQuery, excludeQuery)
            requestMethodCount++
        }
        element.getAnnotation(HEAD::class.java)?.let {
            requestToJsonPath += MockPathRule("HEAD", it.value, jsonPath,  includeQuery, excludeQuery)
        }
        element.getAnnotation(DELETE::class.java)?.let {
            requestToJsonPath += MockPathRule("DELETE", it.value, jsonPath, includeQuery, excludeQuery)
            requestMethodCount++
        }
        element.getAnnotation(HTTP::class.java)?.let {
            requestToJsonPath += MockPathRule(it.method, it.path, jsonPath, includeQuery, excludeQuery)
        }

        if (requestMethodCount > 1) {
            val packageName = processingEnv.elementUtils.getPackageOf(element).toString()
            val className = element.enclosingElement.simpleName
            throw IllegalStateException(
                "${packageName}.${className}.${element.simpleName} cannot have multiple method type"
            )
        }
    }

    private fun generateSourceCode() {
        val packageName = this.javaClass.`package`.name
        val fileName = "MockFitConfig"
        val fileBuilder = FileSpec.builder(packageName, fileName)
        val classBuilder = TypeSpec.objectBuilder(fileName)
        classBuilder.addProperty(
            PropertySpec.builder(
                "REQUEST_TO_JSON",
                Array::class.asClassName().parameterizedBy(
                    MockPathRule::class.asTypeName()
                )
            ).initializer(
                """
                |arrayOf(
                | ${generateRequestToJsonPathSource()}
                |)
                """.trimMargin()
            ).addAnnotation(JvmField::class.java)
                .build()
        )
        val file = fileBuilder.addType(classBuilder.build()).build()
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        file.writeTo(File(kaptKotlinGeneratedDir!!))
    }

    private fun generateRequestToJsonPathSource(): String {
        val className = MockPathRule::class.java.name
        return requestToJsonPath.joinToString(",") { rule ->
            // replace path identifier with # so that interceptor can operate it
            val requestPath = rule.route.replaceEndpointDynamicPath()
            val includeQueries = """arrayOf(${rule.includeQueries.joinToString(",") { """"$it""""}})"""
            val excludeQueries = """arrayOf(${rule.excludeQueries.joinToString(","){ """"$it""""}})"""

            """${className}("${rule.method}", "$requestPath", "${rule.responseFile}", $includeQueries, $excludeQueries)"""
        }
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}