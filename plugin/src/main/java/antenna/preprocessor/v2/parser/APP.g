// include a little do not edit warning at the top of the generated files.
// the warning is obviously not about THIS file.
header
{
/**
 * Automatically generated code, do not edit!
 * To modify, make changes to APP.g (ANTLR file).
 */
package antenna.preprocessor.v2.parser;
}

/**
 * Antenna preprocessor ANTLR lexer and parser
 */
class APPLexer extends Lexer;

options 
{
	k=3;
	testLiterals=false; 
	defaultErrorHandler=false;
}  

WS
  : ( ' '
    | '\t'
    )
    { $setType(Token.SKIP); }
  ;
  
DOT	: ".";
COMMA	: ",";
SEMI	: ";";
PERCENT	: "%";
  
protected FSLASH: '/';
protected BSLASH: '\\';
ASLASH : FSLASH | BSLASH;
protected COLON: ':';
protected DIGIT_0 : '0'..'9';   
protected DIGIT_1 : '1'..'9'; 
protected CHAR : ('a'..'z' | 'A'..'Z');

LPAR:	'(' ;
RPAR:	')' ;
BLPAR:	'[' ;
BRPAR:	']' ;


EOL: ('\n' | '\r' | "\r\n"){ newline();};
  
PREFIX : "//" (WS)* "#";

SYMBOL 
	options{testLiterals=true;}  
	: 
	CHAR (CHAR | DIGIT_0 | "_" | '-' | '+' | '.' | BSLASH | FSLASH | SEMI | COLON)* ;

NUMBER : ('+'|'-')? (DIGIT_0)+ (('.')(DIGIT_0)+)?;
STRING : "\""! (~'"')* "\""! | "'"! (~'\'')* "'"!;

// boolean operands
NOT	: "!";
AND	: "&" ("&")?;
OR	: "|" ("|")?;
XOR	: "^";

AT	: '@'; // "x" @ "a,x" is true.
EQ	: "=" ("=")?;
NEQ	: "!=";
LT	: "<";
GT	: ">";
LTE	: "<=";
GTE	: ">=";

class APPParser extends Parser;
options 
{
	buildAST=true; 	  // Automatically build the AST while parsing
	defaultErrorHandler=false;	
}

r_boolean : "true" | "false";
ident: SYMBOL|STRING|NUMBER|r_boolean; // identifier


bool: ident
	  | LPAR! expression RPAR!
	  ;
	  
	  
not_bool : (NOT^)* bool;

eq_bool :
	not_bool ((AT^|EQ^|NEQ^|LT^|GT^|LTE^|GTE^) not_bool)?;

and_bool : eq_bool (AND^ eq_bool)*;

xor_bool : and_bool (XOR^ and_bool)*;

expression : xor_bool (OR^ xor_bool)*;

anything : (~EOL)*;

debug_level : "debug" | "info" | "warn" | "error" | "fatal";

line :
(
	(
		(PREFIX) => PREFIX 
			(
				(
				"define" define |
				"undefine" SYMBOL | 
				("if" | "elif" | "condition") expression) | 
				("ifdef" | "ifndef" | "elifdef" | "elifndef") SYMBOL | 
				("endif" | "else" | "endinclude")|
				("include" (~EOL)+) /* beware, include is handled using a special hack in the code */|
				("expand" (~EOL)+)  /* beware, define is handled using a special hack in the code  */|
				(("debug"|"mdebug") (debug_level)?) |
				"enddebug"
				)
			| 
			anything
	)
	(EOL)?
);

define_command : "unset" | "add_if_new";
define_value: ident | debug_level;
define : (define_command AT)? SYMBOL^ (EQ! define_value)?;
defines : (EOL|EOF) | define (COMMA! define)* (EOL)?;