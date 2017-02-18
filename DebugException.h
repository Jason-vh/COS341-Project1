#ifndef DEBUG_EXCEPTION_H
#define DEBUG_EXCEPTION_H

#include <exception>
#include <list>
#include <string>
#include <typeinfo>

#define DEBUG_TRACK() DebugState::track(__LINE__, __FILE__)
#define DEBUG_TRACK_INFO(INFO) DebugState::track(__LINE__, __FILE__, INFO)

#define DEXCEPT(CLASS, STRING) throw DebugException<CLASS, __LINE__>(std::string() + STRING)

static char const nulStr[] = "";
struct DebugTrackInfo{
	size_t line;
	char const * file;
	char const * info;
};
class DebugState{
public:
	static void track(size_t line, char const * file, char const * other = nulStr);
	static size_t bufMax;
	static std::list<DebugTrackInfo> trackBuffer;
};


template <class C, int L = 0>
class DebugException : public std::exception{
public:
	DebugException(std::string reason_ = "", size_t trackDepth = -1)
	{
		reason = std::string() + "(" + std::to_string(L) + ")[" + typeid(C).name() + "]{" + reason_ + "}\n";
		std::list<DebugTrackInfo>::iterator it = DebugState::trackBuffer.begin();
		for(size_t i = 0; i < trackDepth && i < DebugState::trackBuffer.size(); i++, it++){
			reason += "trace: (" + std::to_string(it->line) + ")[" + it->file + "]{" + it->info + "}\n";
		}
	}
	char const * what ()const throw(){
		return reason.c_str();
	}
protected:
	std::string reason;
};

#endif