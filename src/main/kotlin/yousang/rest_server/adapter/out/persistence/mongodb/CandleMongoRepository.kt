package yousang.rest_server.adapter.out.persistence.mongodb

import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface CandleMongoRepository : MongoRepository<CandleDocument, String> {

    @Query("{'symbol': ?0, 'exchange': ?1, 'interval': ?2, 'openTime': {'\$gte': ?3, '\$lte': ?4}}")
    fun findBySymbolAndExchangeAndIntervalAndOpenTimeBetween(
        symbol: String,
        exchange: String,
        interval: String,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<CandleDocument>

    fun findBySymbolAndExchangeAndIntervalOrderByOpenTimeDesc(
        symbol: String,
        exchange: String,
        interval: String,
        pageable: Pageable
    ): List<CandleDocument>

    @Query("{'openTime': {'\$lt': ?0}}")
    fun deleteByOpenTimeBefore(before: LocalDateTime)
}
