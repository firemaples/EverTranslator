package tw.firemaples.onscreenocr.utils

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.BitmapFactory
import java.lang.ref.SoftReference
import java.util.Collections

fun BitmapFactory.Options.reuseBitmap() {
    inMutable = true
    BitmapCache.getBitmapFromReusableSet(this)?.also { inBitmap ->
        this.inBitmap = inBitmap
    }
}

fun Bitmap.setReusable() = BitmapCache.addToReusableSet(this)

object BitmapCache {
    private val reusableBitmaps: MutableSet<SoftReference<Bitmap>> =
        Collections.synchronizedSet(HashSet<SoftReference<Bitmap>>())

    fun addToReusableSet(bitmap: Bitmap) {
        reusableBitmaps.add(SoftReference(bitmap))
    }

    fun getBitmapFromReusableSet(options: BitmapFactory.Options): Bitmap? {
        val width = options.outWidth / options.inSampleSize
        val height = options.outHeight / options.inSampleSize
        return getBitmapFromReusableSet(
            width = width,
            height = height,
            config = null,
            fixedSize = false,
        )
    }

    fun getReusableBitmapOrCreate(
        width: Int, height: Int, config: Config, fixedSize: Boolean,
    ): Bitmap = getBitmapFromReusableSet(
        width = width,
        height = height,
        config = config,
        fixedSize = fixedSize,
    ) ?: Bitmap.createBitmap(width, height, config)

    fun getBitmapFromReusableSet(
        width: Int, height: Int, config: Config?, fixedSize: Boolean,
    ): Bitmap? {
        reusableBitmaps
            .takeIf { it.isNotEmpty() }
            ?.also { reusableBitmaps ->
                synchronized(reusableBitmaps) {
                    val iterator = reusableBitmaps.iterator()
                    while (iterator.hasNext()) {
                        iterator.next().get()?.also { item ->
                            if (item.isMutable) {
                                if (item.canUseForInBitmap(
                                        width = width,
                                        height = height,
                                        config = config,
                                        fixedSize = fixedSize,
                                    )
                                ) {
                                    iterator.remove()
                                    return item
                                }
                            } else {
                                // remove recycled item
                                iterator.remove()
                            }
                        }
                    }
                }
            }

        return null
    }

    private fun Bitmap.canUseForInBitmap(
        width: Int, height: Int, config: Config?, fixedSize: Boolean,
    ): Boolean {
        if (fixedSize && this.width != width && this.height != height) {
            return false
        }

        val targetConfig = config ?: this.config
        val byteCount = width * height * targetConfig.getBytesPerPixel()
        return byteCount <= this.allocationByteCount
    }

    private fun Config.getBytesPerPixel(): Int =
        when (this) {
            Config.ARGB_8888 -> 4
            Config.RGB_565, Config.ARGB_4444 -> 2
            Config.ALPHA_8 -> 1
            else -> 1
        }
}
