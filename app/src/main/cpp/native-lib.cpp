#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "FCM_NATIVE"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT void JNICALL
Java_com_simple_notification_testing_FcmTestFragment_sendNotificationNative(
        JNIEnv* env, jobject thiz, jstring token, jstring message, jstring auth_id_token, jobject result_listener) {

    // URL được giữ trực tiếp trong file C để đơn giản hóa
    std::string url = "https://us-central1-detect-translate-8.cloudfunctions.net/sendPushNotification";

    LOGD("URL: %s", url.c_str());

    // 1. Tạo JSON Body
    jclass json_cls = env->FindClass("org/json/JSONObject");
    jmethodID json_init = env->GetMethodID(json_cls, "<init>", "()V");
    jobject json_obj = env->NewObject(json_cls, json_init);
    jmethodID put_mid = env->GetMethodID(json_cls, "put", "(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;");

    env->CallObjectMethod(json_obj, put_mid, env->NewStringUTF("token"), token);
    env->CallObjectMethod(json_obj, put_mid, env->NewStringUTF("title"), env->NewStringUTF("Native Secure 🛡️"));
    env->CallObjectMethod(json_obj, put_mid, env->NewStringUTF("body"), message);

    jstring json_str = (jstring)env->CallObjectMethod(json_obj, env->GetMethodID(json_cls, "toString", "()Ljava/lang/String;"));

    // 2. OkHttp: RequestBody.create
    jclass media_type_cls = env->FindClass("okhttp3/MediaType");
    jmethodID media_parse = env->GetStaticMethodID(media_type_cls, "parse", "(Ljava/lang/String;)Lokhttp3/MediaType;");
    jobject media_type = env->CallStaticObjectMethod(media_type_cls, media_parse, env->NewStringUTF("application/json; charset=utf-8"));

    jclass body_cls = env->FindClass("okhttp3/RequestBody");
    jmethodID body_create = env->GetStaticMethodID(body_cls, "create", "(Lokhttp3/MediaType;Ljava/lang/String;)Lokhttp3/RequestBody;");
    jobject body = env->CallStaticObjectMethod(body_cls, body_create, media_type, json_str);

    // 3. OkHttp: Request.Builder
    jclass builder_cls = env->FindClass("okhttp3/Request$Builder");
    jobject builder = env->NewObject(builder_cls, env->GetMethodID(builder_cls, "<init>", "()V"));
    env->CallObjectMethod(builder, env->GetMethodID(builder_cls, "url", "(Ljava/lang/String;)Lokhttp3/Request$Builder;"), env->NewStringUTF(url.c_str()));

    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();
        env->ExceptionClear();
        LOGD("Error: Invalid URL format");
        return;
    }

    env->CallObjectMethod(builder, env->GetMethodID(builder_cls, "post", "(Lokhttp3/RequestBody;)Lokhttp3/Request$Builder;"), body);

    const char *token_ptr = env->GetStringUTFChars(auth_id_token, NULL);
    std::string auth_val = "Bearer " + std::string(token_ptr);
    env->ReleaseStringUTFChars(auth_id_token, token_ptr);
    env->CallObjectMethod(builder, env->GetMethodID(builder_cls, "addHeader", "(Ljava/lang/String;Ljava/lang/String;)Lokhttp3/Request$Builder;"),
                          env->NewStringUTF("Authorization"), env->NewStringUTF(auth_val.c_str()));

    jobject request = env->CallObjectMethod(builder, env->GetMethodID(builder_cls, "build", "()Lokhttp3/Request;"));

    // 4. Khởi tạo CallbackBridge
    jclass bridge_cls = env->FindClass("com/simple/notification/testing/FcmTestFragment$CallbackBridge");
    jmethodID bridge_init = env->GetMethodID(bridge_cls, "<init>", "(Lcom/simple/notification/testing/FcmTestFragment$OnPushResult;)V");
    jobject bridge_obj = env->NewObject(bridge_cls, bridge_init, result_listener);

    // 5. OkHttpClient & enqueue
    jclass client_cls = env->FindClass("okhttp3/OkHttpClient");
    jobject client = env->NewObject(client_cls, env->GetMethodID(client_cls, "<init>", "()V"));
    jobject call = env->CallObjectMethod(client, env->GetMethodID(client_cls, "newCall", "(Lokhttp3/Request;)Lokhttp3/Call;"), request);

    jclass call_cls = env->FindClass("okhttp3/Call");
    env->CallVoidMethod(call, env->GetMethodID(call_cls, "enqueue", "(Lokhttp3/Callback;)V"), bridge_obj);
}
