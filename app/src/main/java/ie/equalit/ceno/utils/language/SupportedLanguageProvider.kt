package ie.equalit.ceno.utils.language

import java.util.Locale

object SupportedLanguageProvider {

    fun getSupportedLocales() = listOf<Locale>(
        Locale.forLanguageTag("ar"),
        Locale.forLanguageTag("cs"),
        Locale.forLanguageTag("de"),
        Locale.forLanguageTag("el"),
        Locale.forLanguageTag("en"),
        Locale.forLanguageTag("es"),
        Locale.forLanguageTag("fa"),
        Locale.forLanguageTag("fr"),
        Locale.forLanguageTag("hu"),
        Locale.forLanguageTag("ia"),
        Locale.forLanguageTag("in"),
        Locale.forLanguageTag("iw"),
        Locale.forLanguageTag("my"),
        Locale.forLanguageTag("pl"),
        Locale.forLanguageTag("pt"),
        Locale.forLanguageTag("ru"),
        Locale.forLanguageTag("sv"),
        Locale.forLanguageTag("ta"),
        Locale.forLanguageTag("tr"),
        Locale.forLanguageTag("uk"),
        Locale.forLanguageTag("ur"),
        Locale.forLanguageTag("zh"),
    )
}