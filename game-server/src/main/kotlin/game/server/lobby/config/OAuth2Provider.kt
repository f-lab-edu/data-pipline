package game.server.lobby.config

enum class OAuth2Provider(val providerName: String) {
    GOOGLE("google") {
        override fun extractProviderId(attrs: Map<String, Any>): String =
            attrs["sub"] as? String ?: throw IllegalArgumentException("Google user id(sub) is missing")
    },
    KAKAO("kakao") {
        override fun extractProviderId(attrs: Map<String, Any>): String =
            (attrs["id"] as? Long)?.toString() ?: throw IllegalArgumentException("Kakao user id is missing")
    },
    NAVER("naver") {
        override fun extractProviderId(attrs: Map<String, Any>): String =
            ((attrs["response"] as? Map<*, *>)?.get("id"))?.toString()
                ?: throw IllegalArgumentException("Naver user id is missing")
    };

    abstract fun extractProviderId(attrs: Map<String, Any>): String

    companion object {
        fun from(providerName: String): OAuth2Provider =
            entries.firstOrNull { it.providerName.equals(providerName, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unsupported OAuth2 provider: $providerName")
    }
}