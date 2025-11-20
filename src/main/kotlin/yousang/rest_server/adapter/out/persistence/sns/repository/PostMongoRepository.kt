package yousang.rest_server.adapter.out.persistence.sns.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import yousang.rest_server.adapter.out.persistence.sns.document.PostDocument

@Repository
interface PostMongoRepository : MongoRepository<PostDocument, Long>, PostCustomRepository {

    @Query("{ 'userId': ?0, 'isHidden': false }")
    fun findByUserId(userId: Long, pageable: Pageable): List<PostDocument>

    @Query("{ 'userId': { '\$in': ?0 }, 'isHidden': false }")
    fun findByUserIdIn(userIds: List<Long>, pageable: Pageable): List<PostDocument>

    @Query("{ 'hashtags': ?0, 'isHidden': false }")
    fun findByHashtag(hashtag: String, pageable: Pageable): List<PostDocument>

    @Query("{ 'isHidden': false }")
    fun findAllVisible(pageable: Pageable): List<PostDocument>

    fun countByUserId(userId: Long): Long
}
