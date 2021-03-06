

#include <iostream>
#include <cwiid.h>


#include <jni.h>
#include "javah/Wii4Java_Manager.h"
#include "javah/Wii4Java_WiiListener.h"
#ifndef JNI_VERSION_1_2
#error "Needs Java version 1.2 or higher.\n"
#endif


cwiid_wiimote_t *wiimote = nullptr; // wiimote handle 
bdaddr_t bdaddr; // bluetooth device address 

jobject wiiListener;
JavaVM * jvm;
/**
 * Java Method identifier of the method "WiiListener.connectionChanged".
 */
jmethodID connectionChangedMid;
/**
 * Java Method identifier of the method "WiiListener.buttonAChanged".
 */
jmethodID buttonAChangedMid;
/**
 * Java Method identifier of the method "WiiListener.buttonBChanged".
 */
jmethodID buttonBChangedMid;
/**
 * Java Method identifier of the method "WiiListener.buttonEvent".
 */
jmethodID buttonEventMid;
/**
 * Java Method identifier of the method "WiiListener.accelerometerEvent".
 */
jmethodID accelerometerEventMid;
cwiid_mesg_callback_t cwiid_callback;

acc_cal wm_cal;

uint16_t previousButtons;
uint16_t buttonAmask = 8;
uint16_t buttonBmask = 4;

void handleButtonMessage(uint16_t buttons) {
  JNIEnv * env;
  uint16_t changedButtons = buttons ^ previousButtons;
  jvm->AttachCurrentThread((void**) &env, NULL);
  if (changedButtons & buttonAmask) {
    env->CallVoidMethod(wiiListener, buttonAChangedMid,
            (jboolean) (buttons & buttonAmask));
  }

  if (changedButtons & buttonBmask) {
    env->CallVoidMethod(wiiListener, buttonBChangedMid,
            (jboolean) (buttons & buttonBmask));
  }

  env->CallVoidMethod(wiiListener, buttonEventMid,
          (jint) previousButtons,
          (jint) buttons);

  jvm->DetachCurrentThread();
  previousButtons = buttons;
}

void handleAccelerometerEvent(int accX, int accY, int accZ) {
  JNIEnv * env;

  jvm->AttachCurrentThread((void**) &env, NULL);


  env->CallVoidMethod(wiiListener, accelerometerEventMid,
          (jint) accX,
          (jint) accY,
          (jint) accZ);

  jvm->DetachCurrentThread();
}

/**
 * 
 * @param wiimote
 * @param mesg_count
 * @param mesg
 * @param timestamp
 */
void cwiid_callback(cwiid_wiimote_t *wiimote, int mesg_count,
        union cwiid_mesg mesg[], struct timespec *timestamp) {
  for (int i = 0; i < mesg_count; i++) {
    switch (mesg[i].type) {
      case CWIID_MESG_BTN:
        handleButtonMessage(mesg[i].btn_mesg.buttons);
        break;
      case CWIID_MESG_ACC:
        handleAccelerometerEvent(
                mesg[i].acc_mesg.acc[CWIID_X] - wm_cal.zero[CWIID_X],
                mesg[i].acc_mesg.acc[CWIID_Y] - wm_cal.zero[CWIID_Y],
                mesg[i].acc_mesg.acc[CWIID_Z] - wm_cal.zero[CWIID_Z]);
        break;
      default:
        printf("Unknown Report");
        break;
    }
  }
}

/*
 * Class:     Wii4Java_Manager
 * Method:    nConnect
 * Signature: (LWii4Java/WiiListener;)V
 */
JNIEXPORT void JNICALL Java_Wii4Java_Manager_nConnect
(JNIEnv * env, jclass, jobject listener) {

  if (wiimote != nullptr) {
    std::cerr << "Wiimote already connected. Cannot connect.\n";
    return;
  }

  env->GetJavaVM(&jvm);


  bdaddr = {
    {0, 0, 0, 0, 0, 0}
  };

  // cache the method identifier of the given WiiListener object.
  wiiListener = env->NewGlobalRef(listener);
  jclass listenerClass = env->GetObjectClass(wiiListener);
  connectionChangedMid = env->GetMethodID(listenerClass, "connectionChangedNative", "(I)V");
  buttonAChangedMid = env->GetMethodID(listenerClass, "buttonAChangedNative", "(Z)V");
  buttonBChangedMid = env->GetMethodID(listenerClass, "buttonBChangedNative", "(Z)V");
  buttonEventMid = env->GetMethodID(listenerClass, "buttonEventNative", "(II)V");
  accelerometerEventMid = env->GetMethodID(listenerClass, "accelerometerEvent", "(III)V");

  // Connect to the wiimote
  wiimote = cwiid_open(&bdaddr, 0);
  if (wiimote == nullptr) {
    std::cerr << "Unable to connect to wiimote\n";
    env->CallVoidMethod(wiiListener, connectionChangedMid,
            (jint) Wii4Java_WiiListener_ABORTED);
    env->DeleteGlobalRef(wiiListener);
    return;
  }

  if (cwiid_set_mesg_callback(wiimote, cwiid_callback)) {
    std::cerr << "Unable to set message callback\n";
    env->CallVoidMethod(wiiListener, connectionChangedMid,
            (jint) Wii4Java_WiiListener_ABORTED);
    env->DeleteGlobalRef(wiiListener);
    wiimote = nullptr;
    return;
  }

  if (cwiid_get_acc_cal(wiimote, CWIID_EXT_NONE, &wm_cal)) {
    std::cerr << "Unable to retrieve accelerometer calibration\n";
    wm_cal.zero[CWIID_X] = 124;
    wm_cal.zero[CWIID_Y] = 124;
    wm_cal.zero[CWIID_Z] = 124;
  }

  cwiid_enable(wiimote, CWIID_FLAG_MESG_IFC);
  cwiid_set_rpt_mode(wiimote, CWIID_RPT_BTN | CWIID_RPT_ACC);

  env->CallVoidMethod(wiiListener, connectionChangedMid,
          (jint) Wii4Java_WiiListener_CONNECTED);

}

/*
 * Class:     Wii4Java_Manager
 * Method:    nDisconnect
 * Signature: (LWii4Java/WiiListener;)V
 */
JNIEXPORT void JNICALL Java_Wii4Java_Manager_nDisconnect
(JNIEnv * env, jclass, jobject) {

  if (wiimote == nullptr) {
    std::cerr << "Wiimote is not connected. Cannot disconnect.\n";
    return;
  }

  std::cout << "JNICALL Java_Wii4Java_Manager_nDisconnect...\n";
  if (cwiid_close(wiimote)) {
    std::cerr << "Error on wiimote disconnect\n";
  }
  env->CallVoidMethod(wiiListener, connectionChangedMid,
          (jint) Wii4Java_WiiListener_ENDED);
  wiimote = nullptr;
  env->DeleteGlobalRef(wiiListener);

}