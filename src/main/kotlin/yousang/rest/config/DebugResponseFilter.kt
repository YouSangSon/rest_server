package yousang.rest.config

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.util.ContentCachingResponseWrapper
import yousang.rest.shared.log.log

@Component
class DebugResponseFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val wrappedResponse = ContentCachingResponseWrapper(response as HttpServletResponse)
        
        try {
            chain.doFilter(request, wrappedResponse)
        } finally {
            val responseBody = wrappedResponse.contentAsByteArray
            if (responseBody.isNotEmpty()) {
                log.info("Response body length: ${responseBody.size}")
                log.info("Response body content: ${String(responseBody)}")
            } else {
                log.info("Response body is empty!")
            }
            
            // Copy content to the original response
            wrappedResponse.copyBodyToResponse()
        }
    }
} 