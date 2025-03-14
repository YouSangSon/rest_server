package yousang.rest.interfaces

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse(
    @JsonProperty("status_code")
    val statusCode: Int,

    @JsonProperty("message")
    val message: String,

    @JsonProperty("data")
    val data: Any? = null
) {
    constructor(status: HttpStatus, message: String, data: Any?) : this(status.value(), message, data)
}