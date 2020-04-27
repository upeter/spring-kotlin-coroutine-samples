package org.up.utils

import org.slf4j.MDC
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.*

fun <T : Any> Optional<T>.toNullable(): T? {
    return if (this.isPresent) {
        this.get()
    } else {
        null
    }
}

fun withMdc(runnable: Runnable): Runnable? {
    val mdc: Map<String, String> = MDC.getCopyOfContextMap()
    return Runnable {
        MDC.setContextMap(mdc)
        runnable.run()
    }
}

fun <U> withMdc(supplier: () -> U): U? {
    val mdc: Map<String, String> = MDC.getCopyOfContextMap()
    return MDC.setContextMap(mdc).let{supplier()}
}

//@Component
//@Order(1)
//class MDCRequestFilter : Filter {
//    @Throws(ServletException::class)
//    fun init(filterConfig: FilterConfig?) {
//    }
//
//    @Throws(IOException::class, ServletException::class)
//    fun doFilter(request: ServletRequest, response: ServletResponse?, chain: FilterChain) {
//        try {
//            val req: HttpServletRequest = request as HttpServletRequest
//            putRequestInformationOnMdc(req)
//            chain.doFilter(request, response)
//        } finally {
//            MDC.clear()
//        }
//    }
//
//    fun destroy() {}
//    private fun putRequestInformationOnMdc(request: HttpServletRequest) {
//        val extRequestId: String = request.getHeader("x-amzn-trace-id")
//        val extConsumerId: String = request.getHeader("x-consumer-id")
//        val extConsumerName: String = request.getHeader("x-consumer-username")
//        val requestId = extRequestId ?: UUID.randomUUID().toString()
//        request.setAttribute(MDC_REQUEST_ID, requestId)
//        MDC.put(MDC_REQUEST_ID, requestId)
//        if (extConsumerId != null) MDC.put(MDC_CONSUMER_ID, extConsumerId)
//        if (extConsumerName != null) MDC.put(MDC_CONSUMER_NAME, extConsumerName)
//        val cookies: Array<Cookie> = request.getCookies()
//        if (cookies != null) Arrays.stream(cookies).filter({ c -> c.getName().equals("JSESSIONID") })
//                .findAny()
//                .ifPresent({ c -> MDC.put(SESSION_ID, StringUtils.abbreviate(c.getValue(), MAX_LENGTH_SESSION_ID)) })
//    }
//
//    companion object {
//        const val MDC_REQUEST_ID = "request_id"
//        const val MDC_CONSUMER_ID = "consumer_id"
//        const val MDC_CONSUMER_NAME = "consumer_name"
//        const val SESSION_ID = "session_id"
//        private const val MAX_LENGTH_SESSION_ID = 50
//    }
//}
//
