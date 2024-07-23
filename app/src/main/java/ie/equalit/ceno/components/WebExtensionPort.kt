package ie.equalit.ceno.components

import android.content.Context
import android.util.Log
import androidx.annotation.Nullable
import ie.equalit.ceno.EngineProvider
import org.json.JSONObject
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.WebExtension

class WebExtensionPort(private val context: Context) {

    var mPort: WebExtension.Port? = null

    fun createPort() {
        sRuntime = EngineProvider.getOrCreateRuntime(context)

        val portDelegate: WebExtension.PortDelegate = object : WebExtension.PortDelegate {
            override fun onPortMessage(
                message: Any, port: WebExtension.Port
            ) {
                Log.d("PortDelegate", "Received message from extension: $message")
            }

            override fun onDisconnect(port: WebExtension.Port) {
                // This port is not usable anymore.
                if (port === mPort) {
                    mPort = null
                }
            }
        }

        val messageDelegate: WebExtension.MessageDelegate = object : WebExtension.MessageDelegate {
            @Nullable
            override fun onConnect(port: WebExtension.Port) {
                mPort = port
                mPort!!.setDelegate(portDelegate)

                val message = JSONObject()
                message.put("init", "true")
                mPort!!.postMessage(message)

            }
        }

        sRuntime!!
            .webExtensionController
            .ensureBuiltIn(
                EXTENSION_LOCATION,
                EXTENSION_ID
            )
            .accept( // Register message delegate for background script
                { extension -> setMessageDelegate(extension, messageDelegate) }
            ) { e -> Log.e("MessageDelegate", "Error registering WebExtension", e) }
    }

    private fun setMessageDelegate(
        extension: WebExtension?,
        messageDelegate: WebExtension.MessageDelegate
    ) {
        extension!!.setMessageDelegate(messageDelegate, "browser")
    }

    companion object {
        private var sRuntime: GeckoRuntime? = null
        private const val EXTENSION_LOCATION = "resource://android/assets/addons/ceno/"
        private const val EXTENSION_ID = "ceno@equalit.ie"
    }
}