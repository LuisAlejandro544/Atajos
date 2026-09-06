/*
 * Copyright (C) 2012-2015 Reece H. Dunn
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.reecedunn.espeak;

import android.util.Log;

public class SpeechSynthesis {
    private static final String TAG = "SpeechSynthesis";

    public interface SynthCallback {
        void onSynthDataReady(byte[] audioData);
        void onSynthDataComplete();
    }

    static {
        try {
            System.loadLibrary("ttsespeak");
            nativeClassInit();
        } catch (Throwable t) {
            Log.e(TAG, "Error loading libttsespeak: " + t.getMessage());
        }
    }

    private final SynthCallback mCallback;
    private int mSampleRate = 0;
    private boolean mInitialized = false;

    public SpeechSynthesis(String dataPath, SynthCallback callback) {
        mCallback = callback;
        try {
            mSampleRate = nativeCreate(dataPath);
            if (mSampleRate > 0) {
                mInitialized = true;
                Log.i(TAG, "eSpeak-NG inicializado con sample rate = " + mSampleRate);
            } else {
                Log.e(TAG, "eSpeak-NG nativeCreate returned 0 for path: " + dataPath);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Excepción en nativeCreate: " + t.getMessage());
        }
    }

    public boolean isInitialized() {
        return mInitialized;
    }

    public int getSampleRate() {
        return mSampleRate > 0 ? mSampleRate : 22050;
    }

    public static String getVersion() {
        try {
            return nativeGetVersion();
        } catch (Throwable t) {
            return "1.52.0";
        }
    }

    public boolean setVoiceByName(String name) {
        try {
            return nativeSetVoiceByName(name);
        } catch (Throwable t) {
            Log.e(TAG, "Error setVoiceByName: " + t.getMessage());
            return false;
        }
    }

    public boolean setParameter(int param, int value) {
        try {
            return nativeSetParameter(param, value);
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean synthesize(String text) {
        try {
            return nativeSynthesize(text, false);
        } catch (Throwable t) {
            Log.e(TAG, "Error nativeSynthesize: " + t.getMessage());
            return false;
        }
    }

    public boolean stop() {
        try {
            return nativeStop();
        } catch (Throwable t) {
            return false;
        }
    }

    // Callbacks llamados desde JNI en C
    private void nativeSynthCallback(byte[] audioData) {
        if (mCallback == null) return;
        if (audioData == null) {
            mCallback.onSynthDataComplete();
        } else {
            mCallback.onSynthDataReady(audioData);
        }
    }

    private void nativeSynthWordCallback(int textPosition, int textLength, int markerInFrames) {
        // Marcador de sincronización de palabras
    }

    // Métodos nativos JNI
    private static native boolean nativeClassInit();
    private native int nativeCreate(String path);
    private static native String nativeGetVersion();
    private native String[] nativeGetAvailableVoices();
    private native boolean nativeSetVoiceByName(String name);
    private native boolean nativeSetVoiceByProperties(String language, int gender, int age);
    private native boolean nativeSetParameter(int parameter, int value);
    private native int nativeGetParameter(int parameter, int current);
    private native boolean nativeSetPunctuationCharacters(String characters);
    private native boolean nativeSynthesize(String text, boolean isSsml);
    private native boolean nativeStop();
}
