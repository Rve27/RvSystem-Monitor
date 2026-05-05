//! # CPU Data Provider
//!
//! Provides functions to read and parse CPU information from the system.

use once_cell::sync::OnceCell;
use std::collections::HashMap;
use std::fs::{self, File};
use std::io::{Read, Seek, SeekFrom};
use std::path::PathBuf;
use std::sync::{Mutex, RwLock};

fn read_fd_parsed<T: std::str::FromStr>(file: &mut File, buf: &mut String) -> Option<T> {
    buf.clear();
    file.seek(SeekFrom::Start(0)).ok()?;
    file.read_to_string(buf).ok()?;
    buf.trim().parse::<T>().ok()
}

fn read_path_parsed<T: std::str::FromStr>(path: &str, buf: &mut String) -> Option<T> {
    buf.clear();
    if let Ok(mut f) = File::open(path)
        && f.read_to_string(buf).is_ok()
    {
        return buf.trim().parse::<T>().ok();
    }
    None
}

struct CpuFds {
    cur_freq: Vec<Option<File>>,
    max_freq: Vec<Option<File>>,
    min_freq: Vec<Option<File>>,
    governor: Vec<Option<File>>,
}

static CPU_FDS: OnceCell<Mutex<CpuFds>> = OnceCell::new();

fn get_cpu_fds() -> &'static Mutex<CpuFds> {
    CPU_FDS.get_or_init(|| {
        let cores = get_core_count() as usize;

        let open_opt = |path: String| -> Option<File> { File::open(&path).ok() };

        let mut cur_freq = Vec::with_capacity(cores);
        let mut max_freq = Vec::with_capacity(cores);
        let mut min_freq = Vec::with_capacity(cores);
        let mut governor = Vec::with_capacity(cores);

        for i in 0..cores {
            cur_freq.push(open_opt(format!(
                "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_cur_freq",
                i
            )));
            max_freq.push(open_opt(format!(
                "/sys/devices/system/cpu/cpu{}/cpufreq/cpuinfo_max_freq",
                i
            )));
            min_freq.push(open_opt(format!(
                "/sys/devices/system/cpu/cpu{}/cpufreq/cpuinfo_min_freq",
                i
            )));
            governor.push(open_opt(format!(
                "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_governor",
                i
            )));
        }

        Mutex::new(CpuFds {
            cur_freq,
            max_freq,
            min_freq,
            governor,
        })
    })
}

static THERMAL_MAP: OnceCell<HashMap<String, PathBuf>> = OnceCell::new();

fn get_thermal_map() -> &'static HashMap<String, PathBuf> {
    THERMAL_MAP.get_or_init(|| {
        let mut map = HashMap::new();
        if let Ok(entries) = fs::read_dir("/sys/class/thermal") {
            for entry in entries.flatten() {
                let base = entry.file_name();
                let name = base.to_string_lossy();
                if !name.starts_with("thermal_zone") {
                    continue;
                }
                let type_path = entry.path().join("type");
                let temp_path = entry.path().join("temp");
                if let Ok(tz_type) = fs::read_to_string(&type_path) {
                    map.insert(tz_type.trim().to_lowercase(), temp_path);
                }
            }
        }
        map
    })
}

static CORE_THERMAL_PATHS: OnceCell<RwLock<Vec<Option<PathBuf>>>> = OnceCell::new();

fn get_core_thermal_paths() -> &'static RwLock<Vec<Option<PathBuf>>> {
    CORE_THERMAL_PATHS.get_or_init(|| {
        let cores = get_core_count() as usize;
        RwLock::new(vec![None; cores])
    })
}

pub fn get_core_count() -> i32 {
    if let Ok(content) = fs::read_to_string("/sys/devices/system/cpu/present") {
        let content = content.trim();
        if let Some((start_str, end_str)) = content.split_once('-') {
            let start: i32 = start_str.parse().unwrap_or(0);
            let end: i32 = end_str.parse().unwrap_or(0);
            return end - start + 1;
        }
        return content.split(',').count() as i32;
    }
    std::thread::available_parallelism()
        .map(|n| n.get() as i32)
        .unwrap_or(0)
}

