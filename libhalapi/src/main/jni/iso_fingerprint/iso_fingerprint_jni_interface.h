#ifndef LED_JNI_INTERFACE_H_
#define LED_JNI_INTERFACE_H_

#include <jni.h>

const char* iso_fingerprint_get_class_name();
//	+add by pengli
const char* get_class_name_internal();
//	-add by pengli
JNINativeMethod* iso_fingerprint_get_methods(int* pCount);

#endif /* LED_JNI_INTERFACE_H_ */
