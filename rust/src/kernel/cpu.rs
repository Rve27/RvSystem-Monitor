//! # CPU Data Provider
//!
//! Provides functions to read and parse CPU information from the system.

use once_cell::sync::OnceCell;
use std::fs;
use std::sync::Mutex;

static CPU_THERMAL_ZONE: OnceCell<Option<i32>> = OnceCell::new();
static CORE_THERMAL_ZONES: OnceCell<Mutex<Vec<Option<i32>>>> = OnceCell::new();

/// Retrieves the number of CPU cores.
pub fn get_core_count() -> i32 {
    let mut count = 0;
    if let Ok(content) = fs::read_to_string("/sys/devices/system/cpu/present") {
        let content = content.trim();
        // Parse "0-7" or similar
        if content.contains('-') {
            let parts: Vec<&str> = content.split('-').collect();
            if parts.len() == 2 {
                let start: i32 = parts[0].parse().unwrap_or(0);
                let end: i32 = parts[1].parse().unwrap_or(0);
                count = end - start + 1;
            }
        } else {
            // Single core or comma-separated?
            count = content.split(',').count() as i32;
        }
    }

    if count <= 0 {
        // Fallback to internal runtime
        count = std::thread::available_parallelism()
            .map(|n| n.get() as i32)
            .unwrap_or(0);
    }

    count
}

/// Retrieves the core frequency in KHz.
pub fn get_core_frequency(core_id: i32, freq_type: &str) -> i64 {
    let file_name = match freq_type {
        "max_info" => "cpuinfo_max_freq",
        "min_info" => "cpuinfo_min_freq",
        "cur" => "scaling_cur_freq",
        _ => return 0,
    };

    let path = format!(
        "/sys/devices/system/cpu/cpu{}/cpufreq/{}",
        core_id, file_name
    );
    if let Ok(content) = fs::read_to_string(path) {
        content.trim().parse().unwrap_or(0)
    } else {
        0
    }
}

/// Retrieves the core scaling governor.
pub fn get_core_governor(core_id: i32) -> String {
    let path = format!(
        "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_governor",
        core_id
    );
    fs::read_to_string(path)
        .map(|s| s.trim().to_string())
        .unwrap_or_else(|_| "N/A".to_string())
}

/// Retrieves the overall CPU temperature in Celsius.
pub fn get_cpu_temperature() -> f64 {
    let zone_idx = CPU_THERMAL_ZONE.get_or_init(|| {
        let thermal_zones = [
            "cpu-thermal",
            "soc-thermal",
            "cpu",
            "soc",
            "thermal-cpufreq",
        ];
        for i in 0..30 {
            let type_path = format!("/sys/class/thermal/thermal_zone{}/type", i);
            if let Ok(tz_type) = fs::read_to_string(type_path) {
                let tz_type = tz_type.trim().to_lowercase();
                if thermal_zones.iter().any(|&tz| tz_type.contains(tz)) {
                    return Some(i);
                }
            }
        }
        None
    });

    if let Some(i) = zone_idx {
        let temp_path = format!("/sys/class/thermal/thermal_zone{}/temp", i);
        if let Ok(temp_str) = fs::read_to_string(temp_path) {
            if let Ok(temp) = temp_str.trim().parse::<f64>() {
                return if temp > 1000.0 { temp / 1000.0 } else { temp };
            }
        }
    }
    0.0
}

/// Retrieves the temperature for a specific core in Celsius.
pub fn get_core_temperature(core_id: i32) -> f64 {
    let cores = get_core_count() as usize;
    let zones_mutex = CORE_THERMAL_ZONES.get_or_init(|| Mutex::new(vec![None; cores]));
    let mut zones = zones_mutex.lock().unwrap();

    // Ensure vector is large enough if core count changed (unlikely but safe)
    if zones.len() <= core_id as usize {
        zones.resize(core_id as usize + 1, None);
    }

    if zones[core_id as usize].is_none() {
        let core_tz_type = format!("cpu{}-thermal", core_id);
        for i in 0..30 {
            let type_path = format!("/sys/class/thermal/thermal_zone{}/type", i);
            if let Ok(tz_type) = fs::read_to_string(type_path) {
                if tz_type.trim() == core_tz_type {
                    zones[core_id as usize] = Some(i);
                    break;
                }
            }
        }
    }

    if let Some(i) = zones[core_id as usize] {
        let temp_path = format!("/sys/class/thermal/thermal_zone{}/temp", i);
        if let Ok(temp_str) = fs::read_to_string(temp_path) {
            if let Ok(temp) = temp_str.trim().parse::<f64>() {
                return if temp > 1000.0 { temp / 1000.0 } else { temp };
            }
        }
    }

    // Fallback to overall CPU temperature if specific core temp is not found
    get_cpu_temperature()
}
