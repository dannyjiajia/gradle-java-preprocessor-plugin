package antenna.preprocessor.v2.parser;

import antlr.CommonAST;
import antlr.Token;

/**
 * omry 19/02/2007
 */
public class PPLineAST extends CommonAST
{

	private int column, line;

	public void initialize(Token tok)
	{
		super.initialize(tok);
		this.column = tok.getColumn();
		this.line = tok.getLine();
	}

	public int getColumn()
	{
		return this.column;
	}

	public int getLine()
	{
		return this.line;
	}

	public String toString()
	{
		StringBuffer result = new StringBuffer("");

		result.append(super.toString()).append("[").append(getLine()).append(":").append(getColumn()).append(" - ")
				.append(this.getClass().getName()).append("(").append(getType()).append(")] : ").append(getText());

		return result.toString();
	}

}
