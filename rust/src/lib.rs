//! # RvSystem Monitor Rust Backend
//!
//! This crate provides the native implementation for system monitoring tasks in the RvSystem Monitor application.
//! It interfaces with the Android application via JNI (Java Native Interface).

#![allow(non_snake_case)]

use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jdoubleArray, jint, jstring};

pub mod kernel;
pub mod mm;

/// JNI interface to retrieve both RAM and ZRAM data in a single call.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_MemoryUtils_getMemoryDataNative<'local>(
    env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jdoubleArray {
    let (ram, zram) = mm::memory::get_memory_data();

    let is_active = if zram.is_active { 1.0 } else { 0.0 };
    let data = [
        ram.total,
        ram.available,
        ram.used,
        ram.used_percentage,
        ram.cached,
        ram.buffers,
        ram.active,
        ram.inactive,
        ram.slab,
        is_active,
        zram.total,
        zram.available,
        zram.used,
        zram.used_percentage,
    ];

    let output = env.new_double_array(14).unwrap();
    env.set_double_array_region(&output, 0, &data).unwrap();

    output.into_raw()
}

/// JNI interface to retrieve RAM data.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_MemoryUtils_getRamDataNative<'local>(
    env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jdoubleArray {
    let (ram, _) = mm::memory::get_memory_data();

    let data = [
        ram.total,
        ram.available,
        ram.used,
        ram.used_percentage,
        ram.cached,
        ram.buffers,
        ram.active,
        ram.inactive,
        ram.slab,
    ];

    let output = env.new_double_array(9).unwrap();
    env.set_double_array_region(&output, 0, &data).unwrap();

    output.into_raw()
}

/// JNI interface to retrieve ZRAM data.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_MemoryUtils_getZramDataNative<'local>(
    env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jdoubleArray {
    let (_, zram) = mm::memory::get_memory_data();

    let is_active = if zram.is_active { 1.0 } else { 0.0 };
    let data = [
        is_active,
        zram.total,
        zram.available,
        zram.used,
        zram.used_percentage,
    ];

    let output = env.new_double_array(5).unwrap();
    env.set_double_array_region(&output, 0, &data).unwrap();

    output.into_raw()
}

/// JNI interface to retrieve all core frequencies in a single call.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_CpuUtils_getAllCoreFrequenciesNative<
    'local,
>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jni::sys::jobjectArray {
    let cores = kernel::cpu::get_core_count();
    let mut frequencies = Vec::with_capacity(cores as usize);

    for i in 0..cores {
        frequencies.push(kernel::cpu::get_core_frequency(i, "cur"));
    }

    let class = env.find_class("java/lang/String").unwrap();
    let initial_element = env.new_string("").unwrap();
    let array = env
        .new_object_array(cores, &class, &initial_element)
        .unwrap();

    for (i, freq) in frequencies.into_iter().enumerate() {
        let j_freq = env.new_string(freq).unwrap();
        env.set_object_array_element(&array, i as jni::sys::jsize, &j_freq)
            .unwrap();
    }

    array.into_raw()
}

/// JNI interface to retrieve core count.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_CpuUtils_getCoreCountNative<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jint {
    kernel::cpu::get_core_count()
}

/// JNI interface to retrieve core frequency.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_CpuUtils_getCoreFrequencyNative<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    core_id: jint,
    freq_type: JString<'local>,
) -> jstring {
    let freq_type: String = env.get_string(&freq_type).unwrap().into();
    let freq = kernel::cpu::get_core_frequency(core_id, &freq_type);
    env.new_string(freq).unwrap().into_raw()
}

/// JNI interface to retrieve core governor.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_CpuUtils_getCoreGovernorNative<'local>(
    env: JNIEnv<'local>,
    _class: JClass<'local>,
    core_id: jint,
) -> jstring {
    let governor = kernel::cpu::get_core_governor(core_id);
    env.new_string(governor).unwrap().into_raw()
}
