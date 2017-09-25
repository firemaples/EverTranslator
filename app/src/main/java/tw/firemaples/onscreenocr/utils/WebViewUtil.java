/*
 * Copyright 2016-2017 Louis Chen [firemaples@gmail.com].
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tw.firemaples.onscreenocr.utils;

import android.support.annotation.NonNull;

/**
 * Created by firemaples on 30/11/2016.
 */

public class WebViewUtil {
    private final static String FORMAT_TEXT = "{TEXT}";
    private final static String FORMAT_SOURCE_LANG = "{SL}";
    private final static String FORMAT_TARGET_LANG = "{TL}";

    public enum Type {
        Google("https://translate.google.com/?sl=auto&tl=" + FORMAT_TARGET_LANG + "&q=" + FORMAT_TEXT);

        private final String urlFormat;

        Type(String urlFormat) {
            this.urlFormat = urlFormat;
        }

        public String getFormattedUrl(@NonNull String text, @NonNull String targetLanguage) {
            return getFormattedUrl(text, "", targetLanguage);
        }

        public String getFormattedUrl(@NonNull String text, @NonNull String sourceLanguage, @NonNull String targetLanguage) {
            return urlFormat.replace(FORMAT_TEXT, text).replace(FORMAT_SOURCE_LANG, sourceLanguage).replace(FORMAT_TARGET_LANG, targetLanguage);
        }
    }
}
