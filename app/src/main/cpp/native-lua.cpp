#include <jni.h>
#include <string>
#include <sstream>
#include <vector>
#include <android/log.h>

extern "C" {
#include "lua.h"
#include "lualib.h"
#include "lauxlib.h"
}

#define LOG_TAG "NativeLua54"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

struct LuaExecContext {
    JNIEnv* env;
    jobject engineObj;
    jclass engineClass;
    std::stringstream output;
};

static thread_local LuaExecContext* g_ctx = nullptr;

static void callVoidMethod(const char* name, const char* sig, ...) {
    if (!g_ctx || !g_ctx->env || !g_ctx->engineObj) return;
    va_list args;
    va_start(args, sig);
    jmethodID mid = g_ctx->env->GetMethodID(g_ctx->engineClass, name, sig);
    if (mid) {
        g_ctx->env->CallVoidMethodV(g_ctx->engineObj, mid, args);
        if (g_ctx->env->ExceptionCheck()) {
            g_ctx->env->ExceptionClear();
        }
    } else {
        LOGE("Method %s not found with signature %s", name, sig);
    }
    va_end(args);
}

// Lua: flashlight() or flashlight(bool)
static int l_flashlight(lua_State* L) {
    int on = 1;
    if (lua_gettop(L) >= 1) {
        on = lua_toboolean(L, 1);
    }
    callVoidMethod("onFlashlight", "(Z)V", (jboolean)(on != 0));
    return 0;
}

// Lua: copy(text)
static int l_copy(lua_State* L) {
    const char* text = luaL_optstring(L, 1, "");
    if (g_ctx && g_ctx->env) {
        jstring jText = g_ctx->env->NewStringUTF(text);
        callVoidMethod("onCopy", "(Ljava/lang/String;)V", jText);
        g_ctx->env->DeleteLocalRef(jText);
    }
    return 0;
}

// Lua: open_url(url)
static int l_open_url(lua_State* L) {
    const char* url = luaL_optstring(L, 1, "https://google.com");
    if (g_ctx && g_ctx->env) {
        jstring jUrl = g_ctx->env->NewStringUTF(url);
        callVoidMethod("onOpenUrl", "(Ljava/lang/String;)V", jUrl);
        g_ctx->env->DeleteLocalRef(jUrl);
    }
    return 0;
}

// Lua: open_app(package_name)
static int l_open_app(lua_State* L) {
    const char* pkg = luaL_optstring(L, 1, "");
    if (g_ctx && g_ctx->env) {
        jstring jPkg = g_ctx->env->NewStringUTF(pkg);
        callVoidMethod("onOpenApp", "(Ljava/lang/String;)V", jPkg);
        g_ctx->env->DeleteLocalRef(jPkg);
    }
    return 0;
}

// Lua: set_volume(percent)
static int l_set_volume(lua_State* L) {
    int percent = 70;
    if (lua_gettop(L) >= 1) {
        percent = (int)luaL_checkinteger(L, 1);
    }
    callVoidMethod("onSetVolume", "(I)V", (jint)percent);
    return 0;
}

// Lua: map(query)
static int l_map(lua_State* L) {
    const char* q = luaL_optstring(L, 1, "");
    if (g_ctx && g_ctx->env) {
        jstring jQ = g_ctx->env->NewStringUTF(q);
        callVoidMethod("onMap", "(Ljava/lang/String;)V", jQ);
        g_ctx->env->DeleteLocalRef(jQ);
    }
    return 0;
}

// Lua: timer(minutes_or_seconds)
static int l_timer(lua_State* L) {
    const char* timeParam = "5";
    if (lua_isstring(L, 1)) {
        timeParam = lua_tostring(L, 1);
    } else if (lua_isinteger(L, 1)) {
        static char buf[32];
        snprintf(buf, sizeof(buf), "%lld", (long long)lua_tointeger(L, 1));
        timeParam = buf;
    }
    if (g_ctx && g_ctx->env) {
        jstring jTime = g_ctx->env->NewStringUTF(timeParam);
        callVoidMethod("onTimer", "(Ljava/lang/String;)V", jTime);
        g_ctx->env->DeleteLocalRef(jTime);
    }
    return 0;
}

