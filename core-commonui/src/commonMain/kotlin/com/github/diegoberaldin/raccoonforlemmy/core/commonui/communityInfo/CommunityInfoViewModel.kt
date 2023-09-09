package com.github.diegoberaldin.raccoonforlemmy.core.commonui.communityInfo

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel

class CommunityInfoViewModel(
    private val mvi: DefaultMviModel<CommunityInfoMviModel.Intent, CommunityInfoMviModel.UiState, CommunityInfoMviModel.Effect>,
    private val community: CommunityModel,
) : ScreenModel,
    MviModel<CommunityInfoMviModel.Intent, CommunityInfoMviModel.UiState, CommunityInfoMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()
        mvi.updateState { it.copy(community = community) }
    }
}