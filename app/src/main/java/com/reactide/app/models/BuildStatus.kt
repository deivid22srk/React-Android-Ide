package com.reactide.app.models

sealed class BuildStatus {
    object Idle : BuildStatus()
    object Building : BuildStatus()
    object Running : BuildStatus()
    data class Success(val message: String) : BuildStatus()
    data class Error(val message: String) : BuildStatus()
}
