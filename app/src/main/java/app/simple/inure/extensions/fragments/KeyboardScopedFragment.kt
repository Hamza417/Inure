package app.simple.inure.extensions.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import app.simple.inure.decorations.ime.ControlFocusInsetsAnimationCallback
import app.simple.inure.decorations.ime.HeightDeferringInsetsAnimationCallback
import app.simple.inure.decorations.ime.RootViewDeferringInsetsCallback
import app.simple.inure.decorations.ime.TranslateDeferringInsetsAnimationCallback

open class KeyboardScopedFragment : ScopedFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.addHeightKeyboardCallbacks()
    }

    /**
     * Call this function on the root view of your layout to enable the keyboard height being
     * intercepted by touch (swipe up/down) and the keyboard height being animated when the
     * keyboard is shown/hidden.
     */
    protected fun View.addHeightKeyboardCallbacks() {
        /**
         * Since our Activity has declared `window.setDecorFitsSystemWindows(false)`, we need to
         * handle any [WindowInsetsCompat] as appropriate.
         *
         * Our [RootViewDeferringInsetsCallback] will update our attached view's padding to match
         * the combination of the [WindowInsetsCompat.Type.systemBars], and selectively apply the
         * [WindowInsetsCompat.Type.ime] insets, depending on any ongoing WindowInsetAnimations
         * (see that class for more information).
         */
        @Suppress("UNUSED_VARIABLE") val deferringInsetsListener = RootViewDeferringInsetsCallback(
                persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                deferredInsetTypes = WindowInsetsCompat.Type.ime()
        )

        /**
         * The second step is reacting to any animations which run. This can be system driven,
         * such as the user focusing on an EditText and on-screen keyboard (IME) coming on screen,
         * or app driven (more on that in step 3).
         *
         * To react to animations, we set an [android.view.WindowInsetsAnimation.Callback] on any
         * views which we wish to react to inset animations. In this example, we want our
         * EditText holder view, and the conversation RecyclerView to react.
         *
         * We use our [TranslateDeferringInsetsAnimationCallback] class, bundled in this sample,
         * which will automatically move each view as the IME animates.
         *
         * Note about [TranslateDeferringInsetsAnimationCallback], it relies on the behavior of
         * [RootViewDeferringInsetsCallback] on the layout's root view.
         */
        ViewCompat.setWindowInsetsAnimationCallback(
                this,
                HeightDeferringInsetsAnimationCallback(
                        view = this,
                        persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                        deferredInsetTypes = WindowInsetsCompat.Type.ime(),
                        // We explicitly allow dispatch to continue down to binding.messageHolder's
                        // child views, so that step 2.5 below receives the call
                        dispatchMode = WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
                )
        )
        ViewCompat.setWindowInsetsAnimationCallback(
                this,
                HeightDeferringInsetsAnimationCallback(
                        view = this,
                        persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                        deferredInsetTypes = WindowInsetsCompat.Type.ime()
                )
        )
    }

    /**
     * Call this function on the root view of your layout to enable the view being translated
     * when the keyboard is shown/hidden.
     */
    protected fun View.addTranslateKeyboardCallbacks() {
        /**
         * Since our Activity has declared `window.setDecorFitsSystemWindows(false)`, we need to
         * handle any [WindowInsetsCompat] as appropriate.
         *
         * Our [RootViewDeferringInsetsCallback] will update our attached view's padding to match
         * the combination of the [WindowInsetsCompat.Type.systemBars], and selectively apply the
         * [WindowInsetsCompat.Type.ime] insets, depending on any ongoing WindowInsetAnimations
         * (see that class for more information).
         */
        @Suppress("UNUSED_VARIABLE") val deferringInsetsListener = RootViewDeferringInsetsCallback(
                persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                deferredInsetTypes = WindowInsetsCompat.Type.ime()
        )

        /**
         * The second step is reacting to any animations which run. This can be system driven,
         * such as the user focusing on an EditText and on-screen keyboard (IME) coming on screen,
         * or app driven (more on that in step 3).
         *
         * To react to animations, we set an [android.view.WindowInsetsAnimation.Callback] on any
         * views which we wish to react to inset animations. In this example, we want our
         * EditText holder view, and the conversation RecyclerView to react.
         *
         * We use our [TranslateDeferringInsetsAnimationCallback] class, bundled in this sample,
         * which will automatically move each view as the IME animates.
         *
         * Note about [TranslateDeferringInsetsAnimationCallback], it relies on the behavior of
         * [RootViewDeferringInsetsCallback] on the layout's root view.
         */
        ViewCompat.setWindowInsetsAnimationCallback(
                this,
                HeightDeferringInsetsAnimationCallback(
                        view = this,
                        persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                        deferredInsetTypes = WindowInsetsCompat.Type.ime(),
                        // We explicitly allow dispatch to continue down to binding.messageHolder's
                        // child views, so that step 2.5 below receives the call
                        dispatchMode = WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
                )
        )
        ViewCompat.setWindowInsetsAnimationCallback(
                this,
                HeightDeferringInsetsAnimationCallback(
                        view = this,
                        persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                        deferredInsetTypes = WindowInsetsCompat.Type.ime()
                )
        )
    }

    protected fun EditText.setWindowInsetsAnimationCallback() {
        /**
         * 2.5) We also want to make sure that our EditText is focused once the IME
         * is animated in, to enable it to accept input. Similarly, if the IME is animated
         * off screen and the EditText is focused, we should clear that focus.
         *
         * The bundled [ControlFocusInsetsAnimationCallback] callback will automatically request
         * and clear focus for us.
         *
         * Since `binding.messageEdittext` is a child of `binding.messageHolder`, this
         * [WindowInsetsAnimationCompat.Callback] will only work if the ancestor view's callback uses the
         * [WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE] dispatch mode, which
         * we have done above.
         */
        ViewCompat.setWindowInsetsAnimationCallback(this, ControlFocusInsetsAnimationCallback(this)
        )
    }
}