pub fn get_core_frequency(core_id: i32, freq_type: &str) -> i64 {
    let core_idx = core_id as usize;
    let mut buf = String::with_capacity(32);

    let fds_mutex = get_cpu_fds();
    let mut fds = fds_mutex.lock().unwrap();

    let slot: Option<&mut Option<File>> = match freq_type {
        "max_info" => fds.max_freq.get_mut(core_idx),
        "min_info" => fds.min_freq.get_mut(core_idx),
        "cur" => fds.cur_freq.get_mut(core_idx),
        _ => None,
    };

    if let Some(Some(file)) = slot {
        return read_fd_parsed::<i64>(file, &mut buf).unwrap_or(0);
    }
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
    read_path_parsed::<i64>(&path, &mut buf).unwrap_or(0)
}

pub fn get_core_governor(core_id: i32) -> String {
    let core_idx = core_id as usize;
    let mut buf = String::with_capacity(32);

    let fds_mutex = get_cpu_fds();
    let mut fds = fds_mutex.lock().unwrap();

    if let Some(Some(file)) = fds.governor.get_mut(core_idx) {
        buf.clear();
        if file.seek(SeekFrom::Start(0)).is_ok() && file.read_to_string(&mut buf).is_ok() {
            let len = buf.trim_end().len();
            buf.truncate(len);
            return buf;
        }
    }

    // Fallback
    let path = format!(
        "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_governor",
        core_id
    );
    fs::read_to_string(&path)
        .map(|mut s| {
            let l = s.trim_end().len();
            s.truncate(l);
            s
        })
        .unwrap_or_else(|_| "N/A".to_string())
}

pub fn get_cpu_temperature() -> f64 {
    let map = get_thermal_map();
    let priority = [
        "cpu-thermal",
        "soc-thermal",
        "cpu",
        "soc",
        "thermal-cpufreq",
    ];
    let mut buf = String::with_capacity(16);

    for zone in priority {
        if let Some(temp_path) = map.get(zone)
            && let Some(temp) = read_path_parsed::<f64>(temp_path.to_str().unwrap_or(""), &mut buf)
        {
            return if temp > 1000.0 { temp / 1000.0 } else { temp };
        }
    }

    for (tz_type, temp_path) in map {
        if priority.iter().any(|p| tz_type.contains(p))
            && let Some(temp) = read_path_parsed::<f64>(temp_path.to_str().unwrap_or(""), &mut buf)
        {
            return if temp > 1000.0 { temp / 1000.0 } else { temp };
        }
    }
    0.0
}

pub fn get_core_temperature(core_id: i32) -> f64 {
    let rw = get_core_thermal_paths();
    let mut buf = String::with_capacity(16);

    {
        let paths = rw.read().unwrap();
        if let Some(Some(path)) = paths.get(core_id as usize)
            && let Some(temp) = read_path_parsed::<f64>(path.to_str().unwrap_or(""), &mut buf)
        {
            return if temp > 1000.0 { temp / 1000.0 } else { temp };
        }
    }

    let key = format!("cpu{}-thermal", core_id);
    let map = get_thermal_map();
    let found_path = map.get(&key).cloned();

    {
        let mut paths = rw.write().unwrap();
        if paths.len() <= core_id as usize {
            paths.resize(core_id as usize + 1, None);
        }
        if paths[core_id as usize].is_none() {
            paths[core_id as usize] = found_path.clone();
        }
    }

    if let Some(path) = found_path
        && let Some(temp) = read_path_parsed::<f64>(path.to_str().unwrap_or(""), &mut buf)
    {
        return if temp > 1000.0 { temp / 1000.0 } else { temp };
    }

    get_cpu_temperature()
}