// Lua: message(text) or message(phone, text)
static int l_message(lua_State* L) {
    int top = lua_gettop(L);
    std::string phone = "";
    std::string text = "";
    if (top >= 2) {
        phone = luaL_optstring(L, 1, "");
        text = luaL_optstring(L, 2, "");
    } else if (top == 1) {
        text = luaL_optstring(L, 1, "");
    }
    if (g_ctx && g_ctx->env) {
        jstring jPanel = g_ctx->env->NewStringUTF(phone.c_str());
        jstring jText = g_ctx->env->NewStringUTF(text.c_str());
        callVoidMethod("onMessage", "(Ljava/lang/String;Ljava/lang/String;)V", jPanel, jText);
        g_ctx->env->DeleteLocalRef(jPanel);
        g_ctx->env->DeleteLocalRef(jText);
    }
    return 0;
}

// Lua: sound_settings()
static int l_sound_settings(lua_State* /* L */) {
    callVoidMethod("onSoundSettings", "()V");
    return 0;
}

// Lua: share(text)
static int l_share(lua_State* L) {
    const char* text = luaL_optstring(L, 1, "");
    if (g_ctx && g_ctx->env) {
        jstring jText = g_ctx->env->NewStringUTF(text);
        callVoidMethod("onShare", "(Ljava/lang/String;)V", jText);
        g_ctx->env->DeleteLocalRef(jText);
    }
    return 0;
}

