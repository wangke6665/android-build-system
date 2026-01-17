#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>

static int check_debugger() {
    FILE* fp = fopen("/proc/self/status", "r");
    if (fp) {
        char line[128];
        while (fgets(line, sizeof(line), fp)) {
            if (strncmp(line, "TracerPid:", 10) == 0) {
                int pid = atoi(line + 10);
                fclose(fp);
                return pid != 0;
            }
        }
        fclose(fp);
    }
    return 0;
}

static int check_hooks() {
    FILE* fp = fopen("/proc/self/maps", "r");
    if (fp) {
        char line[256];
        while (fgets(line, sizeof(line), fp)) {
            if (strstr(line, "frida") || strstr(line, "xposed") || 
                strstr(line, "substrate") || strstr(line, "libsubstrate")) {
                fclose(fp);
                return 1;
            }
        }
        fclose(fp);
    }
    return 0;
}

static const unsigned char k_seg1[] = {0x53, 0x6d, 0x40};
static const unsigned char k_seg2[] = {0x32, 0x30, 0x32, 0x34};
static const unsigned char k_seg3[] = {0x23, 0x54, 0x76};
static const unsigned char k_seg4[] = {0x4B, 0x65, 0x79};

static const unsigned int SEED_A = 0x5A5A5A5A;
static const unsigned int SEED_B = 0xA5A5A5A5;
static const unsigned int SEED_C = 0x12345678;

static void generate_key(char* out, int key_len) {
    int idx = 0;
    for (int i = 0; i < 3 && idx < key_len; i++, idx++) {
        out[idx] = k_seg1[i];
    }
    for (int i = 0; i < 4 && idx < key_len; i++, idx++) {
        out[idx] = k_seg2[i];
    }
    for (int i = 0; i < 3 && idx < key_len; i++, idx++) {
        out[idx] = k_seg3[i];
    }
    while (idx < key_len) {
        unsigned char byte = ((SEED_A >> (idx % 4)) & 0xFF) ^ 
                            ((SEED_B >> ((idx + 1) % 4)) & 0xFF) ^ 
                            ((SEED_C >> ((idx + 2) % 4)) & 0xFF);
        out[idx] = byte ^ 0x42;
        idx++;
    }
    out[key_len] = '\0';
}

static void xor_layer(unsigned char* data, int len, const char* key) {
    int key_len = strlen(key);
    for (int i = 0; i < len; i++) {
        data[i] ^= key[i % key_len];
    }
}

static void position_unscramble(unsigned char* data, int len) {
    for (int i = 0; i < len / 2; i++) {
        int j = len - 1 - i;
        unsigned char temp_i = data[i];
        unsigned char temp_j = data[j];
        data[i] = temp_j ^ 0x55;
        data[j] = temp_i ^ 0xAA;
    }
}

static void byte_unrotate(unsigned char* data, int len, int shift) {
    for (int i = 0; i < len; i++) {
        data[i] ^= (i & 0xFF);
        data[i] = ((data[i] >> shift) | (data[i] << (8 - shift)));
    }
}

static char* multi_layer_decrypt(const unsigned char* encrypted, int len, const char* key) {
    unsigned char* data = (unsigned char*)malloc(len);
    memcpy(data, encrypted, len);
    byte_unrotate(data, len, 3);
    position_unscramble(data, len);
    xor_layer(data, len, key);
    char* result = (char*)malloc(len + 1);
    memcpy(result, data, len);
    result[len] = '\0';
    free(data);
    return result;
}

static const unsigned char ENC_COS_PART1[] = {
    0xcc, 0xf7, 0x8c, 0x0c, 0xda, 0x83, 0x83, 0xb0, 0x0d, 0x6e,
    0xee, 0xe4, 0xdb, 0xfb, 0x68, 0x00, 0x24, 0x44, 0x77, 0x67
};
static const unsigned char ENC_COS_PART2[] = {
    0xb1, 0x02, 0xe9, 0x52, 0xef, 0xae, 0xc7, 0xf6, 0x68, 0x2f,
    0x35, 0x7e, 0xb9, 0x38, 0x7b, 0x22, 0x0f, 0x56, 0x64, 0xbf
};
static const unsigned char ENC_COS_PART3[] = {
    0xea, 0xc1, 0xab, 0x83, 0x55, 0xa4, 0x95, 0x3c, 0x63, 0x43,
    0xf0, 0x12, 0x2d, 0x4c, 0x77, 0x3e, 0x22, 0x9b, 0x19
};
static const int ENC_COS_LEN = 59;

