// $ANTLR : "APP.g" -> "APPParser.java"$

/**
 * Automatically generated code, do not edit!
 * To modify, make changes to APP.g (ANTLR file).
 */
package antenna.preprocessor.v2.parser;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.collections.AST;
import java.util.Hashtable;
import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;

public class APPParser extends antlr.LLkParser       implements APPLexerTokenTypes
 {

protected APPParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public APPParser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected APPParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public APPParser(TokenStream lexer) {
  this(lexer,1);
}

public APPParser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

	public final void r_boolean() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST r_boolean_AST = null;
		
		switch ( LA(1)) {
		case LITERAL_true:
		{
			AST tmp1_AST = null;
			tmp1_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp1_AST);
			match(LITERAL_true);
			r_boolean_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_false:
		{
			AST tmp2_AST = null;
			tmp2_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp2_AST);
			match(LITERAL_false);
			r_boolean_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = r_boolean_AST;
	}
	
	public final void ident() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST ident_AST = null;
		
		switch ( LA(1)) {
		case SYMBOL:
		{
			AST tmp3_AST = null;
			tmp3_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp3_AST);
			match(SYMBOL);
			ident_AST = (AST)currentAST.root;
			break;
		}
		case STRING:
		{
			AST tmp4_AST = null;
			tmp4_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp4_AST);
			match(STRING);
			ident_AST = (AST)currentAST.root;
			break;
		}
		case NUMBER:
		{
			AST tmp5_AST = null;
			tmp5_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp5_AST);
			match(NUMBER);
			ident_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_true:
		case LITERAL_false:
		{
			r_boolean();
			astFactory.addASTChild(currentAST, returnAST);
			ident_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = ident_AST;
	}
	
	public final void bool() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST bool_AST = null;
		
		switch ( LA(1)) {
		case SYMBOL:
		case NUMBER:
		case STRING:
		case LITERAL_true:
		case LITERAL_false:
		{
			ident();
			astFactory.addASTChild(currentAST, returnAST);
			bool_AST = (AST)currentAST.root;
			break;
		}
		case LPAR:
		{
			match(LPAR);
			expression();
			astFactory.addASTChild(currentAST, returnAST);
			match(RPAR);
			bool_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = bool_AST;
	}
	
	public final void expression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expression_AST = null;
		
		xor_bool();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop2085:
		do {
			if ((LA(1)==OR)) {
				AST tmp8_AST = null;
				tmp8_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp8_AST);
				match(OR);
				xor_bool();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop2085;
			}
			
		} while (true);
		}
		expression_AST = (AST)currentAST.root;
		returnAST = expression_AST;
	}
	
	public final void not_bool() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST not_bool_AST = null;
		
		{
		_loop2073:
		do {
			if ((LA(1)==NOT)) {
				AST tmp9_AST = null;
				tmp9_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp9_AST);
				match(NOT);
			}
			else {
				break _loop2073;
			}
			
		} while (true);
		}
		bool();
		astFactory.addASTChild(currentAST, returnAST);
		not_bool_AST = (AST)currentAST.root;
		returnAST = not_bool_AST;
	}
	
	public final void eq_bool() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST eq_bool_AST = null;
		
		not_bool();
		astFactory.addASTChild(currentAST, returnAST);
		{
		switch ( LA(1)) {
		case AT:
		case EQ:
		case NEQ:
		case LT:
		case GT:
		case LTE:
		case GTE:
		{
			{
			switch ( LA(1)) {
			case AT:
			{
				AST tmp10_AST = null;
				tmp10_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp10_AST);
				match(AT);
				break;
			}
			case EQ:
			{
				AST tmp11_AST = null;
				tmp11_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp11_AST);
				match(EQ);
				break;
			}
			case NEQ:
			{
				AST tmp12_AST = null;
				tmp12_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp12_AST);
				match(NEQ);
				break;
			}
			case LT:
			{
				AST tmp13_AST = null;
				tmp13_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp13_AST);
				match(LT);
				break;
			}
			case GT:
			{
				AST tmp14_AST = null;
				tmp14_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp14_AST);
				match(GT);
				break;
			}
			case LTE:
			{
				AST tmp15_AST = null;
				tmp15_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp15_AST);
				match(LTE);
				break;
			}
			case GTE:
			{
				AST tmp16_AST = null;
				tmp16_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp16_AST);
				match(GTE);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			not_bool();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case EOF:
		case RPAR:
		case EOL:
		case AND:
		case OR:
		case XOR:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		eq_bool_AST = (AST)currentAST.root;
		returnAST = eq_bool_AST;
	}
	
	public final void and_bool() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST and_bool_AST = null;
		
		eq_bool();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop2079:
		do {
			if ((LA(1)==AND)) {
				AST tmp17_AST = null;
				tmp17_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp17_AST);
				match(AND);
				eq_bool();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop2079;
			}
			
		} while (true);
		}
		and_bool_AST = (AST)currentAST.root;
		returnAST = and_bool_AST;
	}
	
	public final void xor_bool() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST xor_bool_AST = null;
		
		and_bool();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop2082:
		do {
			if ((LA(1)==XOR)) {
				AST tmp18_AST = null;
				tmp18_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp18_AST);
				match(XOR);
				and_bool();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop2082;
			}
			
		} while (true);
		}
		xor_bool_AST = (AST)currentAST.root;
		returnAST = xor_bool_AST;
	}
	
	public final void anything() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST anything_AST = null;
		
		{
		_loop2088:
		do {
			if ((_tokenSet_0.member(LA(1)))) {
				AST tmp19_AST = null;
				tmp19_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp19_AST);
				matchNot(EOL);
			}
			else {
				break _loop2088;
			}
			
		} while (true);
		}
		anything_AST = (AST)currentAST.root;
		returnAST = anything_AST;
	}
	
	public final void debug_level() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST debug_level_AST = null;
		
		switch ( LA(1)) {
		case LITERAL_debug:
		{
			AST tmp20_AST = null;
			tmp20_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp20_AST);
			match(LITERAL_debug);
			debug_level_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_info:
		{
			AST tmp21_AST = null;
			tmp21_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp21_AST);
			match(LITERAL_info);
			debug_level_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_warn:
		{
			AST tmp22_AST = null;
			tmp22_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp22_AST);
			match(LITERAL_warn);
			debug_level_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_error:
		{
			AST tmp23_AST = null;
			tmp23_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp23_AST);
			match(LITERAL_error);
			debug_level_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_fatal:
		{
			AST tmp24_AST = null;
			tmp24_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp24_AST);
			match(LITERAL_fatal);
			debug_level_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = debug_level_AST;
	}
	
	public final void line() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST line_AST = null;
		
		{
		{
		boolean synPredMatched2094 = false;
		if (((LA(1)==PREFIX))) {
			int _m2094 = mark();
			synPredMatched2094 = true;
			inputState.guessing++;
			try {
				{
				match(PREFIX);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched2094 = false;
			}
			rewind(_m2094);
inputState.guessing--;
		}
		if ( synPredMatched2094 ) {
			AST tmp25_AST = null;
			tmp25_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp25_AST);
			match(PREFIX);
			{
			switch ( LA(1)) {
			case LITERAL_define:
			case LITERAL_undefine:
			case LITERAL_if:
			case LITERAL_elif:
			case LITERAL_condition:
			{
				{
				switch ( LA(1)) {
				case LITERAL_define:
				{
					AST tmp26_AST = null;
					tmp26_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp26_AST);
					match(LITERAL_define);
					define();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case LITERAL_undefine:
				{
					AST tmp27_AST = null;
					tmp27_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp27_AST);
					match(LITERAL_undefine);
					AST tmp28_AST = null;
					tmp28_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp28_AST);
					match(SYMBOL);
					break;
				}
				case LITERAL_if:
				case LITERAL_elif:
				case LITERAL_condition:
				{
					{
					switch ( LA(1)) {
					case LITERAL_if:
					{
						AST tmp29_AST = null;
						tmp29_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp29_AST);
						match(LITERAL_if);
						break;
					}
					case LITERAL_elif:
					{
						AST tmp30_AST = null;
						tmp30_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp30_AST);
						match(LITERAL_elif);
						break;
					}
					case LITERAL_condition:
					{
						AST tmp31_AST = null;
						tmp31_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp31_AST);
						match(LITERAL_condition);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					expression();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				break;
			}
			case LITERAL_ifdef:
			case LITERAL_ifndef:
			case LITERAL_elifdef:
			case LITERAL_elifndef:
			{
				{
				switch ( LA(1)) {
				case LITERAL_ifdef:
				{
					AST tmp32_AST = null;
					tmp32_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp32_AST);
					match(LITERAL_ifdef);
					break;
				}
				case LITERAL_ifndef:
				{
					AST tmp33_AST = null;
					tmp33_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp33_AST);
					match(LITERAL_ifndef);
					break;
				}
				case LITERAL_elifdef:
				{
					AST tmp34_AST = null;
					tmp34_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp34_AST);
					match(LITERAL_elifdef);
					break;
				}
				case LITERAL_elifndef:
				{
					AST tmp35_AST = null;
					tmp35_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp35_AST);
					match(LITERAL_elifndef);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				AST tmp36_AST = null;
				tmp36_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp36_AST);
				match(SYMBOL);
				break;
			}
			case LITERAL_endif:
			case LITERAL_else:
			case LITERAL_endinclude:
			{
				{
				switch ( LA(1)) {
				case LITERAL_endif:
				{
					AST tmp37_AST = null;
					tmp37_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp37_AST);
					match(LITERAL_endif);
					break;
				}
				case LITERAL_else:
				{
					AST tmp38_AST = null;
					tmp38_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp38_AST);
					match(LITERAL_else);
					break;
				}
				case LITERAL_endinclude:
				{
					AST tmp39_AST = null;
					tmp39_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp39_AST);
					match(LITERAL_endinclude);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				break;
			}
			case LITERAL_include:
			{
				{
				AST tmp40_AST = null;
				tmp40_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp40_AST);
				match(LITERAL_include);
				{
				int _cnt2102=0;
				_loop2102:
				do {
					if ((_tokenSet_0.member(LA(1)))) {
						AST tmp41_AST = null;
						tmp41_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp41_AST);
						matchNot(EOL);
					}
					else {
						if ( _cnt2102>=1 ) { break _loop2102; } else {throw new NoViableAltException(LT(1), getFilename());}
					}
					
					_cnt2102++;
				} while (true);
				}
				}
				break;
			}
			case LITERAL_expand:
			{
				{
				AST tmp42_AST = null;
				tmp42_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp42_AST);
				match(LITERAL_expand);
				{
				int _cnt2105=0;
				_loop2105:
				do {
					if ((_tokenSet_0.member(LA(1)))) {
						AST tmp43_AST = null;
						tmp43_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp43_AST);
						matchNot(EOL);
					}
					else {
						if ( _cnt2105>=1 ) { break _loop2105; } else {throw new NoViableAltException(LT(1), getFilename());}
					}
					
					_cnt2105++;
				} while (true);
				}
				}
				break;
			}
			case LITERAL_debug:
			case LITERAL_mdebug:
			{
				{
				{
				switch ( LA(1)) {
				case LITERAL_debug:
				{
					AST tmp44_AST = null;
					tmp44_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp44_AST);
					match(LITERAL_debug);
					break;
				}
				case LITERAL_mdebug:
				{
					AST tmp45_AST = null;
					tmp45_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp45_AST);
					match(LITERAL_mdebug);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				{
				switch ( LA(1)) {
				case LITERAL_debug:
				case LITERAL_info:
				case LITERAL_warn:
				case LITERAL_error:
				case LITERAL_fatal:
				{
					debug_level();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case EOF:
				case EOL:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				}
				break;
			}
			case LITERAL_enddebug:
			{
				AST tmp46_AST = null;
				tmp46_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp46_AST);
				match(LITERAL_enddebug);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		else if ((_tokenSet_1.member(LA(1)))) {
			anything();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		{
		switch ( LA(1)) {
		case EOL:
		{
			AST tmp47_AST = null;
			tmp47_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp47_AST);
			match(EOL);
			break;
		}
		case EOF:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		}
		line_AST = (AST)currentAST.root;
		returnAST = line_AST;
	}
	
	public final void define() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST define_AST = null;
		
		{
		switch ( LA(1)) {
		case LITERAL_unset:
		case LITERAL_add_if_new:
		{
			define_command();
			astFactory.addASTChild(currentAST, returnAST);
			AST tmp48_AST = null;
			tmp48_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp48_AST);
			match(AT);
			break;
		}
		case SYMBOL:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		AST tmp49_AST = null;
		tmp49_AST = astFactory.create(LT(1));
		astFactory.makeASTRoot(currentAST, tmp49_AST);
		match(SYMBOL);
		{
		switch ( LA(1)) {
		case EQ:
		{
			match(EQ);
			define_value();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case EOF:
		case COMMA:
		case EOL:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		define_AST = (AST)currentAST.root;
		returnAST = define_AST;
	}
	
	public final void define_command() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST define_command_AST = null;
		
		switch ( LA(1)) {
		case LITERAL_unset:
		{
			AST tmp51_AST = null;
			tmp51_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp51_AST);
			match(LITERAL_unset);
			define_command_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_add_if_new:
		{
			AST tmp52_AST = null;
			tmp52_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp52_AST);
			match(LITERAL_add_if_new);
			define_command_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = define_command_AST;
	}
	
	public final void define_value() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST define_value_AST = null;
		
		switch ( LA(1)) {
		case SYMBOL:
		case NUMBER:
		case STRING:
		case LITERAL_true:
		case LITERAL_false:
		{
			ident();
			astFactory.addASTChild(currentAST, returnAST);
			define_value_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_debug:
		case LITERAL_info:
		case LITERAL_warn:
		case LITERAL_error:
		case LITERAL_fatal:
		{
			debug_level();
			astFactory.addASTChild(currentAST, returnAST);
			define_value_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = define_value_AST;
	}
	
	public final void defines() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST defines_AST = null;
		
		switch ( LA(1)) {
		case EOF:
		case EOL:
		{
			{
			switch ( LA(1)) {
			case EOL:
			{
				AST tmp53_AST = null;
				tmp53_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp53_AST);
				match(EOL);
				break;
			}
			case EOF:
			{
				AST tmp54_AST = null;
				tmp54_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp54_AST);
				match(Token.EOF_TYPE);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			defines_AST = (AST)currentAST.root;
			break;
		}
		case SYMBOL:
		case LITERAL_unset:
		case LITERAL_add_if_new:
		{
			define();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop2118:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					define();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop2118;
				}
				
			} while (true);
			}
			{
			switch ( LA(1)) {
			case EOL:
			{
				AST tmp56_AST = null;
				tmp56_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp56_AST);
				match(EOL);
				break;
			}
			case EOF:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			defines_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = defines_AST;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"WS",
		"DOT",
		"COMMA",
		"SEMI",
		"PERCENT",
		"FSLASH",
		"BSLASH",
		"ASLASH",
		"COLON",
		"DIGIT_0",
		"DIGIT_1",
		"CHAR",
		"LPAR",
		"RPAR",
		"BLPAR",
		"BRPAR",
		"EOL",
		"PREFIX",
		"SYMBOL",
		"NUMBER",
		"STRING",
		"NOT",
		"AND",
		"OR",
		"XOR",
		"AT",
		"EQ",
		"NEQ",
		"LT",
		"GT",
		"LTE",
		"GTE",
		"\"true\"",
		"\"false\"",
		"\"debug\"",
		"\"info\"",
		"\"warn\"",
		"\"error\"",
		"\"fatal\"",
		"\"define\"",
		"\"undefine\"",
		"\"if\"",
		"\"elif\"",
		"\"condition\"",
		"\"ifdef\"",
		"\"ifndef\"",
		"\"elifdef\"",
		"\"elifndef\"",
		"\"endif\"",
		"\"else\"",
		"\"endinclude\"",
		"\"include\"",
		"\"expand\"",
		"\"mdebug\"",
		"\"enddebug\"",
		"\"unset\"",
		"\"add_if_new\""
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2305843009212645360L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 2305843009213693938L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	
	}
