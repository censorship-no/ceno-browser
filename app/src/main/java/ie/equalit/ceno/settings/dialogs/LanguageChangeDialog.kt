package ie.equalit.ceno.settings.dialogs

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat.getString
import androidx.core.view.children
import ie.equalit.ceno.R
import ie.equalit.ceno.utils.language.SupportedLanguageProvider
import java.util.Locale


class LanguageChangeDialog(
    val context: Context,
    private val setLanguageListener: SetLanguageListener?
) {

    private val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    private var allSupportedLocales = mutableListOf<Locale>()
    private var currentLocale: Locale? = null

    init {

        currentLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
        } else {
            Locale.getDefault()
        }

        val languageChangeDialogView = View.inflate(context, R.layout.language_change_dialog, null)
        val radioGroup = languageChangeDialogView.findViewById<RadioGroup>(R.id.radio_group)

        builder.apply {
            setTitle(getString(context, R.string.change_language))
            setView(languageChangeDialogView)
            setNegativeButton(R.string.customize_addon_collection_cancel) { dialog: DialogInterface, _ -> dialog.cancel() }
            setPositiveButton(R.string.update) { _, _ ->
                if (radioGroup.checkedRadioButtonId == -1) {
                    return@setPositiveButton
                }

                val checkedIndex =
                    radioGroup.children.indexOfFirst { it.id == radioGroup.checkedRadioButtonId }

                allSupportedLocales[checkedIndex].let { locale ->
                    setLanguageListener?.onLanguageSelected(locale)
                }
            }

            // clear list
            allSupportedLocales.clear()

            // Add currentLocale as first view
            currentLocale?.let { current ->
                val radioButton = LayoutInflater.from(context).inflate(
                    R.layout.item_langauge,
                    radioGroup,
                    false
                ) as RadioButton
                radioButton.apply {
                    isClickable = true
                    text = current.displayLanguage
                }
                radioGroup.addView(radioButton)
                radioButton.performClick()

                allSupportedLocales.add(current)
            }

            // Add subsequent locales
            SupportedLanguageProvider.getSupportedLocales().forEach {
                if (it.displayLanguage != currentLocale?.displayLanguage && !it.toLanguageTag()
                        .contains("-")
                ) {
                    val radioButton = LayoutInflater.from(context).inflate(
                        R.layout.item_langauge,
                        radioGroup,
                        false
                    ) as RadioButton
                    radioButton.apply {
                        isClickable = true
                        text = it.displayLanguage
                    }
                    radioGroup.addView(radioButton)
                    allSupportedLocales.add(it)
                }
            }
        }
    }

    fun getDialog(): AlertDialog {
        return builder.create()
    }

    interface SetLanguageListener {
        fun onLanguageSelected(locale: Locale)
    }
}