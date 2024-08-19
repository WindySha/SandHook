cmake_minimum_required(VERSION 3.4.1)
project(sandhook)

ENABLE_LANGUAGE(ASM)

add_definitions(-std=c++11)

set(nativehook_lib_path ${PROJECT_SOURCE_DIR}/../../../../nativehook/src/main/cpp)

if(${CMAKE_ANDROID_ARCH_ABI} STREQUAL "arm64-v8a")
    set(OS_DEPENDENDED_SRC
            ${nativehook_lib_path}/archs/arm/arm64/assembler/assembler_arm64.cpp
            ${nativehook_lib_path}/archs/arm/arm64/inst/inst_arm64.cpp
            ${nativehook_lib_path}/archs/arm/arm64/register/register_arm64.cpp
            ${nativehook_lib_path}/archs/arm/arm64/register/register_list_arm64.cpp
            ${nativehook_lib_path}/archs/arm/arm64/decoder/decoder_arm64.cpp
            ${nativehook_lib_path}/archs/arm/arm64/relocate/code_relocate_arm64.cpp
            ${nativehook_lib_path}/archs/arm/arm64/hook/hook_arm64.cpp)

elseif (${CMAKE_ANDROID_ARCH_ABI} STREQUAL "armeabi-v7a")
    set(OS_DEPENDENDED_SRC
            ${nativehook_lib_path}/archs/arm/arm32/inst/inst_arm32.cpp
            ${nativehook_lib_path}/archs/arm/arm32/inst/inst_t32.cpp
            ${nativehook_lib_path}/archs/arm/arm32/inst/inst_t16.cpp
            ${nativehook_lib_path}/archs/arm/arm32/register/register_arm32.cpp
            ${nativehook_lib_path}/archs/arm/arm32/register/register_list_arm32.cpp
            ${nativehook_lib_path}/archs/arm/arm32/assembler/assembler_arm32.cpp
            ${nativehook_lib_path}/archs/arm/arm32/decoder/decoder_arm32.cpp
            ${nativehook_lib_path}/archs/arm/arm32/hook/hook_arm32.cpp
            ${nativehook_lib_path}/archs/arm/arm32/hook/breakpoint_shellcode.S
            ${nativehook_lib_path}/archs/arm/arm32/relocate/code_relocate_arm32.cpp)
endif()

add_library(cydia_substrate STATIC IMPORTED)
set_target_properties(cydia_substrate
        PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/${ANDROID_ABI}/libcydia-substrate.a)

set(${PROJECT_NAME}_SOURCES
        sandhook.cpp
        trampoline/trampoline.cpp
        trampoline/trampoline_manager.cpp
        utils/dlfcn_nougat.cpp
        utils/hide_api.cpp
        utils/classlinker_offset_helper.cpp
        utils/utils.cpp
        utils/offset.cpp
        utils/elf_util.cpp
        casts/cast_art_method.cpp
        casts/cast_compiler_options.cpp
        art/art_method.cpp
        art/art_compiler_options.cpp
        trampoline/arch/arm32.S
        trampoline/arch/arm64.S
        inst/insts_arm32.cpp
        inst/insts_arm64.cpp
        nativehook/native_hook.cpp

        # 将nativehook模块编译到本模块中  added by windysha
        ${nativehook_lib_path}/sandhook_native.cpp
        ${nativehook_lib_path}/decoder/decoder.cpp
        ${nativehook_lib_path}/relocate/code_relocate.cpp
        ${nativehook_lib_path}/elf/elf.cpp
        ${nativehook_lib_path}/assembler/assembler.cpp
        ${nativehook_lib_path}/buffer/code_buffer.cpp
        ${nativehook_lib_path}/utils/platform.cpp
        ${nativehook_lib_path}/hook/hook.cpp
        ${OS_DEPENDENDED_SRC}
        )

# 将nativehook模块编译到本模块中  added by windysha
include_directories(
        ${nativehook_lib_path}/asm
        ${nativehook_lib_path}/decoder
        ${nativehook_lib_path}/elf
        ${nativehook_lib_path}/utils
        ${nativehook_lib_path}/includes
        ${nativehook_lib_path}/buffer
        ${nativehook_lib_path}/archs/arm
        ${nativehook_lib_path}/archs/arm/arm64/inst
        ${nativehook_lib_path}/archs/arm/arm64/register
        ${nativehook_lib_path}/archs/arm/arm64/decoder
        ${nativehook_lib_path}/archs/arm/arm64/assembler
        ${nativehook_lib_path}/archs/arm/arm64/relocate
        ${nativehook_lib_path}/archs/arm/arm64/hook
        ${nativehook_lib_path}/archs/arm/arm32/inst
        ${nativehook_lib_path}/archs/arm/arm32/register
        ${nativehook_lib_path}/archs/arm/arm32/assembler
        ${nativehook_lib_path}/archs/arm/arm32/decoder
        ${nativehook_lib_path}/archs/arm/arm32/hook
        ${nativehook_lib_path}/archs/arm/arm32/relocate
        ${nativehook_lib_path}/src/main/cpp/antihook
)

add_library(${PROJECT_NAME}
        SHARED
        ${${PROJECT_NAME}_SOURCES})

target_link_libraries(${PROJECT_NAME} log cydia_substrate)