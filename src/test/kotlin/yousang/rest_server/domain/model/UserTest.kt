package yousang.rest_server.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserTest {

    @Test
    fun `should create valid user`() {
        // Given
        val username = "testuser"
        val email = "test@example.com"
        val password = "password123"

        // When
        val user = User(
            username = username,
            email = email,
            password = password
        )

        // Then
        assertEquals(username, user.username)
        assertEquals(email, user.email)
        assertEquals(password, user.password)
        assertTrue(user.enabled)
        assertEquals(setOf(Role.USER), user.roles)
    }

    @Test
    fun `should throw exception when username is blank`() {
        assertThrows<IllegalArgumentException> {
            User(username = "", email = "test@example.com", password = "password123")
        }
    }

    @Test
    fun `should throw exception when username is too short`() {
        assertThrows<IllegalArgumentException> {
            User(username = "ab", email = "test@example.com", password = "password123")
        }
    }

    @Test
    fun `should throw exception when email is invalid`() {
        assertThrows<IllegalArgumentException> {
            User(username = "testuser", email = "invalid-email", password = "password123")
        }
    }

    @Test
    fun `should disable user`() {
        // Given
        val user = User(username = "testuser", email = "test@example.com", password = "password123")

        // When
        val disabledUser = user.disable()

        // Then
        assertFalse(disabledUser.enabled)
    }

    @Test
    fun `should update user profile`() {
        // Given
        val user = User(username = "testuser", email = "test@example.com", password = "password123")

        // When
        val updatedUser = user.updateProfile(username = "newuser", email = "new@example.com")

        // Then
        assertEquals("newuser", updatedUser.username)
        assertEquals("new@example.com", updatedUser.email)
    }
}
