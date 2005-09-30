package com.db4o.nativequery.expr;

import com.db4o.nativequery.expr.Expression.ExpressionVisitor;

public class AndExpression implements Expression {
	public interface Visitor extends Expression.ExpressionVisitor {

		void visit(AndExpression expression);

	}

	private Expression _left;
	private Expression _right;
	
	public AndExpression(Expression left, Expression right) {
		this._left = left;
		this._right = right;
	}
		
	public Expression left() {
		return _left;
	}

	public Expression right() {
		return _right;
	}

	public String toString() {
		return "("+_left+")&&("+_right+")";
	}
	
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		AndExpression casted = (AndExpression) other;
		return _left.equals(casted._left)&&(_right.equals(casted._right))||_left.equals(casted._right)&&(_right.equals(casted._left));
	}
	
	public int hashCode() {
		return _left.hashCode()+_right.hashCode();
	}

	public void accept(Expression.ExpressionVisitor visitor) {
		((Visitor)visitor).visit(this);
	}
}
