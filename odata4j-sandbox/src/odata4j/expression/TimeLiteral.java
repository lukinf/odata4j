package odata4j.expression;

import org.joda.time.LocalTime;

public interface TimeLiteral extends LiteralExpression {

	public abstract LocalTime getValue();
}