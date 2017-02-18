//some usefull regex info
//http://www.regular-expressions.info/stdregex.html

#include <fstream>
#include <istream>
#include <ostream>
#include <iostream>
#include <string>
#include <vector>
#include <list>
#include <map>
#include <regex>

#include "DebugException.h"

using namespace std;

typedef class Token *(*TokenFactory_fnc)(std::string const & value, size_t line, size_t offset);

class Token{
protected:
	inline static void printDebug(std::string const & c, std::string const & value, size_t line, size_t offset){
		std::cout << c << ": " << value << " (" << line << ":" << offset << ")" << std::endl;
	}
};

class Tokenizer{
public:

	inline Tokenizer()
		:fInit(0)
	{
	}
	inline void init(std::string const & langFilename){
		std::ifstream langStream(langFilename);
		if( ! langStream.is_open() )
			DEXCEPT(Tokenizer, "Tokenizer() | could not open file '" + langFilename + "'");
		std::string tokenInfoLine;

		//load expressions
		while(std::getline(langStream, tokenInfoLine)){
			size_t split = tokenInfoLine.find(": ");
			if(split == -1)
				continue;
			std::string key = tokenInfoLine.substr(0, split);
			tokenWord_map[key] = tokenInfoLine.substr(split + 2);
			tokenOrderOfOperations_vec.push_back(key);
		}

		//test if all bound tokenFactories have a expression
		//and build general expression
		std::string genExpr = "(^$)";//NOTE: use "" to get group count of the current expr by calling regex_search(~null~, match, re)
		int lastGroupIndex = 1;
		tokenFactoryGroup_vec.push_back(0);
		for(auto key : tokenOrderOfOperations_vec){
			string tokenExpr = tokenWord_map[key];
			auto tokenFactory_it = tokenFactory_map.find(key);
			if(tokenFactory_it == tokenFactory_map.end()){
				//DEXCEPT(Tokenizer, "init() | the token key '" + key + "' has no coresponding word definition");
				std::cerr << "Tokenizer::init() | the token key '" + key + "' has is not linked" << std::endl;
				continue;
			}

			genExpr += (genExpr.size() ? string("|") : "" ) + "(" + tokenExpr + ")"; 

			try{
				std::string empty("");
				std::regex regexTmp(genExpr);
				std::smatch match;
				std::regex_search(empty, match, regexTmp);
				for(	; lastGroupIndex < match.size(); lastGroupIndex++){
					tokenFactoryGroup_vec.push_back(tokenFactory_it->second);
				}
			}catch(std::regex_error& e){
				DEXCEPT(Tokenizer, "init() | syntax error in the regular expression at key '" + key + "'");
			}
		}
		genExpr;
		try{
			globalRegex = std::regex(genExpr);
		}catch(std::regex_error& e){
			// something worng in general expr (programming error?)
			DEXCEPT(Tokenizer, "init() | syntax error in the general expression \\" + genExpr + "\\");
		}
		std::cout << "general expression: " << genExpr << std::endl;
		fInit = true;
	}
	inline void bindTokenFactory(std::string const & key, TokenFactory_fnc fnc){
		if(fInit)
			DEXCEPT(Tokenizer, "bindTokenFactory() | binding token factory after initialization");
		tokenFactory_map[key] = fnc;
	}
	inline void build(std::string const & inputFilename){
		if( ! fInit)
			DEXCEPT(Tokenizer, ")() | building before initialization");

		std::string line;
		std::ifstream fileStream(inputFilename);
		if( ! fileStream.is_open() )
			DEXCEPT(Tokenizer, "build() | could not open file '" + inputFilename + "'");

		

		for(size_t lineNum = 1; std::getline(fileStream, line); lineNum++){
			std::string subject(line);
			std::regex regexTmp(globalRegex);
			std::sregex_iterator next(subject.begin(), subject.end(), regexTmp);
			std::sregex_iterator end;
			size_t linCursor = 0;
			while (next != end) {
				std::smatch match = *next;
				//find matching tokenFactory
				for(size_t i = 0; i < tokenFactoryGroup_vec.size(); i++){
					size_t keyIndex = i + 1;//note: regex group 0 is the compleat expr
					if(match[keyIndex].length()){
						std::cout <<  match.position(keyIndex) << " " << linCursor <<  std::endl;
						if(match.position(keyIndex) > linCursor){
							std::cout << "not found: |" << line.substr(linCursor, match.position(keyIndex) - linCursor) << "|" << std::endl ;
						}
						linCursor = match.position(keyIndex) + match.str().size() + 1;
						if(tokenFactoryGroup_vec[keyIndex])
							pToken_list.push_back(tokenFactoryGroup_vec[keyIndex](match.str(), lineNum, 1 + match.position(keyIndex)));
						break;
					}
				}
				next++;
			}
			if(linCursor < line.size()){
				std::cout << "not found: |" << line.substr(linCursor, line.size() - linCursor) << "|" << std::endl ;
			}
		}
	}
protected:
	bool fInit;
	std::vector<std::string> tokenOrderOfOperations_vec;
	std::map<std::string, std::string> tokenWord_map;
	std::map<std::string, TokenFactory_fnc> tokenFactory_map;

