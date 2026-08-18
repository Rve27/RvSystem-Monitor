//! # Thermal Zone Provider
//!
//! Discovers and reads thermal zones from sysfs.

use once_cell::sync::OnceCell;
use std::collections::HashMap;
use std::fs::{self, File};
use std::io::{Read, Seek};
use std::path::PathBuf;
use std::sync::Mutex;

use super::cpu::get_core_count;

fn read_fd_parsed<T: std::str::FromStr>(file: &mut File, buf: &mut String) -> Option<T> {
    buf.clear();
    file.seek(std::io::SeekFrom::Start(0)).ok()?;
    file.read_to_string(buf).ok()?;
    buf.trim().parse::<T>().ok()
}

fn read_temp(file_opt: &mut Option<File>, buf: &mut String) -> f64 {
    if let Some(file) = file_opt.as_mut()
        && let Some(temp) = read_fd_parsed::<f64>(file, buf)
    {
        return if temp > 1000.0 {
            temp / 1000.0
        } else if temp > 150.0 {
            temp / 10.0
        } else {
            temp
        };
    }
    0.0
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

static CPU_THERMAL_FD: OnceCell<Mutex<Option<File>>> = OnceCell::new();
static GPU_THERMAL_FD: OnceCell<Mutex<Option<File>>> = OnceCell::new();

fn get_thermal_fd_from_priority(
    map: &HashMap<String, PathBuf>,
    priority: &[&str],
) -> Mutex<Option<File>> {
    let mut best_path = None;
    let mut buf = String::with_capacity(32);

    let mut is_valid = |p: &PathBuf| -> bool {
        let mut file_opt = File::open(p).ok();
        let temp = read_temp(&mut file_opt, &mut buf);
        temp > 5.0 && temp < 120.0
    };

    for zone in priority {
        if let Some(path) = map.get(*zone).filter(|p| is_valid(p)) {
            best_path = Some(path.clone());
            break;
        }
    }
    if best_path.is_none() {
        for zone in priority {
            if let Some((_, path)) = map.iter().find(|(k, p)| k.contains(*zone) && is_valid(p)) {
                best_path = Some(path.clone());
                break;
            }
        }
    }
    let file = best_path.and_then(|p| File::open(p).ok());
    Mutex::new(file)
}

fn get_cpu_thermal_fd() -> &'static Mutex<Option<File>> {
    CPU_THERMAL_FD.get_or_init(|| {
        get_thermal_fd_from_priority(
            get_thermal_map(),
            &[
                "soc_max",
                "soc_thermal",
                "soc-thermal",
                "cpu_max",
                "cpu-thermal",
                "msm_therm",
                "mtktsap",
                "ap_ntc",
                "apc",
                "cpuss",
                "cpu",
                "soc",
                "tsens_tz_sensor0",
                "thermal-cpufreq",
            ],
        )
    })
}

fn get_gpu_thermal_fd() -> &'static Mutex<Option<File>> {
    GPU_THERMAL_FD.get_or_init(|| {
        get_thermal_fd_from_priority(
            get_thermal_map(),
            &[
                "gpu-thermal",
                "gpu0-thermal",
                "gpuss-0-usr",
                "gpu",
                "tsens_tz_sensor9",
            ],
        )
    })
}

static CORE_THERMAL_FDS: OnceCell<Mutex<Vec<Option<File>>>> = OnceCell::new();

