package ie.equalit.ceno.share

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import mozilla.components.browser.state.action.BrowserAction
import mozilla.components.browser.state.action.EngineAction
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.lib.state.Middleware
import mozilla.components.lib.state.MiddlewareContext

/**
 * [BrowserAction] middleware reacting in response to Save to PDF related [Action]s.
 *
 * @param context An Application context.
 * @param mainScope Coroutine scope to launch coroutines.
 * @param nimbusEventStore Nimbus event store for recording events.
 */
class SaveToPDFMiddleware(
    private val context: Context,
    private val mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : Middleware<BrowserState, BrowserAction> {

    override fun invoke(
        ctx: MiddlewareContext<BrowserState, BrowserAction>,
        next: (BrowserAction) -> Unit,
        action: BrowserAction,
    ) {
        when (action) {
            is EngineAction.SaveToPdfAction -> {
//                postTelemetryTapped(ctx.state.findTab(action.tabId), isPrint = false)
                // Continue to generate the PDF, passing through here to add telemetry
                next(action)
            }

            is EngineAction.SaveToPdfCompleteAction -> {
//                postTelemetryCompleted(ctx.state.findTab(action.tabId), isPrint = false)
            }

            is EngineAction.SaveToPdfExceptionAction -> {
//                context.components.appStore.dispatch(
//                    AppAction.UpdateStandardSnackbarErrorAction(
//                        StandardSnackbarError(
//                            context.getString(R.string.unable_to_save_to_pdf_error),
//                        ),
//                    ),
//                )
//                postTelemetryFailed(ctx.state.findTab(action.tabId), action.throwable, isPrint = false)
            }

            is EngineAction.PrintContentAction -> {
//                postTelemetryTapped(ctx.state.findTab(action.tabId), isPrint = true)
                // Continue to print, passing through here to add telemetry
                next(action)
            }

            is EngineAction.PrintContentCompletedAction -> {
//                postTelemetryCompleted(ctx.state.findTab(action.tabId), isPrint = true)
            }

            is EngineAction.PrintContentExceptionAction -> {
//                context.components.appStore.dispatch(
//                    AppAction.UpdateStandardSnackbarErrorAction(
//                        StandardSnackbarError(
//                            context.getString(R.string.unable_to_print_page_error),
//                        ),
//                    ),
//                )
//                postTelemetryFailed(ctx.state.findTab(action.tabId), action.throwable, isPrint = true)
            }

            else -> {
                next(action)
            }
        }
    }
}
