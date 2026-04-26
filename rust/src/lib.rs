//! # RvSystem Monitor Rust Backend
//!
//! This crate provides the native implementation for system monitoring tasks in the RvSystem Monitor application.
//! It interfaces with the Android application via JNI (Java Native Interface).

#![allow(non_snake_case)]

use jni::JNIEnv;
use jni::objects::JClass;
use jni::sys::jdoubleArray;

pub mod mm;

/// JNI interface to retrieve RAM data.
///
/// This function is called from Kotlin's `MemoryUtils.getRamDataNative()`.
/// It returns a `jdoubleArray` containing:
/// 1. Total RAM (GB)
/// 2. Available RAM (GB)
/// 3. Used RAM (GB)
/// 4. Used RAM Percentage (%)
///
/// # Safety
/// This function is marked as `unsafe` because it is called via JNI and interacts with the JVM.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_MemoryUtils_getRamDataNative<'local>(
    env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jdoubleArray {
    let (ram, _) = mm::memory::get_memory_data();

    let data = [ram.total, ram.available, ram.used, ram.used_percentage];

    let output = env.new_double_array(4).unwrap();
    env.set_double_array_region(&output, 0, &data).unwrap();

    output.into_raw()
}

/// JNI interface to retrieve ZRAM data.
///
/// This function is called from Kotlin's `MemoryUtils.getZramDataNative()`.
/// It returns a `jdoubleArray` containing:
/// 1. Is Active (1.0 for true, 0.0 for false)
/// 2. Total ZRAM (GB)
/// 3. Available ZRAM (GB)
/// 4. Used ZRAM (GB)
/// 5. Used ZRAM Percentage (%)
///
/// # Safety
/// This function is marked as `unsafe` because it is called via JNI and interacts with the JVM.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_MemoryUtils_getZramDataNative<'local>(
    env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jdoubleArray {
    let (_, zram) = mm::memory::get_memory_data();

    let is_active = if zram.is_active { 1.0 } else { 0.0 };
    let data = [is_active, zram.total, zram.available, zram.used, zram.used_percentage];

    let output = env.new_double_array(5).unwrap();
    env.set_double_array_region(&output, 0, &data).unwrap();

    output.into_raw()
}