fn get_core_thermal_fds() -> &'static Mutex<Vec<Option<File>>> {
    CORE_THERMAL_FDS.get_or_init(|| {
        let cores = get_core_count() as usize;
        let map = get_thermal_map();
        let mut fds = Vec::with_capacity(cores);

        let mut qc_zones: Vec<(i32, i32, &str)> = Vec::new();
        for key in map.keys() {
            if key.starts_with("cpu-") {
                let parts: Vec<&str> = key.split('-').collect();
                if parts.len() >= 3 {
                    if let (Ok(c), Ok(n)) = (parts[1].parse::<i32>(), parts[2].parse::<i32>()) {
                        qc_zones.push((c, n, key));
                        continue;
                    }
                    if let Some(n) = parts[2].strip_prefix("core").and_then(|s| s.parse().ok()) {
                        let c = match parts[1] {
                            "little" => 0,
                            "medium" => 1,
                            "big" => 2,
                            "prime" => 3,
                            _ => continue,
                        };
                        qc_zones.push((c, n, key));
                    }
                }
            } else if key.starts_with("cpu_") {
                let is_little = key.starts_with("cpu_little");
                let is_big = key.starts_with("cpu_big");
                if is_little || is_big {
                    let prefix_len = if is_little {
                        "cpu_little".len()
                    } else {
                        "cpu_big".len()
                    };
                    if let Ok(n) = key[prefix_len..].parse::<i32>() {
                        let c = if is_little { 0 } else { 1 };
                        qc_zones.push((c, n - 1, key));
                    }
                }
            } else if key.starts_with("cpu") {
                if let Some(n) = key.find('-').and_then(|i| key[3..i].parse().ok()) {
                    let rest = &key[key.find('-').unwrap() + 1..];
                    let c = match rest {
                        r if r.starts_with("silver") || r.starts_with("little") => 0,
                        r if r.starts_with("gold") || r.starts_with("big") => 1,
                        r if r.starts_with("prime") => 2,
                        _ => continue,
                    };
                    qc_zones.push((c, n, key));
                }
            } else if let Some(n) = key
                .strip_prefix("tsens_tz_sensor")
                .and_then(|s| s.parse().ok())
            {
                let core_idx = if (1..=8).contains(&n) {
                    n - 1
                } else if n == 0 {
                    99
                } else {
                    n
                };
                qc_zones.push((99, core_idx, key));
            }
        }

        let mut unique_cn: HashMap<(i32, i32), &str> = HashMap::new();
        for (c, n, key) in qc_zones {
            if !unique_cn.contains_key(&(c, n))
                || key.ends_with("-0")
                || key.ends_with("-0-0")
                || key.ends_with("-usr")
            {
                unique_cn.insert((c, n), key);
            }
        }

        let mut sorted_cn: Vec<(&(i32, i32), &&str)> = unique_cn.iter().collect();
        sorted_cn.sort_by(|a, b| {
            if a.0.0 != b.0.0 {
                a.0.0.cmp(&b.0.0)
            } else {
                a.0.1.cmp(&b.0.1)
            }
        });

        for i in 0..cores {
            if i < sorted_cn.len() {
                let key = *sorted_cn[i].1;
                fds.push(map.get(key).and_then(|p| File::open(p).ok()));
            } else {
                fds.push(None);
            }
        }

        Mutex::new(fds)
    })
}

pub fn get_cpu_temperature() -> f64 {
    let mut buf = String::with_capacity(16);
    let mut fd_mutex = get_cpu_thermal_fd().lock().unwrap();
    read_temp(&mut fd_mutex, &mut buf)
}

pub fn get_gpu_temperature() -> f64 {
    let mut buf = String::with_capacity(16);
    let mut fd_mutex = get_gpu_thermal_fd().lock().unwrap();
    read_temp(&mut fd_mutex, &mut buf)
}

pub fn get_core_temperature(core_id: i32) -> f64 {
    let mut buf = String::with_capacity(16);
    let mut fds_mutex = get_core_thermal_fds().lock().unwrap();
    if let Some(slot) = fds_mutex.get_mut(core_id as usize) {
        let temp = read_temp(slot, &mut buf);
        if temp != 0.0 {
            return temp;
        }
    }
    get_cpu_temperature()
}

pub fn get_all_core_temperatures() -> Vec<f64> {
    let cores = get_core_count() as usize;
    let mut result = Vec::with_capacity(cores);
    let mut buf = String::with_capacity(16);
    let mut fds_mutex = get_core_thermal_fds().lock().unwrap();

    for i in 0..cores {
        if let Some(slot) = fds_mutex.get_mut(i) {
            let temp = read_temp(slot, &mut buf);
            result.push(if temp != 0.0 { temp } else { 0.0 });
        } else {
            result.push(0.0);
        }
    }
    result
}
