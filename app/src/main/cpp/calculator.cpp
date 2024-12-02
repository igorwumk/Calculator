#include <jni.h>

extern "C"
JNIEXPORT jdouble JNICALL
Java_pl_igorwumk_calculator_CalculatorViewModel_addJNI(JNIEnv *env, jobject thiz,
                                                       jdouble left_value, jdouble right_value) {
    return left_value + right_value;
}
extern "C"
JNIEXPORT jdouble JNICALL
Java_pl_igorwumk_calculator_CalculatorViewModel_subtractJNI(JNIEnv *env, jobject thiz,
                                                            jdouble left_value,
                                                            jdouble right_value) {
    return left_value - right_value;
}
extern "C"
JNIEXPORT jdouble JNICALL
Java_pl_igorwumk_calculator_CalculatorViewModel_multiplyJNI(JNIEnv *env, jobject thiz,
                                                            jdouble left_value,
                                                            jdouble right_value) {
    return left_value * right_value;
}
extern "C"
JNIEXPORT jdouble JNICALL
Java_pl_igorwumk_calculator_CalculatorViewModel_divideJNI(JNIEnv *env, jobject thiz,
                                                          jdouble left_value, jdouble right_value) {
    return left_value / right_value;
}