package com.google.gwt.dev.jjs.impl;

import com.google.gwt.dev.jjs.ast.*;
import com.google.gwt.dev.jjs.ast.js.JsniMethodBody;
import com.google.gwt.dev.util.collect.HashSet;

import java.util.*;

/**
 * Copy methods from interface types which have method bodies onto classes which inherit them.
 */
public class MixinDefenderMethods {
    private JProgram program;

    public MixinDefenderMethods(JProgram program) {

        this.program = program;
    }

    public static void exec(JProgram program) {
        new MixinDefenderMethods(program).execImpl();
    }

    private void execImpl() {

        List<JMethod> defenderMethods = new ArrayList<>();
        Map<JMethod, JMethod> virtual2static = new HashMap<>();
        MakeCallsStatic.CreateStaticImplsVisitor staticImplsVisitor =
                new MakeCallsStatic.CreateStaticImplsVisitor(program);
nextIntf:  for (JDeclaredType type : program.getDeclaredTypes()) {
            if (type instanceof JInterfaceType) {
               defenderMethods.clear();

               for (JMethod meth : type.getMethods()) {
                  if (isDefender(meth)) {
                      defenderMethods.add(meth);
                  }
               }

               if (!defenderMethods.isEmpty()) {
                   Set<JClassType> implementors = program.typeOracle.getImplementors((JInterfaceType) type);
                   if (implementors == null || implementors.isEmpty()) {
                       continue nextIntf;
                   }
                   for (JMethod dMeth : defenderMethods) {
                       staticImplsVisitor.accept(dMeth);
                       virtual2static.put(dMeth, program.getStaticImpl(dMeth));
                      for (JClassType cType : implementors) {
                          boolean implemented = false;
                          for (JMethod cMeth : cType.getMethods()) {
                             if (JTypeOracle.methodsDoMatch(dMeth, cMeth)) {
                                 implemented = true;
                             }
                          }
                          if (!implemented) {
                             JMethod clone = new JMethod(dMeth.getSourceInfo(), dMeth.getName(),
                                     cType, dMeth.getType(), false, false, false,
                                     dMeth.getAccess());
                             clone.addThrownExceptions(dMeth.getThrownExceptions());
                             for (JParameter p : dMeth.getParams()) {
                                 clone.addParam(new JParameter(p.getSourceInfo(), p.getName(), p.getType(), p.isFinal(),
                                         p.isThis(), clone));
                             }
                             JMethodBody body = new JMethodBody(dMeth.getSourceInfo());
                             JMethodCall delegate = new JMethodCall(dMeth.getSourceInfo(),
                                     null, virtual2static.get(dMeth));
                             delegate.addArg(new JThisRef(dMeth.getSourceInfo(), cType));
                             for (JParameter p : clone.getParams()) {
                                 delegate.addArg(new JParameterRef(p.getSourceInfo(), p));
                             }
                             body.getBlock().addStmt(clone.getType() == JPrimitiveType.VOID ?
                                     delegate.makeStatement() : new JReturnStatement(dMeth.getSourceInfo(), delegate));
                             clone.setBody(body);
                             clone.freezeParamTypes();
                             cType.addMethod(clone);
                             clone.addOverride(dMeth);
                          }
                      }
                   }
               }
            }
        }
        List<JInterfaceType> defendersToRemove = new ArrayList<>();
        for (JInterfaceType intf : defendersToRemove) {
          new DefenderRemoverVisitor().accept(intf);
        }
    }

    private class DefenderRemoverVisitor extends JModVisitor {

        @Override
        public void endVisit(JMethod x, Context ctx) {
            if (isDefender(x)) {
                x.setBody(new JMethodBody(x.getSourceInfo()));
                x.setAbstract(true);
            }
        }
    }

    private static boolean isDefender(JMethod x) {
        JAbstractMethodBody body = x.getBody();
        if (JProgram.isClinit(x)) {
            return false;
        }
        return (body instanceof JMethodBody && !((JMethodBody) body).getStatements().isEmpty()
            || body != null && body.isNative() && ((JsniMethodBody) body).getFunc() != null);
    }
}
