package yousang.rest_server.adapter.`in`.web.sns

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import yousang.rest_server.application.ports.out.SnsUserRepositoryPort
import yousang.rest_server.application.service.sns.FollowService

/**
 * 사용자 API 컨트롤러
 * /api/v1/sns/users
 */
@RestController
@RequestMapping("/api/v1/sns/users")
class UserController(
    private val userRepository: SnsUserRepositoryPort,
    private val followService: FollowService
) {
    /**
     * GET /api/v1/sns/users/search
     * 사용자 검색
     */
    @GetMapping("/search")
    fun searchUsers(
        @RequestParam q: String,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int
    ): ResponseEntity<UsersResponse> {
        val users = userRepository.search(q, limit, offset)
        return ResponseEntity.ok(UsersResponse(
            data = users.map { SnsUserDto.from(it) },
            meta = PaginationMeta(limit, offset, users.size >= limit)
        ))
    }

    /**
     * GET /api/v1/sns/users/{userId}
     * 사용자 프로필 조회
     */
    @GetMapping("/{userId}")
    fun getUserProfile(@PathVariable userId: Long): ResponseEntity<SnsUserDto> {
        val user = userRepository.findById(userId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(SnsUserDto.from(user))
    }

    /**
     * GET /api/v1/sns/users/{userId}/followers
     * 팔로워 목록 조회
     */
    @GetMapping("/{userId}/followers")
    fun getFollowers(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "50") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int
    ): ResponseEntity<FollowersResponse> {
        val followers = followService.getFollowers(userId, limit, offset)
        return ResponseEntity.ok(FollowersResponse(
            data = followers.map { FollowDto(it.followId, it.followerId, it.followingId, it.createdAt.toString()) },
            meta = PaginationMeta(limit, offset, followers.size >= limit)
        ))
    }

    /**
     * GET /api/v1/sns/users/{userId}/following
     * 팔로잉 목록 조회
     */
    @GetMapping("/{userId}/following")
    fun getFollowing(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "50") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int
    ): ResponseEntity<FollowersResponse> {
        val following = followService.getFollowing(userId, limit, offset)
        return ResponseEntity.ok(FollowersResponse(
            data = following.map { FollowDto(it.followId, it.followerId, it.followingId, it.createdAt.toString()) },
            meta = PaginationMeta(limit, offset, following.size >= limit)
        ))
    }

    /**
     * POST /api/v1/sns/users/{userId}/follow
     * 사용자 팔로우
     */
    @PostMapping("/{userId}/follow")
    fun followUser(
        @PathVariable userId: Long,
        @RequestAttribute("userId") currentUserId: Long
    ): ResponseEntity<Map<String, String>> {
        followService.followUser(currentUserId, userId)
        return ResponseEntity.ok(mapOf("message" to "User followed successfully"))
    }

    /**
     * DELETE /api/v1/sns/users/{userId}/follow
     * 사용자 언팔로우
     */
    @DeleteMapping("/{userId}/follow")
    fun unfollowUser(
        @PathVariable userId: Long,
        @RequestAttribute("userId") currentUserId: Long
    ): ResponseEntity<Map<String, String>> {
        followService.unfollowUser(currentUserId, userId)
        return ResponseEntity.ok(mapOf("message" to "User unfollowed successfully"))
    }
}

data class UsersResponse(
    val data: List<SnsUserDto>,
    val meta: PaginationMeta
)

data class FollowersResponse(
    val data: List<FollowDto>,
    val meta: PaginationMeta
)

data class FollowDto(
    val followId: Long,
    val followerId: Long,
    val followingId: Long,
    val createdAt: String
)
