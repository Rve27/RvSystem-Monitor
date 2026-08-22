use libc::{RTLD_NOW, dlopen, dlsym};
use std::ptr;

type VkInstance = *mut std::ffi::c_void;
type VkPhysicalDevice = *mut std::ffi::c_void;
type VkResult = i32;

#[repr(C)]
#[derive(Clone)]
struct VkExtensionProperties {
    extension_name: [std::ffi::c_char; 256],
    spec_version: u32,
}

#[repr(C)]
struct VkApplicationInfo {
    s_type: i32,
    p_next: *const std::ffi::c_void,
    p_application_name: *const i8,
    application_version: u32,
    p_engine_name: *const i8,
    engine_version: u32,
    api_version: u32,
}

#[repr(C)]
struct VkInstanceCreateInfo {
    s_type: i32,
    p_next: *const std::ffi::c_void,
    flags: u32,
    p_application_info: *const VkApplicationInfo,
    enabled_layer_count: u32,
    pp_enabled_layer_names: *const *const i8,
    enabled_extension_count: u32,
    pp_enabled_extension_names: *const *const i8,
}

pub fn get_vulkan_version() -> String {
    unsafe {
        let handle = dlopen(c"libvulkan.so".as_ptr(), RTLD_NOW);

        if handle.is_null() {
            return "Not Supported".to_string();
        }

        // Load functions with explicit null checks
        let vk_create_instance_ptr = dlsym(handle, c"vkCreateInstance".as_ptr());
        let vk_destroy_instance_ptr = dlsym(handle, c"vkDestroyInstance".as_ptr());
        let vk_enumerate_physical_devices_ptr =
            dlsym(handle, c"vkEnumeratePhysicalDevices".as_ptr());
        let vk_get_physical_device_properties_ptr =
            dlsym(handle, c"vkGetPhysicalDeviceProperties".as_ptr());
        let vk_enumerate_instance_version_ptr =
            dlsym(handle, c"vkEnumerateInstanceVersion".as_ptr());

        let vk_enumerate_instance_version: Option<extern "system" fn(*mut u32) -> i32> =
            if !vk_enumerate_instance_version_ptr.is_null() {
                Some(std::mem::transmute::<
                    *mut libc::c_void,
                    extern "system" fn(*mut u32) -> i32,
                >(vk_enumerate_instance_version_ptr))
            } else {
                None
            };

        let vk_enumerate_device_extension_properties_ptr =
            dlsym(handle, c"vkEnumerateDeviceExtensionProperties".as_ptr());
        let vk_enumerate_instance_extension_properties_ptr =
            dlsym(handle, c"vkEnumerateInstanceExtensionProperties".as_ptr());

        if vk_create_instance_ptr.is_null()
            || vk_destroy_instance_ptr.is_null()
            || vk_enumerate_physical_devices_ptr.is_null()
            || vk_get_physical_device_properties_ptr.is_null()
        {
            return query_instance_version(vk_enumerate_instance_version);
        }

        let vk_create_instance: extern "system" fn(
            *const VkInstanceCreateInfo,
            *const std::ffi::c_void,
            *mut VkInstance,
        ) -> VkResult = std::mem::transmute(vk_create_instance_ptr);
        let vk_destroy_instance: extern "system" fn(VkInstance, *const std::ffi::c_void) =
            std::mem::transmute(vk_destroy_instance_ptr);
        let vk_enumerate_physical_devices: extern "system" fn(
            VkInstance,
            *mut u32,
            *mut VkPhysicalDevice,
        ) -> VkResult = std::mem::transmute(vk_enumerate_physical_devices_ptr);
        let vk_get_physical_device_properties: extern "system" fn(VkPhysicalDevice, *mut u8) =
            std::mem::transmute(vk_get_physical_device_properties_ptr);

        let vk_enumerate_device_extension_properties: Option<
            extern "system" fn(
                VkPhysicalDevice,
                *const i8,
                *mut u32,
                *mut std::ffi::c_void,
            ) -> VkResult,
        > = if !vk_enumerate_device_extension_properties_ptr.is_null() {
            Some(std::mem::transmute::<
                *mut libc::c_void,
                extern "system" fn(
                    VkPhysicalDevice,
                    *const i8,
                    *mut u32,
                    *mut std::ffi::c_void,
                ) -> VkResult,
            >(vk_enumerate_device_extension_properties_ptr))
        } else {
            None
        };


        let vk_enumerate_instance_extension_properties: Option<
            extern "system" fn(
                *const i8,
                *mut u32,
                *mut std::ffi::c_void,
            ) -> VkResult,
        > = if !vk_enumerate_instance_extension_properties_ptr.is_null() {
            Some(std::mem::transmute::<
                *mut libc::c_void,
                extern "system" fn(
                    *const i8,
                    *mut u32,
                    *mut std::ffi::c_void,
                ) -> VkResult,
            >(vk_enumerate_instance_extension_properties_ptr))
        } else {
            None
        };


        let mut inst_extension_count: u32 = 0;
        let mut inst_extensions_str = String::new();
        if let Some(vk_enum_inst_ext_props) = vk_enumerate_instance_extension_properties {
            vk_enum_inst_ext_props(
                ptr::null(),
                &mut inst_extension_count,
                ptr::null_mut(),
            );
            if inst_extension_count > 0 {
                let mut extensions = vec![
                    VkExtensionProperties {
                        extension_name: [0; 256],
                        spec_version: 0,
                    };
                    inst_extension_count as usize
                ];
                if vk_enum_inst_ext_props(
                    ptr::null(),
                    &mut inst_extension_count,
                    extensions.as_mut_ptr() as *mut std::ffi::c_void,
                ) == 0 {
                    let names: Vec<String> = extensions
                        .iter()
                        .map(|ext| {
                            let c_str = std::ffi::CStr::from_ptr(ext.extension_name.as_ptr());
                            c_str.to_string_lossy().into_owned()
                        })
                        .collect();
                    inst_extensions_str = names.join(",");
                }
            }
        }

        // Create a minimal instance
        let app_info = VkApplicationInfo {
            s_type: 0, // VK_STRUCTURE_TYPE_APPLICATION_INFO
            p_next: ptr::null(),
            p_application_name: ptr::null(),
            application_version: 0,
            p_engine_name: ptr::null(),
            engine_version: 0,
            api_version: 0x00400000, // 1.0.0
        };

        let create_info = VkInstanceCreateInfo {
            s_type: 1, // VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO
            p_next: ptr::null(),
            flags: 0,
            p_application_info: &app_info,
            enabled_layer_count: 0,
            pp_enabled_layer_names: ptr::null(),
            enabled_extension_count: 0,
            pp_enabled_extension_names: ptr::null(),
        };

        let mut instance: VkInstance = ptr::null_mut();
        if vk_create_instance(&create_info, ptr::null(), &mut instance) == 0 {
            let mut device_count: u32 = 0;
            if vk_enumerate_physical_devices(instance, &mut device_count, ptr::null_mut()) == 0
                && device_count > 0
            {
                let mut devices = vec![ptr::null_mut(); device_count as usize];
                if vk_enumerate_physical_devices(instance, &mut device_count, devices.as_mut_ptr())
                    == 0
                {
                    // VkPhysicalDeviceProperties is a large struct (~824+ bytes).
                    // We extract apiVersion (offset 0) and driverVersion (offset 4).
                    // We use a 1024-byte buffer to safely accommodate the full struct and avoid stack corruption.
                    let mut props = [0u8; 1024];
                    vk_get_physical_device_properties(devices[0], props.as_mut_ptr());

                    let api_version = u32::from_le_bytes(props[0..4].try_into().unwrap());
                    let driver_version = u32::from_le_bytes(props[4..8].try_into().unwrap());
                    let device_type = u32::from_le_bytes(props[16..20].try_into().unwrap());
                    let max_image_1d = u32::from_le_bytes(props[296..300].try_into().unwrap());
                    let max_image_2d = u32::from_le_bytes(props[300..304].try_into().unwrap());
                    let max_image_3d = u32::from_le_bytes(props[304..308].try_into().unwrap());
                    let max_image_cube = u32::from_le_bytes(props[308..312].try_into().unwrap());
                    let max_image_array_layers =
                        u32::from_le_bytes(props[312..316].try_into().unwrap());
                    let max_uniform_buffer_range =
                        u32::from_le_bytes(props[320..324].try_into().unwrap());
                    let max_storage_buffer_range =
                        u32::from_le_bytes(props[324..328].try_into().unwrap());
                    let max_sampler_anisotropy =
                        f32::from_le_bytes(props[568..572].try_into().unwrap());

                    let sample_counts = u32::from_le_bytes(props[672..676].try_into().unwrap());
                    let max_color_samples = if (sample_counts & 0x40) != 0 {
                        64
                    } else if (sample_counts & 0x20) != 0 {
                        32
                    } else if (sample_counts & 0x10) != 0 {
                        16
                    } else if (sample_counts & 0x08) != 0 {
                        8
                    } else if (sample_counts & 0x04) != 0 {
                        4
                    } else if (sample_counts & 0x02) != 0 {
                        2
                    } else if (sample_counts & 0x01) != 0 {
                        1
                    } else {
                        0
                    };

                    let depth_sample_counts =
                        u32::from_le_bytes(props[676..680].try_into().unwrap());
                    let max_depth_samples = if (depth_sample_counts & 0x40) != 0 {
                        64
                    } else if (depth_sample_counts & 0x20) != 0 {
                        32
                    } else if (depth_sample_counts & 0x10) != 0 {
                        16
                    } else if (depth_sample_counts & 0x08) != 0 {
                        8
                    } else if (depth_sample_counts & 0x04) != 0 {
                        4
                    } else if (depth_sample_counts & 0x02) != 0 {
                        2
                    } else if (depth_sample_counts & 0x01) != 0 {
                        1
                    } else {
                        0
                    };

                    let mut extension_count: u32 = 0;
                    let mut extensions_str = String::new();
                    if let Some(vk_enumerate_device_extension_props) =
                        vk_enumerate_device_extension_properties
                    {
                        vk_enumerate_device_extension_props(
                            devices[0],
                            ptr::null(),
                            &mut extension_count,
                            ptr::null_mut(),
                        );

                        if extension_count > 0 {
                            let mut extensions = vec![
                                VkExtensionProperties {
                                    extension_name: [0; 256],
                                    spec_version: 0,
                                };
                                extension_count as usize
                            ];
                            if vk_enumerate_device_extension_props(
                                devices[0],
                                ptr::null(),
                                &mut extension_count,
                                extensions.as_mut_ptr() as *mut std::ffi::c_void,
                            ) == 0
                            {
                                let names: Vec<String> = extensions
                                    .iter()
                                    .map(|ext| {
                                        let c_str =
                                            std::ffi::CStr::from_ptr(ext.extension_name.as_ptr());
                                        c_str.to_string_lossy().into_owned()
                                    })
                                    .collect();
                                extensions_str = names.join(",");
                            }
                        }
                    }

                    vk_destroy_instance(instance, ptr::null());
                    return format!(
                        "{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}",
                        format_version(api_version),
                        format_version(driver_version),
                        format_device_type(device_type),
                        extension_count,
                        extensions_str,
                        max_image_1d,
                        max_image_2d,
                        max_image_3d,
                        max_image_cube,
                        max_image_array_layers,
                        max_uniform_buffer_range,
                        max_storage_buffer_range,
                        max_sampler_anisotropy,
                        max_color_samples,
                        max_depth_samples,
                        inst_extension_count,
                        inst_extensions_str,
                        query_instance_version(vk_enumerate_instance_version)
                    );
                }
            }
            if !instance.is_null() {
                vk_destroy_instance(instance, ptr::null());
            }
        }

        format!(
            "Unknown|Unknown|Unknown|0||0|0|0|0|0|0|0|0|0|0|{}|{}|{}",
            inst_extension_count,
            inst_extensions_str,
            query_instance_version(vk_enumerate_instance_version)
        )
    }
}

fn format_device_type(device_type: u32) -> String {
    match device_type {
        1 => "Integrated GPU".to_string(),
        2 => "Discrete GPU".to_string(),
        3 => "Virtual GPU".to_string(),
        4 => "CPU".to_string(),
        _ => "Other".to_string(),
    }
}

unsafe fn query_instance_version(func: Option<extern "system" fn(*mut u32) -> i32>) -> String {
    if let Some(vk_enumerate_instance_version) = func {
        let mut version: u32 = 0;
        if vk_enumerate_instance_version(&mut version) == 0 {
            return format_version(version);
        }
    }
    "1.0.0".to_string()
}

fn format_version(version: u32) -> String {
    let major = (version >> 22) & 0x3FF;
    let minor = (version >> 12) & 0x3FF;
    let patch = version & 0xFFF;
    format!("{}.{}.{}", major, minor, patch)
}