	std::vector<TokenFactory_fnc> tokenFactoryGroup_vec;//order added @ initialization

	std::list<Token *> pToken_list;

	std::regex globalRegex;
};

struct Test : public Token{
	inline static Token * Factory(std::string const & value, size_t line, size_t offset){
		printDebug("Test", value, line, offset);
		return new Test;
	}
};
struct Comparison : public Token{
	inline static Token * Factory(std::string const & value, size_t line, size_t offset){
		printDebug("Comparison", value, line, offset);
		return new Comparison;
	}
};
struct BooleanOp : public Token{
	inline static Token * Factory(std::string const & value, size_t line, size_t offset){
		printDebug("BooleanOp", value, line, offset);
		return new BooleanOp;
	}
};
struct Separator : public Token{
	inline static Token * Factory(std::string const & value, size_t line, size_t offset){
		printDebug("Separator", value, line, offset);
		return new Separator;
	}
};
struct ControlStructure : public Token{
	inline static Token * Factory(std::string const & value, size_t line, size_t offset){
		printDebug("ControlStructure", value, line, offset);
		return new ControlStructure;
	}
};
struct Variable : public Token{
	inline static Token * Factory(std::string const & value, size_t line, size_t offset){
		printDebug("Variable", value, line, offset);
		return new Variable;
	}
};

struct ShortString : public Token{
	inline static Token * Factory(std::string const & value, size_t line, size_t offset){
		printDebug("ShortString", value, line, offset);
		return new ShortString;
	}
};

int main(int, char**){
	std::string subject("ac0d abd");
	std::string result;
	try {
	  std::regex re("a(b|c(0|1))d");
	  std::smatch match;
	  if (std::regex_search(subject, match, re) && match.size() > 1) {
		result = match.str(1);
	  } else {
		result = std::string("");
	  } 
	} catch (std::regex_error& e) {
	  // Syntax error in the regular expression
	}
	Tokenizer tokenizer;
	tokenizer.bindTokenFactory("Test", Test::Factory);
	tokenizer.bindTokenFactory("Comparison", Comparison::Factory);
	tokenizer.bindTokenFactory("BooleanOp", BooleanOp::Factory);
	//tokenizer.bindTokenFactory("Separator", Separator::Factory);
	tokenizer.bindTokenFactory("ControlStructure", ControlStructure::Factory);
	tokenizer.bindTokenFactory("Variable", Variable::Factory);
	tokenizer.bindTokenFactory("ShortString", ShortString::Factory);
	tokenizer.init("tokens.txt");
	tokenizer.build("data.txt");
	int foo = 0;
}