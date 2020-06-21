package com.orangeman.probemanapp.db.repository.domain

import com.orangeman.probemanapp.customview.domain.Recognition

data class Profile(
    val imageName: String,
    val recognitions: Map<Int, List<Recognition>>
)