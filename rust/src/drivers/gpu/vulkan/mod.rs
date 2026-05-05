use libc::{RTLD_NOW, dlopen, dlsym};
use std::ptr;

type VkInstance = *mut std::ffi::c_void;
type VkPhysicalDevice = *mut std::ffi::c_void;
type VkResult = i32;

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
                    // apiVersion is the first 4 bytes. We provide a buffer to avoid stack corruption.
                    let mut properties_buffer = [0u8; 1024];
                    vk_get_physical_device_properties(devices[0], properties_buffer.as_mut_ptr());

                    // Extract apiVersion (first 4 bytes, little endian)
                    let api_version = u32::from_le_bytes([
                        properties_buffer[0],
                        properties_buffer[1],
                        properties_buffer[2],
                        properties_buffer[3],
                    ]);

                    vk_destroy_instance(instance, ptr::null());
                    return format_version(api_version);
                }
            }
            if !instance.is_null() {
                vk_destroy_instance(instance, ptr::null());
            }
        }

        query_instance_version(vk_enumerate_instance_version)
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
    let major = (version >> 22) & 0x7F;
    let minor = (version >> 12) & 0x3FF;
    let patch = version & 0xFFF;
    format!("{}.{}.{}", major, minor, patch)
}
