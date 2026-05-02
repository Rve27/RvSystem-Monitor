//! # RvSystem Monitor Rust Backend
//!
//! This crate provides the native implementation for system monitoring tasks in the RvSystem Monitor application.
//! It interfaces with the Android application via JNI (Java Native Interface).

#![allow(non_snake_case)]

use jni::objects::{JClass, JString};
use jni::sys::{jdoubleArray, jint, jstring};
use jni::EnvUnowned;
use jni::errors::LogErrorAndDefault;

pub mod kernel;
pub mod mm;

/// JNI interface to retrieve both RAM and ZRAM data in a single call.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_MemoryUtils_getMemoryDataNative<'local>(
    mut unowned_env: EnvUnowned<'local>,
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

    unowned_env.with_env(|env| {
        let output = env.new_double_array(14)?;
        env.set_double_array_region(&output, 0, &data)?;
        Ok::<_, jni::errors::Error>(output.into_raw())
    })
    .resolve::<LogErrorAndDefault>()
}

/// JNI interface to retrieve RAM data.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_MemoryUtils_getRamDataNative<'local>(
    mut unowned_env: EnvUnowned<'local>,
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

    unowned_env.with_env(|env| {
        let output = env.new_double_array(9)?;
        env.set_double_array_region(&output, 0, &data)?;
        Ok::<_, jni::errors::Error>(output.into_raw())
    })
    .resolve::<LogErrorAndDefault>()
}

/// JNI interface to retrieve ZRAM data.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_MemoryUtils_getZramDataNative<'local>(
    mut unowned_env: EnvUnowned<'local>,
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

    unowned_env.with_env(|env| {
        let output = env.new_double_array(5)?;
        env.set_double_array_region(&output, 0, &data)?;
        Ok::<_, jni::errors::Error>(output.into_raw())
    })
    .resolve::<LogErrorAndDefault>()
}

/// JNI interface to retrieve all core frequencies in a single call.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_CpuUtils_getAllCoreFrequenciesNative<
    'local,
>(
    mut unowned_env: EnvUnowned<'local>,
    _class: JClass<'local>,
) -> jni::sys::jobjectArray {
    let cores = kernel::cpu::get_core_count();
    let mut frequencies = Vec::with_capacity(cores as usize);

    for i in 0..cores {
        frequencies.push(kernel::cpu::get_core_frequency(i, "cur"));
    }

    unowned_env.with_env(|env| {
        let class_name = jni::strings::JNIString::try_from("java/lang/String").unwrap();
        let class = env.find_class(class_name)?;
        let initial_element = env.new_string("")?;
        let array = env.new_object_array(cores, &class, &initial_element)?;

        for (i, freq) in frequencies.into_iter().enumerate() {
            let j_freq = env.new_string(freq)?;
            env.set_object_array_element(&array, i as usize, &j_freq)?;
        }

        Ok::<_, jni::errors::Error>(array.into_raw())
    })
    .resolve::<LogErrorAndDefault>()
}

/// JNI interface to retrieve core count.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_CpuUtils_getCoreCountNative<'local>(
    mut unowned_env: EnvUnowned<'local>,
    _class: JClass<'local>,
) -> jint {
    unowned_env.with_env(|_env| {
        Ok::<_, jni::errors::Error>(kernel::cpu::get_core_count())
    })
    .resolve::<LogErrorAndDefault>()
}

/// JNI interface to retrieve core frequency.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_CpuUtils_getCoreFrequencyNative<'local>(
    mut unowned_env: EnvUnowned<'local>,
    _class: JClass<'local>,
    core_id: jint,
    freq_type: JString<'local>,
) -> jstring {
    unowned_env.with_env(|env| {
        let freq_type: String = env.get_string(&freq_type)?.into();
        let freq = kernel::cpu::get_core_frequency(core_id, &freq_type);
        Ok::<_, jni::errors::Error>(env.new_string(freq)?.into_raw())
    })
    .resolve::<LogErrorAndDefault>()
}

/// JNI interface to retrieve core governor.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_CpuUtils_getCoreGovernorNative<'local>(
    mut unowned_env: EnvUnowned<'local>,
    _class: JClass<'local>,
    core_id: jint,
) -> jstring {
    unowned_env.with_env(|env| {
        let governor = kernel::cpu::get_core_governor(core_id);
        Ok::<_, jni::errors::Error>(env.new_string(governor)?.into_raw())
    })
    .resolve::<LogErrorAndDefault>()
}
