//! # RvSystem Monitor Rust Backend
//!
//! This crate provides the native implementation for system monitoring tasks in the RvSystem Monitor application.
//! It interfaces with the Android application via JNI (Java Native Interface).

#![allow(non_snake_case)]

use jni::EnvUnowned;
use jni::errors::LogErrorAndDefault;
use jni::objects::{JClass, JString};
use jni::sys::{jdouble, jdoubleArray, jint, jlong, jlongArray, jstring};

pub mod drivers;
pub mod kernel;
pub mod mm;

/// JNI interface to retrieve Vulkan version natively.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_GpuUtils_getVulkanVersionNative<'local>(
    mut unowned_env: EnvUnowned<'local>,
    _class: JClass<'local>,
) -> jstring {
    let version = drivers::gpu::vulkan::get_vulkan_version();

    unowned_env
        .with_env(|env| Ok::<_, jni::errors::Error>(env.new_string(version)?.into_raw()))
        .resolve::<LogErrorAndDefault>()
}

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

    unowned_env
        .with_env(|env| {
            let output = env.new_double_array(14)?;
            output.set_region(env, 0, &data)?;
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

    unowned_env
        .with_env(|env| {
            let output = env.new_double_array(9)?;
            output.set_region(env, 0, &data)?;
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

    unowned_env
        .with_env(|env| {
            let output = env.new_double_array(5)?;
            output.set_region(env, 0, &data)?;
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
) -> jlongArray {
    let cores = kernel::cpu::get_core_count();
    let mut frequencies = Vec::with_capacity(cores as usize);

    for i in 0..cores {
        frequencies.push(kernel::cpu::get_core_frequency(i, "cur"));
    }

    unowned_env
        .with_env(|env| {
            let output = env.new_long_array(cores as usize)?;
            output.set_region(env, 0, &frequencies)?;
            Ok::<_, jni::errors::Error>(output.into_raw())
        })
        .resolve::<LogErrorAndDefault>()
}

/// JNI interface to retrieve core count.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_CpuUtils_getCoreCountNative<'local>(
    mut unowned_env: EnvUnowned<'local>,
    _class: JClass<'local>,
) -> jint {
    unowned_env
        .with_env(|_env| Ok::<_, jni::errors::Error>(kernel::cpu::get_core_count()))
        .resolve::<LogErrorAndDefault>()
}

/// JNI interface to retrieve core frequency in KHz.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_CpuUtils_getCoreFrequencyNative<'local>(
    mut unowned_env: EnvUnowned<'local>,
    _class: JClass<'local>,
    core_id: jint,
    freq_type: JString<'local>,
) -> jlong {
    unowned_env
        .with_env(|env| {
            let freq_type: String = freq_type.try_to_string(env)?;
            Ok::<_, jni::errors::Error>(kernel::cpu::get_core_frequency(core_id, &freq_type))
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
    unowned_env
        .with_env(|env| {
            let governor = kernel::cpu::get_core_governor(core_id);
            Ok::<_, jni::errors::Error>(env.new_string(governor)?.into_raw())
        })
        .resolve::<LogErrorAndDefault>()
}

/// JNI interface to retrieve overall CPU temperature.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_CpuUtils_getCpuTemperatureNative<'local>(
    mut unowned_env: EnvUnowned<'local>,
    _class: JClass<'local>,
) -> jdouble {
    unowned_env
        .with_env(|_env| Ok::<_, jni::errors::Error>(kernel::cpu::get_cpu_temperature()))
        .resolve::<LogErrorAndDefault>()
}

/// JNI interface to retrieve all core temperatures in a single call.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_CpuUtils_getAllCoreTemperaturesNative<
    'local,
>(
    mut unowned_env: EnvUnowned<'local>,
    _class: JClass<'local>,
) -> jdoubleArray {
    let cores = kernel::cpu::get_core_count();
    let mut temps = Vec::with_capacity(cores as usize);

    for i in 0..cores {
        temps.push(kernel::cpu::get_core_temperature(i));
    }

    unowned_env
        .with_env(|env| {
            let output = env.new_double_array(cores as usize)?;
            output.set_region(env, 0, &temps)?;
            Ok::<_, jni::errors::Error>(output.into_raw())
        })
        .resolve::<LogErrorAndDefault>()
}

/// JNI interface to retrieve all dynamic CPU data (temp, freqs, core temps) in a single call.
/// Array structure: [overall_temp, core0_freq, core0_temp, core1_freq, core1_temp, ...]
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_rve_systemmonitor_utils_CpuUtils_getCpuDynamicDataNative<'local>(
    mut unowned_env: EnvUnowned<'local>,
    _class: JClass<'local>,
) -> jdoubleArray {
    let cores = kernel::cpu::get_core_count() as usize;
    let mut data = Vec::with_capacity(1 + 2 * cores);

    data.push(kernel::cpu::get_cpu_temperature());

    for i in 0..cores {
        data.push(kernel::cpu::get_core_frequency(i as i32, "cur") as f64);
        data.push(kernel::cpu::get_core_temperature(i as i32));
    }

    unowned_env
        .with_env(|env| {
            let output = env.new_double_array(data.len())?;
            output.set_region(env, 0, &data)?;
            Ok::<_, jni::errors::Error>(output.into_raw())
        })
        .resolve::<LogErrorAndDefault>()
}
