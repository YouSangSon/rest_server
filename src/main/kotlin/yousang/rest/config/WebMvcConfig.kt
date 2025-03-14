package yousang.rest.config

// 주석 처리하여 WebMVC 구성을 비활성화합니다. WebFlux와 충돌이 발생하므로 제거합니다.
// 이 파일은 참조용으로만 유지합니다. 애플리케이션은 WebFlux 모드로 구성됩니다.
//@Configuration
//class WebMvcConfig(private val objectMapper: ObjectMapper) : WebMvcConfigurer {
//
//    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
//        // Swagger UI 관련 리소스 핸들러 추가
//        registry.addResourceHandler("/swagger-ui.html")
//            .addResourceLocations("classpath:/META-INF/resources/")
//
//        registry.addResourceHandler("/webjars/**")
//            .addResourceLocations("classpath:/META-INF/resources/webjars/")
//
//        registry.addResourceHandler("/swagger-ui/**")
//            .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/")
//    }
//
//    override fun addViewControllers(registry: ViewControllerRegistry) {
//        // Swagger UI 경로로 접근할 때 자동 리다이렉트
//        registry.addRedirectViewController("/swagger-ui", "/swagger-ui/index.html")
//        registry.addRedirectViewController("/api/v1/swagger-ui", "/swagger-ui/index.html")
//    }
//
//    override fun addFormatters(registry: FormatterRegistry) {
//        super.addFormatters(registry)
//    }
//
//    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
//        // Clear any existing converters
//        converters.clear()
//
//        // Add String converter
//        val stringConverter = StringHttpMessageConverter(StandardCharsets.UTF_8)
//        stringConverter.setSupportedMediaTypes(listOf(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON))
//        converters.add(stringConverter)
//
//        // Add JSON converter
//        val jacksonConverter = MappingJackson2HttpMessageConverter(objectMapper)
//        jacksonConverter.setSupportedMediaTypes(
//            listOf(
//                MediaType.APPLICATION_JSON,
//                MediaType.APPLICATION_PROBLEM_JSON,
//                MediaType.APPLICATION_OCTET_STREAM
//            )
//        )
//        converters.add(jacksonConverter)
//    }
//
//    override fun configureContentNegotiation(configurer: ContentNegotiationConfigurer) {
//        configurer
//            .defaultContentType(MediaType.APPLICATION_JSON)
//            .favorParameter(false)
//    }
//}