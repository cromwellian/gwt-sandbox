package com.google.gwt.dev.jjs.ast;

import com.google.gwt.dev.jjs.SourceInfo;

/**
 * Created by cromwellian on 9/29/13.
 */
public class JLambdaExpression extends JExpression {
    private final JMethod method;

    public JLambdaExpression(SourceInfo info, JMethod method) {
        super(info);
        this.method = method;
    }

    @Override
    public boolean hasSideEffects() {
        return true;
    }

    @Override
    public JType getType() {
        return method.getType();
    }

    @Override
    public void traverse(JVisitor visitor, Context ctx) {
      visitor.accept(method);
    }
}
