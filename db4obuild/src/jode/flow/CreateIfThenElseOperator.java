/* CreateIfThenElseOperator - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
package jode.flow;
import jode.GlobalOptions;
import jode.expr.CompareUnaryOperator;
import jode.expr.ConstOperator;
import jode.expr.Expression;
import jode.expr.IfThenElseOperator;
import jode.type.Type;

public class CreateIfThenElseOperator
{
    private static boolean createFunnyHelper(FlowBlock flowblock,
					     FlowBlock flowblock_0_,
					     StructuredBlock structuredblock) {
	if (structuredblock instanceof InstructionBlock
	    && !((InstructionBlock) structuredblock).getInstruction().isVoid())
	    return true;
	if (structuredblock instanceof IfThenElseBlock) {
	    IfThenElseBlock ifthenelseblock
		= (IfThenElseBlock) structuredblock;
	    if (ifthenelseblock.elseBlock == null)
		return false;
	    if (!createFunnyHelper(flowblock, flowblock_0_,
				   ifthenelseblock.thenBlock)
		| !createFunnyHelper(flowblock, flowblock_0_,
				     ifthenelseblock.elseBlock))
		return false;
	    if (GlobalOptions.verboseLevel > 0)
		GlobalOptions.err.print('?');
	    Expression expression
		= new IfThenElseOperator(Type.tBoolean).addOperand
		      (((InstructionBlock) ifthenelseblock.elseBlock)
			   .getInstruction())
		      .addOperand
		      (((InstructionBlock) ifthenelseblock.thenBlock)
			   .getInstruction())
		      .addOperand(ifthenelseblock.cond);
	    ((InstructionBlock) ifthenelseblock.thenBlock)
		.setInstruction(expression);
	    ifthenelseblock.thenBlock.moveDefinitions(ifthenelseblock, null);
	    ifthenelseblock.thenBlock.replace(ifthenelseblock);
	    return true;
	}
	if (structuredblock instanceof SequentialBlock
	    && structuredblock.getSubBlocks()[0] instanceof ConditionalBlock
	    && structuredblock.getSubBlocks()[1] instanceof InstructionBlock) {
	    ConditionalBlock conditionalblock
		= (ConditionalBlock) structuredblock.getSubBlocks()[0];
	    InstructionBlock instructionblock
		= (InstructionBlock) structuredblock.getSubBlocks()[1];
	    if (!(instructionblock.getInstruction() instanceof ConstOperator))
		return false;
	    ConstOperator constoperator
		= (ConstOperator) instructionblock.getInstruction();
	    if (conditionalblock.trueBlock.jump.destination == flowblock
		&& constoperator.getValue().equals(new Integer(0))) {
		Expression expression = conditionalblock.getInstruction();
		conditionalblock.flowBlock
		    .removeSuccessor(conditionalblock.trueBlock.jump);
		conditionalblock.trueBlock.removeJump();
		instructionblock.setInstruction(expression);
		instructionblock.moveDefinitions(structuredblock, null);
		instructionblock.replace(structuredblock);
		return true;
	    }
	}
	return false;
    }
    
    public static boolean createFunny(ConditionalBlock conditionalblock,
				      StructuredBlock structuredblock) {
	if (conditionalblock.jump == null
	    || !(conditionalblock.getInstruction()
		 instanceof CompareUnaryOperator)
	    || !(structuredblock.outer instanceof SequentialBlock)
	    || !(structuredblock.outer.getSubBlocks()[0]
		 instanceof IfThenElseBlock))
	    return false;
	CompareUnaryOperator compareunaryoperator
	    = (CompareUnaryOperator) conditionalblock.getInstruction();
	FlowBlock flowblock;
	FlowBlock flowblock_1_;
	if (compareunaryoperator.getOperatorIndex() == 26) {
	    flowblock_1_ = conditionalblock.jump.destination;
	    flowblock = conditionalblock.trueBlock.jump.destination;
	} else if (compareunaryoperator.getOperatorIndex() == 27) {
	    flowblock = conditionalblock.jump.destination;
	    flowblock_1_ = conditionalblock.trueBlock.jump.destination;
	} else
	    return false;
	Expression[] expressions = new Expression[3];
	SequentialBlock sequentialblock
	    = (SequentialBlock) structuredblock.outer;
	return createFunnyHelper(flowblock_1_, flowblock,
				 sequentialblock.subBlocks[0]);
    }
    
    public static boolean create(InstructionContainer instructioncontainer,
				 StructuredBlock structuredblock) {
	if (instructioncontainer.jump == null
	    || !(structuredblock.outer instanceof SequentialBlock))
	    return false;
	SequentialBlock sequentialblock
	    = (SequentialBlock) structuredblock.outer;
	if (!(sequentialblock.subBlocks[0] instanceof IfThenElseBlock))
	    return false;
	IfThenElseBlock ifthenelseblock
	    = (IfThenElseBlock) sequentialblock.subBlocks[0];
	if (!(ifthenelseblock.thenBlock instanceof InstructionBlock)
	    || ifthenelseblock.thenBlock.jump == null
	    || (ifthenelseblock.thenBlock.jump.destination
		!= instructioncontainer.jump.destination)
	    || ifthenelseblock.elseBlock != null)
	    return false;
	InstructionBlock instructionblock
	    = (InstructionBlock) ifthenelseblock.thenBlock;
	Expression expression = instructionblock.getInstruction();
	if (expression.isVoid() || expression.getFreeOperandCount() > 0)
	    return false;
	Expression expression_2_ = instructioncontainer.getInstruction();
	if (expression_2_.isVoid() || expression_2_.getFreeOperandCount() > 0)
	    return false;
	Expression expression_3_ = ifthenelseblock.cond;
	if (GlobalOptions.verboseLevel > 0)
	    GlobalOptions.err.print('?');
	instructionblock.flowBlock.removeSuccessor(instructionblock.jump);
	instructionblock.removeJump();
	IfThenElseOperator ifthenelseoperator
	    = (new IfThenElseOperator
	       (Type.tSuperType(expression.getType())
		    .intersection(Type.tSuperType(expression_2_.getType()))));
	ifthenelseoperator.addOperand(expression_2_);
	ifthenelseoperator.addOperand(expression);
	ifthenelseoperator.addOperand(expression_3_);
	instructioncontainer.setInstruction(ifthenelseoperator);
	instructioncontainer.moveDefinitions(structuredblock.outer,
					     structuredblock);
	structuredblock.replace(structuredblock.outer);
	return true;
    }
}
