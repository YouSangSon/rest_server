package yousang.rest_server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RestServerApplication

fun main(args: Array<String>) {
	runApplication<RestServerApplication>(*args)
}
