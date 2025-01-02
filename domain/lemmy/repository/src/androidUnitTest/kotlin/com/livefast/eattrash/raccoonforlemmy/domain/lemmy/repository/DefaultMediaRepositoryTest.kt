package com.livefast.eattrash.raccoonforlemmy.domain.lemmy.repository

import com.livefast.eattrash.raccoonforlemmy.core.api.provider.ServiceProvider
import com.livefast.eattrash.raccoonforlemmy.core.api.service.PostService
import com.livefast.eattrash.raccoonforlemmy.core.api.service.UserService
import com.livefast.eattrash.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.livefast.eattrash.raccoonforlemmy.domain.lemmy.data.MediaModel
import com.livefast.eattrash.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultMediaRepositoryTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val userService = mockk<UserService>()
    private val postService = mockk<PostService>(relaxUnitFun = true)
    private val serviceProvider =
        mockk<ServiceProvider> {
            every { user } returns userService
            every { post } returns postService
            every { currentInstance } returns INSTANCE
        }
    private val sut =
        DefaultMediaRepository(
            services = serviceProvider,
        )

    @Test
    fun whenUploadImage_thenInteractionsAreAsExpected() =
        runTest {
            coEvery {
                postService.uploadImage(
                    authHeader = any(),
                    url = any(),
                    token = any(),
                    content = any(),
                )
            } returns mockk(relaxed = true)

            sut.uploadImage(
                auth = AUTH_TOKEN,
                bytes = byteArrayOf(),
            )

            coVerify {
                postService.uploadImage(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    url = "https://$INSTANCE/pictrs/image",
                    token = "jwt=${AUTH_TOKEN}",
                    content = any(),
                )
            }
        }

    @Test
    fun givenNoResults_whenGetAll_thenResultAndInteractionsAreAsExpected() =
        runTest {
            coEvery {
                userService.listMedia(
                    authHeader = any(),
                    page = any(),
                    limit = any(),
                )
            } returns
                mockk {
                    every { images } returns emptyList()
                }

            val res =
                sut.getAll(
                    auth = AUTH_TOKEN,
                    page = 1,
                )

            assertTrue(res.isEmpty())
            coVerify {
                userService.listMedia(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    page = 1,
                    limit = 20,
                )
            }
        }

    @Test
    fun givenResults_whenGetAll_thenResultAndInteractionsAreAsExpected() =
        runTest {
            coEvery {
                userService.listMedia(
                    authHeader = any(),
                    page = any(),
                    limit = any(),
                )
            } returns
                mockk {
                    every { images } returns
                        listOf(
                            mockk(relaxed = true) {
                                every { localImage } returns
                                    mockk(relaxed = true) { every { pictrsAlias } returns "fake-alias" }
                            },
                        )
                }

            val res =
                sut.getAll(
                    auth = AUTH_TOKEN,
                    page = 1,
                )

            assertEquals(1, res.size)
            assertEquals("fake-alias", res.first().alias)
            coVerify {
                userService.listMedia(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    page = 1,
                    limit = 20,
                )
            }
        }

    @Test
    fun whenDelete_thenInteractionsAreAsExpected() =
        runTest {
            val instance = "fake-instance"

            val media = MediaModel(alias = "fake-alias", deleteToken = "fake-delete-token")
            sut.delete(
                auth = AUTH_TOKEN,
                media = media,
            )

            coVerify {
                postService.deleteImage(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    url = "https://$instance/pictrs/image/delete/fake-delete-token/fake-alias",
                    token = "jwt=${AUTH_TOKEN}",
                )
            }
        }

    companion object {
        private const val AUTH_TOKEN = "fake-auth-token"
        private const val INSTANCE = "fake-instance"
    }
}
