set(target_include_dir ${PROJECT_SOURCE_DIR}/build/include)
set(current_dir ${PROJECT_SOURCE_DIR}/contrib/spdlog)

# head-only库，只需将其拷贝到include目录
execute_process(
    COMMAND cp -r ${current_dir}/include/spdlog ${target_include_dir}
    WORKING_DIRECTORY ${current_dir}
)