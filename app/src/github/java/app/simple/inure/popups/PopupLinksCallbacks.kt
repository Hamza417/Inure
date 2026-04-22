package app.simple.inure.popups

interface PopupLinksCallbacks {
    fun onGithubClicked()
    fun onPlayStoreClicked()
    fun onFdroidClicked() {
        // No-op
    }

    fun onIzzyondroidClicked() {
        // No-op
    }
}