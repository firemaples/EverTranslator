package tw.firemaples.onscreenocr.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.util.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class SignatureUtil {
    companion object {
        private const val SIGNATURE_SHA = "gK7G8ZtYKeXYMsWzOZ0wP+rTP50=\n"
        private const val SIGNATURE_HASH = 116585913

        @Throws(PackageManager.NameNotFoundException::class, NoSuchAlgorithmException::class)
        fun validateSignature(context: Context): Boolean {
            val currentSignatureSHA = getCurrentSignatureSHA(context) ?: return false

            return currentSignatureSHA.replace("\n", "") ==
                    SIGNATURE_SHA.replace("\n", "")
        }

        @Throws(NoSuchAlgorithmException::class, PackageManager.NameNotFoundException::class)
        fun getCurrentSignatureSHA(context: Context): String? {
            val signature = getCurrentSignature(context) ?: return null
            val md = MessageDigest.getInstance("SHA").apply {
                update(signature.toByteArray())
            }

            return Base64.encodeToString(md.digest(), Base64.DEFAULT)
        }

        @Throws(PackageManager.NameNotFoundException::class)
        fun getCurrentSignatureHashCode(context: Context): Int =
            getCurrentSignature(context)?.hashCode() ?: -1

        @Throws(PackageManager.NameNotFoundException::class)
        fun getCurrentSignature(context: Context): Signature? {
            val signatures = context.packageManager
                .getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES).signatures
            
            return if (!signatures.isNullOrEmpty()) signatures[0] else null
        }
    }
}