// Lua: speak(text, [engine], [voice])
static int l_speak(lua_State* L) {
    const char* text = luaL_optstring(L, 1, "");
    const char* engine = luaL_optstring(L, 2, "");
    const char* voice = luaL_optstring(L, 3, "");
    if (g_ctx && g_ctx->env) {
        jstring jText = g_ctx->env->NewStringUTF(text);
        jstring jEngine = g_ctx->env->NewStringUTF(engine);
        jstring jVoice = g_ctx->env->NewStringUTF(voice);
        callVoidMethod("onSpeak", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", jText, jEngine, jVoice);
        g_ctx->env->DeleteLocalRef(jText);
        g_ctx->env->DeleteLocalRef(jEngine);
        g_ctx->env->DeleteLocalRef(jVoice);
    }
    return 0;
}

// Lua: set_tts_engine(engine)
static int l_set_tts_engine(lua_State* L) {
    const char* engine = luaL_optstring(L, 1, "PIPER");
    if (g_ctx && g_ctx->env) {
        jstring jEngine = g_ctx->env->NewStringUTF(engine);
        callVoidMethod("onSetTtsEngine", "(Ljava/lang/String;)V", jEngine);
        g_ctx->env->DeleteLocalRef(jEngine);
    }
    return 0;
}

// Lua: get_hour() -> int
static int l_get_hour(lua_State* L) {
    jint hour = 0;
    if (g_ctx && g_ctx->env && g_ctx->engineObj) {
        jmethodID mid = g_ctx->env->GetMethodID(g_ctx->engineClass, "onGetHour", "()I");
        if (mid) {
            hour = g_ctx->env->CallIntMethod(g_ctx->engineObj, mid);
            if (g_ctx->env->ExceptionCheck()) {
                g_ctx->env->ExceptionClear();
            }
        }
    }
    lua_pushinteger(L, hour);
    return 1;
}

// Lua: print(...)
static int l_print(lua_State* L) {
    int n = lua_gettop(L);
    std::string line;
    for (int i = 1; i <= n; i++) {
        if (i > 1) line += "\t";
        if (lua_isstring(L, i)) {
            line += lua_tostring(L, i);
        } else if (lua_isboolean(L, i)) {
            line += lua_toboolean(L, i) ? "true" : "false";
        } else if (lua_isnil(L, i)) {
            line += "nil";
        } else {
            line += lua_typename(L, lua_type(L, i));
        }
    }
    if (g_ctx) {
        if (!g_ctx->output.str().empty()) {
            g_ctx->output << "\n";
        }
        g_ctx->output << line;
        if (g_ctx->env) {
            jstring jLine = g_ctx->env->NewStringUTF(line.c_str());
            callVoidMethod("onPrint", "(Ljava/lang/String;)V", jLine);
            g_ctx->env->DeleteLocalRef(jLine);
        }
    }
    return 0;
}

// Handler de errores usando Lua Debug Library (debug.traceback)
static int l_error_handler(lua_State* L) {
    const char* msg = lua_tostring(L, 1);
    if (msg) {
        luaL_traceback(L, L, msg, 1);
    } else if (!lua_isnoneornil(L, 1)) {
        if (!luaL_callmeta(L, 1, "__tostring")) {
            lua_pushliteral(L, "(error object is not a string)");
        }
    }
    return 1;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_executor_LuaShortcutEngine_nativeExecuteScript(
    JNIEnv* env,
    jobject thiz,
    jstring jScript
) {
    const char* scriptChars = env->GetStringUTFChars(jScript, nullptr);
    if (!scriptChars) {
        return env->NewStringUTF("Error: No se pudo leer el script");
    }

    lua_State* L = luaL_newstate();
    if (!L) {
        env->ReleaseStringUTFChars(jScript, scriptChars);
        return env->NewStringUTF("Error: Fallo al inicializar el estado de Lua 5.4.7");
    }

    // Open standard Lua 5.4.7 libraries (incluye ldebug.c y ldblib.c / debug library)
    luaL_openlibs(L);

    // Set execution context
    LuaExecContext ctx;
    ctx.env = env;
    ctx.engineObj = thiz;
    ctx.engineClass = env->GetObjectClass(thiz);
    g_ctx = &ctx;

    // Register hardware & utility bindings
    lua_register(L, "flashlight", l_flashlight);
    lua_register(L, "copy", l_copy);
    lua_register(L, "open_url", l_open_url);
    lua_register(L, "open_app", l_open_app);
    lua_register(L, "set_volume", l_set_volume);
    lua_register(L, "map", l_map);
    lua_register(L, "timer", l_timer);
    lua_register(L, "message", l_message);
    lua_register(L, "sound_settings", l_sound_settings);
    lua_register(L, "share", l_share);
    lua_register(L, "speak", l_speak);
    lua_register(L, "set_tts_engine", l_set_tts_engine);
    lua_register(L, "get_hour", l_get_hour);
    lua_register(L, "print", l_print);

    // Expose version globals
    lua_pushstring(L, LUA_RELEASE);
    lua_setglobal(L, "_LUA_VERSION");

    // Empujar la función error_handler (usando Lua Debug Library para traceback)
    lua_pushcfunction(L, l_error_handler);
    int errHandlerIdx = lua_gettop(L);

    // Cargar el script
    int loadStatus = luaL_loadstring(L, scriptChars);
    env->ReleaseStringUTFChars(jScript, scriptChars);

    std::string resultStr;
    if (loadStatus != LUA_OK) {
        const char* err = lua_tostring(L, -1);
        resultStr = std::string("Error de sintaxis Lua 5.4.7: ") + (err ? err : "Error de sintaxis");
        lua_pop(L, 2); // pop error y error handler
    } else {
        // Ejecutar protegido con traceback de Lua Debug Library
        int execStatus = lua_pcall(L, 0, LUA_MULTRET, errHandlerIdx);
        if (execStatus != LUA_OK) {
            const char* err = lua_tostring(L, -1);
            resultStr = std::string("Error Lua [Debug Traceback]:\n") + (err ? err : "Error de ejecución");
            lua_pop(L, 1);
        } else {
            std::string printed = ctx.output.str();
            if (!printed.empty()) {
                resultStr = printed;
            } else if (lua_gettop(L) > errHandlerIdx && lua_isstring(L, -1)) {
                resultStr = lua_tostring(L, -1);
            } else {
                resultStr = "Script Lua 5.4.7 ejecutado con éxito";
            }
        }
        lua_remove(L, errHandlerIdx); // remover handler de error
    }

    lua_close(L);
    g_ctx = nullptr;

    return env->NewStringUTF(resultStr.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_executor_LuaShortcutEngine_nativeGetVersion(
    JNIEnv* env,
    jobject /* thiz */
) {
    return env->NewStringUTF(LUA_RELEASE);
}
