package ie.equalit.ceno.settings.changeicon.appicons

class AppIconsInteractor(
    private val controller: AppIconsController,
) {
    fun onSelectAppIcon(icon: AppIcon) {
        controller.handleAppIconClicked(icon)
    }
}
