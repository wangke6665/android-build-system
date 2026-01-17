cmake_minimum_required(VERSION 3.18.1)

project("native-lib")

# 添加库
add_library(
    native-lib
    SHARED
    native-lib.c
)

# 链接系统库
find_library(
    log-lib
    log
)

target_link_libraries(
    native-lib
    ${log-lib}
)
