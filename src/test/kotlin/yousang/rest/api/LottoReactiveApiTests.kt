package yousang.rest.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import yousang.rest.interfaces.ApiResponse

/**
 * WebFlux, 코루틴, Flow를 활용한 비동기 로또 API 테스트
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class LottoReactiveApiTests {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    private val apiPath = "/api/v1"

    /**
     * 로또 번호 범위 조회 API 테스트
     */
    @Test
    fun testGetLottoNumbers() {
        webTestClient.get()
            .uri("$apiPath/lotto/numbers?firstDrwNo=1&lastDrwNo=5")
            .exchange()
            .expectStatus().isOk
            .expectBody<ApiResponse>()
            .consumeWith { result ->
                val response = result.responseBody
                assert(response != null)
                assert(response?.statusCode == 200)
                assert(response?.data != null)
                
                // 데이터가 List 타입인지 확인
                val dataList = response?.data as? List<*>
                assert(dataList != null)
                
                println("Lotto numbers API returned ${dataList?.size ?: 0} items")
            }
    }

    /**
     * 단일 로또 조회 API 테스트
     */
    @Test
    fun testGetSingleLotto() {
        webTestClient.get()
            .uri("$apiPath/lotto/single?drwNo=1")
            .exchange()
            .expectStatus().isOk
            .expectBody<ApiResponse>()
            .consumeWith { result ->
                val response = result.responseBody
                assert(response != null)
                assert(response?.statusCode == 200)
                
                // 단일 로또 데이터 검증
                val dataList = response?.data as? List<*>
                if (dataList != null && dataList.isNotEmpty()) {
                    val lottoData = dataList[0] as? Map<*, *>
                    assert(lottoData?.containsKey("drwNo") == true)
                    println("Single lotto API returned data for draw number: ${lottoData?.get("drwNo")}")
                }
            }
    }

    /**
     * 로또 번호 저장 API 테스트
     */
    @Test
    fun testPutLottoNumbers() {
        webTestClient.put()
            .uri("$apiPath/lotto/numbers")
            .exchange()
            .expectStatus().isOk
            .expectBody<ApiResponse>()
            .consumeWith { result ->
                val response = result.responseBody
                assert(response != null)
                assert(response?.statusCode == 200)
                println("Put lotto numbers API response: ${response?.message}")
            }
    }
} 