

#include "DebugException.h"




void 
DebugState::track(size_t line, char const * file, char const * other){
	if(trackBuffer.size() >= bufMax){trackBuffer.pop_front();}
	DebugTrackInfo info = {line, file, other};
	trackBuffer.push_back(info);
}

size_t
DebugState::bufMax = 16;

std::list<DebugTrackInfo>
DebugState::trackBuffer = std::list<DebugTrackInfo>();

