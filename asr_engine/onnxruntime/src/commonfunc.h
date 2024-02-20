#pragma once 
#include <algorithm>
#ifdef _WIN32
#include <codecvt>
#endif

namespace funasr {
typedef struct
{
    std::string msg;
    std::string stamp;
    std::string stamp_sents;
    std::string tpass_msg;
    float snippet_time;
}FUNASR_RECOG_RESULT;

typedef struct
{
    std::vector<std::vector<int>>* segments;
    float  snippet_time;
}FUNASR_VAD_RESULT;

typedef struct
{
    string msg;
    vector<string> arr_cache;
}FUNASR_PUNC_RESULT;

#ifdef _WIN32

#define ORTSTRING(str) StrToWstr(str)
#define ORTCHAR(str) StrToWstr(str).c_str()

inline std::wstring String2wstring(const std::string& str, const std::string& locale)
{
    typedef std::codecvt_byname<wchar_t, char, std::mbstate_t> F;
    std::wstring_convert<F> strCnv(new F(locale));
    return strCnv.from_bytes(str);
}

inline std::wstring  StrToWstr(std::string str) {
    if (str.length() == 0)
        return L"";
    return  String2wstring(str, "zh-CN");

}

#else

#define ORTSTRING(str) str
#define ORTCHAR(str) str

#endif



inline void GetInputName(Ort::Session* session, string& inputName,int nIndex=0) {
    size_t numInputNodes = session->GetInputCount();
    if (numInputNodes > 0) {
        Ort::AllocatorWithDefaultOptions allocator;
        {
            auto t = session->GetInputNameAllocated(nIndex, allocator);
            inputName = t.get();
        }
    }
}

inline void GetOutputName(Ort::Session* session, string& outputName, int nIndex = 0) {
    size_t numOutputNodes = session->GetOutputCount();
    if (numOutputNodes > 0) {
        Ort::AllocatorWithDefaultOptions allocator;
        {
            auto t = session->GetOutputNameAllocated(nIndex, allocator);
            outputName = t.get();
        }
    }
}

template <class ForwardIterator>
inline static size_t Argmax(ForwardIterator first, ForwardIterator last) {
    return std::distance(first, std::max_element(first, last));
}
} // namespace funasr
