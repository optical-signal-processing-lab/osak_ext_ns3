#include "pbstack.h"
#ifdef NS3_LOG_ENABLE

#include <cstdio>
#include <cstdlib>
#include <cxxabi.h>
#include <execinfo.h>
#include <iostream>
#include <memory>
#include <regex>
#endif

std::string
printCallerFunctionName(std::string funcname)
{
#ifdef NS3_LOG_ENABLE
    const int maxFrames = 10;
    void* buffer[maxFrames];
    std::string res;
    // 获取调用栈
    int numFrames = backtrace(buffer, maxFrames);

    // 获取符号名称
    char** symbols = backtrace_symbols(buffer, numFrames);

    if (numFrames > 2 && symbols != nullptr)
    {
        // demangle 函数名
        char* mangledName = nullptr;
        char* offsetBegin = nullptr;
        char* offsetEnd = nullptr;

        // 查找修饰名和偏移量
        for (char* p = symbols[2]; *p; ++p)
        {
            if (*p == '(')
            {
                mangledName = p;
            }
            else if (*p == '+')
            {
                offsetBegin = p;
            }
            else if (*p == ')')
            {
                offsetEnd = p;
                break;
            }
        }

        if (mangledName && offsetBegin && offsetEnd && mangledName < offsetBegin)
        {
            *mangledName++ = '\0';
            *offsetBegin++ = '\0';
            *offsetEnd = '\0';

            int status;
            char* realName = abi::__cxa_demangle(mangledName, 0, 0, &status);

            if (status == 0)
            {
                res = std::string(realName);
                free(realName);
            }
            else
            {
                res = std::string(mangledName);
            }
        }
        else
        {
            res = std::string(symbols[2]);
        }
    }

    free(symbols);
    return res;
#else
    return "";
#endif
}