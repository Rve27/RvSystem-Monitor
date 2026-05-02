//! # CPU Data Provider
//!
//! Provides functions to read and parse CPU information from the system.

use std::fs;

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

/// Retrieves the core frequency.
pub fn get_core_frequency(core_id: i32, freq_type: &str) -> String {
    let file_name = match freq_type {
        "max_info" => "cpuinfo_max_freq",
        "min_info" => "cpuinfo_min_freq",
        "cur" => "scaling_cur_freq",
        _ => return "N/A".to_string(),
    };

    let path = format!(
        "/sys/devices/system/cpu/cpu{}/cpufreq/{}",
        core_id, file_name
    );
    if let Ok(content) = fs::read_to_string(path) {
        let freq_khz: i64 = content.trim().parse().unwrap_or(0);
        format_frequency(freq_khz)
    } else {
        "N/A".to_string()
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

/// Formats frequency from KHz to MHz/GHz.
fn format_frequency(freq_khz: i64) -> String {
    if freq_khz >= 1_000_000 {
        format!("{:.2} GHz", freq_khz as f64 / 1_000_000.0)
    } else {
        format!("{} MHz", freq_khz / 1000)
    }
}
