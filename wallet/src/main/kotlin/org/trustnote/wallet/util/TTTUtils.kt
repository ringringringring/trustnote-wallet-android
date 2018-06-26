package org.trustnote.wallet.util

import android.widget.ImageView
import android.widget.TextView
import com.google.gson.JsonObject
import org.trustnote.wallet.R
import org.trustnote.wallet.biz.TTT
import org.trustnote.wallet.biz.wallet.Credential
import org.trustnote.wallet.biz.wallet.PaymentInfo
import org.trustnote.wallet.biz.js.JSApi
import org.trustnote.wallet.biz.wallet.WalletManager

object TTTUtils {

    fun toAddressQRText(address: String, amount: Long): String {
        if (amount == 0L) {
            return "${TTT.KEY_TTT_QR_TAG}:$address"
        } else {
            return "${TTT.KEY_TTT_QR_TAG}:$address?amount=$amount"
        }
    }

    fun setupAddressQRCode(address: String, amount: Long, qrImageView: ImageView) {
        val qrForAddress = toAddressQRText(address, amount)
        setupQRCode(qrForAddress, qrImageView)
    }

    fun setupQRCode(qrcode: String, qrImageView: ImageView) {
        val qrWidth = org.trustnote.wallet.TApp.context.resources.getDimension(R.dimen.qr_width).toInt()
        val qrBitmap = AndroidUtils.encodeStrAsQrBitmap(qrcode, qrWidth)
        qrImageView.setImageBitmap(qrBitmap)
    }

    fun parsePaymentFromQRCode(qrCode: String): PaymentInfo {
        val res = PaymentInfo()
        if (qrCode.isNotEmpty() && qrCode.length > 4) {
            val hasAmount = qrCode.contains("?")
            res.receiverAddress = if (hasAmount)
                qrCode.substring(4, qrCode.indexOf('?'))
            else qrCode.substring(4)

            if (hasAmount) {
                val key = "amount"
                try {
                    res.amount = qrCode.substring(qrCode.indexOf(key) + key.length + 1).toLong()
                } catch (e: Exception) {
                    Utils.logW(e.localizedMessage)
                }
            }
        }
        return res
    }

    fun formatMN(view: TextView, amount: Long) {
        view.setText(formatMN(amount))
    }

    fun formatMN(amount: Long): String {
        return "${amount / TTT.w_coinunitValue}.${(amount % TTT.w_coinunitValue).toString().padStart(6, '0').substring(0, 4)}"
    }

    fun isValidAddress(address: String): Boolean {
        //TODO:
        return address.isNotEmpty()
    }

    fun isValidAmount(amount: String, balance: Long): Boolean {
        return amount.isNotEmpty() && amount.toFloat() * TTT.w_coinunitValue <= balance
    }

    fun parseTTTAmount(amountStr: String): Long {
        return (amountStr.toFloat() * TTT.w_coinunitValue).toLong()
    }

    fun formatWalletId(walletId: String): String {
        if (walletId.isEmpty()) {
            return "UNKNOWN"
        }
        if (walletId.length <= 16) {
            return walletId.toUpperCase()
        }
        return """${walletId.substring(0, 8).toUpperCase()}...${walletId.takeLast(8).toUpperCase()}"""
    }

    //Step1: TTT:{"type": "c1","name": "TTT","pub":"xpub6CiT96vM5krNhwFA4ro5nKJ6nq9WykFmAsP18jC1Aa3URb69rvUHw6uvU51MQPkMZQ6BLiC5C1E3Zbsm7Xob3FFhNHJkN3v9xuxfqFFKPP5","n": 0,"v": 1234}
    //    TTT: {
    //        "type": "h1",
    //        "id": "LYnW1wl8qHyHyWjoV2CYOlYhUvE3Gj1jh5tUEFzoMn0=",
    //        "v": 1234
    //    }

    //Setp 3:
    //    TTT: {
    //        "type": "c2",
    //        "addr": "0NEYV3ZCRAJYGJDS5UNN4EOZGNVZJXOLI",
    //        "v": 1234
    //    }
    fun genColdScancodeStep3(myDeviceAddress: String, checkCode: Int): String {
        return """${TTT.KEY_TTT_QR_TAG}:{"type":"c2","addr":"$myDeviceAddress","v":$checkCode}"""
    }

