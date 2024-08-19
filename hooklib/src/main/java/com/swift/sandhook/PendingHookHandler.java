package com.swift.sandhook;

import android.util.Log;

import com.swift.sandhook.wrapper.HookErrorException;
import com.swift.sandhook.wrapper.HookWrapper;

import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

// Pending for hook static method
// When Init class error!
public class PendingHookHandler {

    private static Map<Class, Vector<HookWrapper.HookEntity>> pendingHooks = new ConcurrentHashMap<>();

    private static boolean canUsePendingHook;
    private static boolean hasInited = false;

//    static {
//        //init native hook
//        if (SandHookConfig.delayHook) {
//            canUsePendingHook = SandHook.initForPendingHook();
//        }
//    }

    public static boolean canWork() {
        if (SandHookConfig.delayHook && !hasInited) {
            canUsePendingHook = SandHook.initForPendingHook();
            hasInited = true;
        }
//        return canUsePendingHook && SandHook.canGetObject() && !SandHookConfig.DEBUG;
        return false;
    }

    public static synchronized void addPendingHook(HookWrapper.HookEntity hookEntity) {
        if (SandHookConfig.delayHook && !hasInited) {
            canUsePendingHook = SandHook.initForPendingHook();
            hasInited = true;
        }
        Vector<HookWrapper.HookEntity> entities = pendingHooks.get(hookEntity.target.getDeclaringClass());
        if (entities == null) {
            entities = new Vector<>();
            pendingHooks.put(hookEntity.target.getDeclaringClass(), entities);
        }
        entities.add(hookEntity);
    }

    public static void onClassInit(long clazz_ptr) {
        if (clazz_ptr == 0)
            return;
        Class clazz = (Class) SandHook.getObject(clazz_ptr);
        Log.e("xiawanli", " onClassInit is called, clazz = " +clazz);
        if (clazz == null)
            return;
        Vector<HookWrapper.HookEntity> entities = pendingHooks.get(clazz);
        if (entities == null)
            return;
        for (HookWrapper.HookEntity entity:entities) {
            HookLog.w("do pending hook for method: " + entity.target.toString());
            try {
                entity.initClass = false;
                SandHook.hook(entity);
            } catch (HookErrorException e) {
                HookLog.e("Pending Hook Error!", e);
            }
        }
        pendingHooks.remove(clazz);
    }

}
