package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommunityView
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toModel

class CommunityRepository(
    private val services: ServiceProvider,
) {

    suspend fun getCommunity(
        auth: String? = null,
        id: Int,
    ): CommunityModel? {
        val response = services.community.getCommunity(
            auth = auth,
            id = id,
        ).body()
        return response?.communityView?.toModel()
    }
}

private fun CommunityView.toModel() = community.toModel()