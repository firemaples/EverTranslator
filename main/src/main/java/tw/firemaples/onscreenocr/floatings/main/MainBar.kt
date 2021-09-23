package tw.firemaples.onscreenocr.floatings.main

import android.content.Context
import android.view.View
import android.widget.TextView
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.base.MovableFloatingView
import tw.firemaples.onscreenocr.floatings.manager.FloatingStateManager
import tw.firemaples.onscreenocr.floatings.manager.State
import tw.firemaples.onscreenocr.floatings.menu.MenuView
import tw.firemaples.onscreenocr.floatings.translationSelectPanel.TranslationSelectPanel
import tw.firemaples.onscreenocr.utils.showOrHide

class MainBar(context: Context) : MovableFloatingView(context) {
    override val layoutId: Int
        get() = R.layout.floating_main_bar

    override val moveToEdgeAfterMoved: Boolean
        get() = true

    override val fadeOutAfterMoved: Boolean
        get() = !arrayOf(State.ScreenCircling, State.ScreenCircled)
            .contains(FloatingStateManager.currentState)

    private val btLangSelector: View = rootView.findViewById(R.id.bt_langSelector)
    private val tvLang: TextView = rootView.findViewById(R.id.tv_lang)
    private val ivGoogleTranslator: View = rootView.findViewById(R.id.iv_googleTranslator)
    private val btSelect: View = rootView.findViewById(R.id.bt_select)
    private val btTranslate: View = rootView.findViewById(R.id.bt_translate)

    //    private val btOCROnly: View = rootView.findViewById(R.id.bt_ocrOnly)
    private val btClose: View = rootView.findViewById(R.id.bt_close)
    private val btMenu: View = rootView.findViewById(R.id.bt_menu)

    private val menuView: MenuView by lazy {
        MenuView(context, false).apply {
            setAnchor(btMenu)
            onItemSelected = { view, key ->
                view.detachFromScreen()
                viewModel.onMenuItemClicked(key)
            }
        }
    }

    private val viewModel: MainBarViewModel by lazy { MainBarViewModel(viewScope) }

    init {
        setViews()
        setDragView(btMenu)
    }

    private fun setViews() {
        tvLang.text = "En>"
        ivGoogleTranslator.visibility = View.VISIBLE

        btSelect.visibility = View.VISIBLE

        btLangSelector.setOnClickListener {
            rescheduleFadeOut()
            TranslationSelectPanel(context).attachToScreen()
        }

        btSelect.setOnClickListener {
            FloatingStateManager.startScreenCircling()
        }

        btClose.setOnClickListener {
            FloatingStateManager.cancelScreenCircling()
        }

        btMenu.setOnClickListener {
            viewModel.onMenuButtonClicked()
        }

        viewModel.languageText.observe(lifecycleOwner) {
            tvLang.text = it
        }

        viewModel.displayGoogleTranslateIcon.observe(lifecycleOwner) {
            ivGoogleTranslator.showOrHide(it)
        }

        viewModel.displaySelectButton.observe(lifecycleOwner) {
            btSelect.showOrHide(it)
        }

        viewModel.displayTranslateButton.observe(lifecycleOwner) {
            btTranslate.showOrHide(it)
        }

        viewModel.displayCloseButton.observe(lifecycleOwner) {
            btClose.showOrHide(it)
        }

        viewModel.displayMenuItems.observe(lifecycleOwner) {
            with(menuView) {
                updateData(it)
                attachToScreen()
            }
        }

        viewModel.rescheduleFadeOut.observe(lifecycleOwner) {
            rescheduleFadeOut()
        }
//        btOCROnly.visibility = View.VISIBLE
//        btClose.visibility = View.VISIBLE
    }

    override fun onAttachedToScreen() {
        super.onAttachedToScreen()
        viewModel.onAttachedToScreen()
    }
}
