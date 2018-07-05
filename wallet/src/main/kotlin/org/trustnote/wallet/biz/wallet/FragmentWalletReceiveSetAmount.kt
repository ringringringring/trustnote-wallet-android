package org.trustnote.wallet.biz.wallet

import android.view.View
import android.widget.Button
import android.widget.EditText
import org.trustnote.wallet.R
import org.trustnote.wallet.biz.FragmentPageBase
import org.trustnote.wallet.util.Utils
import org.trustnote.wallet.widget.PageHeader

class FragmentWalletReceiveSetAmount : FragmentPageBase() {

    private lateinit var inputAmount: EditText
    private lateinit var btnConfirm: Button
    lateinit var pageHeader: PageHeader
    lateinit var doneAction: (Long) -> Unit

    override fun getLayoutId(): Int {
        return R.layout.l_dialog_wallet_receive_set_amount
    }

    override fun initFragment(view: View) {

        super.initFragment(view)

        mRootView.findViewById<PageHeader>(R.id.page_header).closeAction = {
            onBackPressed()
        }

        inputAmount = mRootView.findViewById(R.id.receive_amount_input)
        btnConfirm = mRootView.findViewById(R.id.receive_set_amount_btn)

        btnConfirm.setOnClickListener {

            hideSystemSoftKeyboard()
            getMyActivity().onBackPressed()
            doneAction.invoke(Utils.mnToNotes(inputAmount.text.toString()))

        }

        pageHeader = findViewById(R.id.page_header)
        pageHeader.hideCloseBtn()
        fixOutmostLayoutPaddingBottom(R.dimen.line_gap_70)

        showSystemSoftKeyboard(inputAmount, activity)

    }


}

