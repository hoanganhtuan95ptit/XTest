#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include <thread>

#define LOG_TAG "FCM_NATIVE"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

JavaVM* g_vm = nullptr;

// Global class references to avoid ClassNotFoundException in native threads
jclass g_client_cls = nullptr;
jclass g_request_cls = nullptr;
jclass g_builder_cls = nullptr;
jclass g_body_cls = nullptr;
jclass g_media_type_cls = nullptr;
jclass g_response_cls = nullptr;
jclass g_call_cls = nullptr;
jclass g_json_cls = nullptr;
jclass g_listener_cls = nullptr;

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    g_vm = vm;
    JNIEnv* env;
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) return JNI_ERR;

    // Cache all required classes on JNI_OnLoad (Main Thread context)
    g_client_cls = (jclass)env->NewGlobalRef(env->FindClass("okhttp3/OkHttpClient"));
    g_request_cls = (jclass)env->NewGlobalRef(env->FindClass("okhttp3/Request"));
    g_builder_cls = (jclass)env->NewGlobalRef(env->FindClass("okhttp3/Request$Builder"));
    g_body_cls = (jclass)env->NewGlobalRef(env->FindClass("okhttp3/RequestBody"));
    g_media_type_cls = (jclass)env->NewGlobalRef(env->FindClass("okhttp3/MediaType"));
    g_response_cls = (jclass)env->NewGlobalRef(env->FindClass("okhttp3/Response"));
    g_call_cls = (jclass)env->NewGlobalRef(env->FindClass("okhttp3/Call"));
    g_json_cls = (jclass)env->NewGlobalRef(env->FindClass("org/json/JSONObject"));
    g_listener_cls = (jclass)env->NewGlobalRef(env->FindClass("com/simple/notification/testing/data/repositories/notification/NotificationRepository$OnPushResult"));

    return JNI_VERSION_1_6;
}

struct NetworkArgs {
    jobject listener;
    jobject request;
    jobject client;
};

void runNetworkTask(NetworkArgs* args) {
    JNIEnv* env;
    if (g_vm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
        delete args;
        return;
    }

    jmethodID new_call_mid = env->GetMethodID(g_client_cls, "newCall", "(Lokhttp3/Request;)Lokhttp3/Call;");
    jmethodID execute_mid = env->GetMethodID(g_call_cls, "execute", "()Lokhttp3/Response;");
    jmethodID is_success_mid = env->GetMethodID(g_response_cls, "isSuccessful", "()Z");
    jmethodID code_mid = env->GetMethodID(g_response_cls, "code", "()I");
    jmethodID on_result_mid = env->GetMethodID(g_listener_cls, "onResult", "(ZLjava/lang/String;)V");

    jobject call = env->CallObjectMethod(args->client, new_call_mid, args->request);
    jobject response = env->CallObjectMethod(call, execute_mid);

    if (env->ExceptionCheck()) {
        env->ExceptionClear();
        env->CallVoidMethod(args->listener, on_result_mid, JNI_FALSE, env->NewStringUTF("Network connection error (Native Sync)"));
    } else {
        jboolean success = env->CallBooleanMethod(response, is_success_mid);
        if (success) {
            env->CallVoidMethod(args->listener, on_result_mid, JNI_TRUE, env->NewStringUTF("Sent successfully via Native!"));
        } else {
            int code = env->CallIntMethod(response, code_mid);
            std::string msg = "API Error: " + std::to_string(code);
            env->CallVoidMethod(args->listener, on_result_mid, JNI_FALSE, env->NewStringUTF(msg.c_str()));
        }
    }

    env->DeleteGlobalRef(args->listener);
    env->DeleteGlobalRef(args->request);
    env->DeleteGlobalRef(args->client);
    delete args;

    g_vm->DetachCurrentThread();
}

extern "C" JNIEXPORT void JNICALL
Java_com_simple_notification_testing_data_repositories_notification_NotificationRepository_sendPushNotificationNative(
        JNIEnv* env, jobject thiz, jstring auth_id_token, jstring target_token, jstring message, jobject result_listener) {

    std::string url = "https://us-central1-detect-translate-8.cloudfunctions.net/sendPushNotification";

    // 1. Create JSON Body
    jmethodID json_init = env->GetMethodID(g_json_cls, "<init>", "()V");
    jobject json_obj = env->NewObject(g_json_cls, json_init);
    jmethodID put_mid = env->GetMethodID(g_json_cls, "put", "(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;");

    env->CallObjectMethod(json_obj, put_mid, env->NewStringUTF("token"), target_token);
    env->CallObjectMethod(json_obj, put_mid, env->NewStringUTF("title"), env->NewStringUTF("Native Secure 🛡️"));
    env->CallObjectMethod(json_obj, put_mid, env->NewStringUTF("body"), message);
    jstring json_str = (jstring)env->CallObjectMethod(json_obj, env->GetMethodID(g_json_cls, "toString", "()Ljava/lang/String;"));

    // 2. OkHttp: RequestBody.create
    jmethodID media_parse = env->GetStaticMethodID(g_media_type_cls, "parse", "(Ljava/lang/String;)Lokhttp3/MediaType;");
    jobject media_type = env->CallStaticObjectMethod(g_media_type_cls, media_parse, env->NewStringUTF("application/json; charset=utf-8"));

    jmethodID body_create = env->GetStaticMethodID(g_body_cls, "create", "(Lokhttp3/MediaType;Ljava/lang/String;)Lokhttp3/RequestBody;");
    jobject body = env->CallStaticObjectMethod(g_body_cls, body_create, media_type, json_str);

    // 3. OkHttp: Request.Builder
    jobject builder = env->NewObject(g_builder_cls, env->GetMethodID(g_builder_cls, "<init>", "()V"));
    env->CallObjectMethod(builder, env->GetMethodID(g_builder_cls, "url", "(Ljava/lang/String;)Lokhttp3/Request$Builder;"), env->NewStringUTF(url.c_str()));
    env->CallObjectMethod(builder, env->GetMethodID(g_builder_cls, "post", "(Lokhttp3/RequestBody;)Lokhttp3/Request$Builder;"), body);
    
    const char *token_ptr = env->GetStringUTFChars(auth_id_token, NULL);
    std::string auth_val = "Bearer " + std::string(token_ptr);
    env->ReleaseStringUTFChars(auth_id_token, token_ptr);
    env->CallObjectMethod(builder, env->GetMethodID(g_builder_cls, "addHeader", "(Ljava/lang/String;Ljava/lang/String;)Lokhttp3/Request$Builder;"), 
                          env->NewStringUTF("Authorization"), env->NewStringUTF(auth_val.c_str()));

    jobject request = env->CallObjectMethod(builder, env->GetMethodID(g_builder_cls, "build", "()Lokhttp3/Request;"));

    // 4. OkHttpClient
    jobject client = env->NewObject(g_client_cls, env->GetMethodID(g_client_cls, "<init>", "()V"));

    // 5. Run in native thread
    NetworkArgs* args = new NetworkArgs();
    args->listener = env->NewGlobalRef(result_listener);
    args->request = env->NewGlobalRef(request);
    args->client = env->NewGlobalRef(client);

    std::thread t(runNetworkTask, args);
    t.detach();
}