static const unsigned char ENC_MAIN_PART1[] = {
    0xd6, 0x6c, 0x67, 0x77, 0xa1, 0x12, 0xf9, 0x42, 0xff, 0xbe,
    0xd7, 0xe6, 0x78, 0x6a, 0xda, 0x91, 0x76, 0xf7, 0xb4, 0xed
};
static const unsigned char ENC_MAIN_PART2[] = {
    0xc0, 0xcc, 0x54, 0x8f, 0xda, 0xf1, 0x9b, 0xb3, 0x65, 0x94,
    0xa5, 0x0c, 0x73, 0x53, 0xe0, 0x02, 0x3d, 0x5c, 0x67, 0x2e
};
static const unsigned char ENC_MAIN_PART3[] = {
    0x32, 0x8b, 0x09
};
static const int ENC_MAIN_LEN = 43;

static const unsigned char ENC_APPID[] = {
    0x55, 0x44, 0x81, 0x43, 0xbd
};
static const int ENC_APPID_LEN = 5;

static unsigned char* merge_parts(const unsigned char* part1, int len1,
                                  const unsigned char* part2, int len2,
                                  const unsigned char* part3, int len3,
                                  const unsigned char* part4, int len4,
                                  const unsigned char* part5, int len5,
                                  const unsigned char* part6, int len6) {
    int total_len = len1 + len2 + len3 + len4 + len5 + len6;
    unsigned char* merged = (unsigned char*)malloc(total_len);
    int offset = 0;
    
    if (part1 && len1 > 0) {
        memcpy(merged + offset, part1, len1);
        offset += len1;
    }
    if (part2 && len2 > 0) {
        memcpy(merged + offset, part2, len2);
        offset += len2;
    }
    if (part3 && len3 > 0) {
        memcpy(merged + offset, part3, len3);
        offset += len3;
    }
    if (part4 && len4 > 0) {
        memcpy(merged + offset, part4, len4);
        offset += len4;
    }
    if (part5 && len5 > 0) {
        memcpy(merged + offset, part5, len5);
        offset += len5;
    }
    if (part6 && len6 > 0) {
        memcpy(merged + offset, part6, len6);
        offset += len6;
    }
    
    return merged;
}

JNIEXPORT jstring JNICALL
Java_com_shenma_tvlauncher_utils_NativeHelper_getMainUrl(JNIEnv *env, jclass clazz) {
    if (check_debugger() || check_hooks()) {
        return (*env)->NewStringUTF(env, "");
    }
    char key[64];
    generate_key(key, 10);
    unsigned char* merged = merge_parts(ENC_MAIN_PART1, 20, ENC_MAIN_PART2, 20, ENC_MAIN_PART3, 3, NULL, 0, NULL, 0, NULL, 0);
    char* decrypted = multi_layer_decrypt(merged, 43, key);
    jstring result = (*env)->NewStringUTF(env, decrypted);
    free(merged);
    free(decrypted);
    return result;
}

JNIEXPORT jstring JNICALL
Java_com_shenma_tvlauncher_utils_NativeHelper_getCosUrl(JNIEnv *env, jclass clazz) {
    if (check_debugger() || check_hooks()) {
        return (*env)->NewStringUTF(env, "");
    }
    char key[64];
    generate_key(key, 10);
    unsigned char* merged = merge_parts(ENC_COS_PART1, 20, ENC_COS_PART2, 20, ENC_COS_PART3, 19, NULL, 0, NULL, 0, NULL, 0);
    char* decrypted = multi_layer_decrypt(merged, 59, key);
    jstring result = (*env)->NewStringUTF(env, decrypted);
    free(merged);
    free(decrypted);
    return result;
}

JNIEXPORT jstring JNICALL
Java_com_shenma_tvlauncher_utils_NativeHelper_getAppId(JNIEnv *env, jclass clazz) {
    if (check_debugger() || check_hooks()) {
        return (*env)->NewStringUTF(env, "");
    }
    char key[64];
    generate_key(key, 10);
    char* decrypted = multi_layer_decrypt(ENC_APPID, 5, key);
    jstring result = (*env)->NewStringUTF(env, decrypted);
    free(decrypted);
    return result;
}