    fun genColdScancodeStep2(jsonObj: JsonObject): String {

        val walletPubKey = jsonObj.getAsJsonPrimitive("pub")?.asString
        val checkCode = jsonObj.getAsJsonPrimitive("v")?.asString
        val walletId = JSApi().walletIDSync(walletPubKey ?: "")

        return """${TTT.KEY_TTT_QR_TAG}:{"type":"h1","id": "$walletId","v":${checkCode ?: ""}}"""
    }

    fun genColdScancodeStep1(credential: Credential): String {
        return """${TTT.KEY_TTT_QR_TAG}:{"type":"c1","name":"${credential.walletName}","pub":"${credential.xPubKey}","n": ${credential.account},"v":${randomCheckCode()}}"""
    }

    //    TTT: {
    //        "type": "h2",
    //        "sign": "Ujn29JQOD0yqErXL+fYofMTIiE2aX7VINhnEL2q62ww=",
    //        "path": "m/44'/0'/2'/1/32",
    //        "addr": "YDKTOQ7VCBQ336VGH3S5RLIWRRAUTB5O",
    //        "amount": 1000,
    //        "v": 1234
    //    }
    fun getQrCodeForColdToSign(sign: String, path: String, addr: String, amount: Long, checkCode: Int): String {
        return """${TTT.KEY_TTT_QR_TAG}:{"type":"h2","sign":"$sign","path":"$path","addr":"$addr","amount":$amount,"v":$checkCode}"""
    }

    //    TTT: {
    //        "type": "c3",
    //        "sign": "cMKJdsCjSCg1iP9VLq6QFDlv3S6tRhKaXcmJhGTMWtxlKDg6tYn7Q7LqUamjRz7JMbSmAZCP/K1LM1vA1p+/wQ==",
    //        "v": 1234
    //    }
    fun getQrCodeForObserver(signature: String, checkCode: Int): String {
        return """${TTT.KEY_TTT_QR_TAG}:{"type":"c3","sign":"$signature","v":$checkCode}"""
    }

    fun checkAndParseSignature(signatureJsonStr: String, checkCode: Int): String? {
        val json = scanStringToJsonObject(signatureJsonStr)
        val checkCodeFromSignature = json.get("v")?.asInt

        if (checkCodeFromSignature != null && checkCode == checkCodeFromSignature) {
            return json.get("sign")?.asString
        }
        return ""
    }

    fun randomCheckCode(): Int {
        return Utils.random.nextInt(8999) + 1000
    }

    val unitAuthentifierPlaceHolder = Utils.genJsonObject("r", TTT.PLACEHOLDER_SIG)

    fun scanStringToJsonObject(str: String): JsonObject {
        //TODO: make sure the protocol is TTT.
        if (str.isBlank() || str.length < 4) {
            return Utils.emptyJsonObject
        }

        try {
            return Utils.getGson().fromJson(str.substring(4), JsonObject::class.java)
        } catch (ex: Exception) {
            Utils.logW(ex.localizedMessage)
        }
        return Utils.emptyJsonObject
    }

    fun formatIconText(s: String): String {
        return if (s.isNotEmpty()) {
            s.substring(0, 1).toUpperCase()
        } else {
            ""
        }
    }

    fun genDefinitions(addressPubkey: String): String {
        return """["sig",{"pubkey":"$addressPubkey"}]"""
    }

    val tttReceiverAddressPattern = Regex("""TTT:.{32}(\?amount=)?""")
    val tttMyPairIdPattern = Regex("""TTT:(.{16,100})@(.{16,100})#(.{6,20})""")
    val tttCodeQrCodeForStep1 = Regex("""TTT:.\{"type":"c1".*\}""")
    fun parseQrCodeType(qrCode: String): SCAN_RESULT_TYPE {
        if (tttReceiverAddressPattern.matches(qrCode)) {
            return SCAN_RESULT_TYPE.MN_TRANSFER
        }
        if (tttMyPairIdPattern.matches(qrCode)) {
            return SCAN_RESULT_TYPE.TTT_PAIRID
        }
        if (tttCodeQrCodeForStep1.matches(qrCode)) {
            return SCAN_RESULT_TYPE.COLD_WALLET
        }
        return SCAN_RESULT_TYPE.UNKNOWN
    }

    fun getDefaultHubAddressBySeed(hubIndexForPairId: Int): String {
        val hubArray = TTT.hubArray
        val hubIndex = hubIndexForPairId % hubArray.size
        return hubArray[hubIndex]
    }



}

enum class SCAN_RESULT_TYPE {
    UNKNOWN,
    MN_TRANSFER,
    TTT_PAIRID,
    COLD_WALLET
}
