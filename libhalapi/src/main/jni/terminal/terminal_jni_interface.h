#ifndef TERMINAL_JNI_INTERFACE_H_
#define TERMINAL_JNI_INTERFACE_H_
#include <jni.h>
const char* get_class_name();
//	+add by pengli
const char* get_class_name_internal();
//	-add by pengli
JNINativeMethod* get_methods(int* pCount);
#endif /* TERMINAL_JNI_INTERFACE_H_ */
