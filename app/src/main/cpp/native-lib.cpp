#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include <thread>

#define LOG_TAG "FCM_NATIVE"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

JavaVM* g_vm = nullptr;

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    g_vm = vm;
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

    jclass client_cls = env->FindClass("okhttp3/OkHttpClient");
    jclass call_cls = env->FindClass("okhttp3/Call");
    jclass response_cls = env->FindClass("okhttp3/Response");
    jclass listener_cls = env->FindClass("com/simple/notification/testing/FcmTestFragment$OnPushResult");

    jmethodID new_call_mid = env->GetMethodID(client_cls, "newCall", "(Lokhttp3/Request;)Lokhttp3/Call;");
    jmethodID execute_mid = env->GetMethodID(call_cls, "execute", "()Lokhttp3/Response;");
    jmethodID is_success_mid = env->GetMethodID(response_cls, "isSuccessful", "()Z");
    jmethodID code_mid = env->GetMethodID(response_cls, "code", "()I");
    jmethodID on_result_mid = env->GetMethodID(listener_cls, "onResult", "(ZLjava/lang/String;)V");

    jobject call = env->CallObjectMethod(args->client, new_call_mid, args->request);
    jobject response = env->CallObjectMethod(call, execute_mid);

    if (env->ExceptionCheck()) {
        env->ExceptionClear();
        env->CallVoidMethod(args->listener, on_result_mid, JNI_FALSE, env->NewStringUTF("Lỗi kết nối mạng (Native)"));
    } else {
        jboolean success = env->CallBooleanMethod(response, is_success_mid);
        if (success) {
            env->CallVoidMethod(args->listener, on_result_mid, JNI_TRUE, env->NewStringUTF("Đã gửi thông báo thành công (Native Secure)!"));
        } else {
            int code = env->CallIntMethod(response, code_mid);
            std::string msg = "Lỗi API: " + std::to_string(code);
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
Java_com_simple_notification_testing_FcmTestFragment_sendNotificationNative(
        JNIEnv* env, jobject thiz, jstring token, jstring message, jstring auth_id_token, jobject result_listener) {

    std::string url = "https://us-central1-detect-translate-8.cloudfunctions.net/sendPushNotification";

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
    env->CallObjectMethod(builder, env->GetMethodID(builder_cls, "post", "(Lokhttp3/RequestBody;)Lokhttp3/Request$Builder;"), body);
    
    const char *token_ptr = env->GetStringUTFChars(auth_id_token, NULL);
    std::string auth_val = "Bearer " + std::string(token_ptr);
    env->ReleaseStringUTFChars(auth_id_token, token_ptr);
    env->CallObjectMethod(builder, env->GetMethodID(builder_cls, "addHeader", "(Ljava/lang/String;Ljava/lang/String;)Lokhttp3/Request$Builder;"), 
                          env->NewStringUTF("Authorization"), env->NewStringUTF(auth_val.c_str()));

    jobject request = env->CallObjectMethod(builder, env->GetMethodID(builder_cls, "build", "()Lokhttp3/Request;"));

    // 4. OkHttpClient
    jclass client_cls = env->FindClass("okhttp3/OkHttpClient");
    jobject client = env->NewObject(client_cls, env->GetMethodID(client_cls, "<init>", "()V"));

    // 5. Chạy mạng trong thread riêng để không dùng CallbackBridge
    NetworkArgs* args = new NetworkArgs();
    args->listener = env->NewGlobalRef(result_listener);
    args->request = env->NewGlobalRef(request);
    args->client = env->NewGlobalRef(client);

    std::thread t(runNetworkTask, args);
    t.detach();
}
