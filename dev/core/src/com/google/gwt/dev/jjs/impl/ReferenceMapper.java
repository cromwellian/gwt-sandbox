/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.dev.jjs.impl;

import com.google.gwt.dev.jjs.InternalCompilerException;
import com.google.gwt.dev.jjs.SourceInfo;
import com.google.gwt.dev.jjs.SourceOrigin;
import com.google.gwt.dev.jjs.ast.AccessModifier;
import com.google.gwt.dev.jjs.ast.JArrayType;
import com.google.gwt.dev.jjs.ast.JClassType;
import com.google.gwt.dev.jjs.ast.JConstructor;
import com.google.gwt.dev.jjs.ast.JDeclaredType;
import com.google.gwt.dev.jjs.ast.JEnumType;
import com.google.gwt.dev.jjs.ast.JField;
import com.google.gwt.dev.jjs.ast.JInterfaceType;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JNullType;
import com.google.gwt.dev.jjs.ast.JParameter;
import com.google.gwt.dev.jjs.ast.JPrimitiveType;
import com.google.gwt.dev.jjs.ast.JReferenceType;
import com.google.gwt.dev.jjs.ast.JType;
import com.google.gwt.dev.util.StringInterner;

import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticArgumentBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates unresolved references to types, fields, and methods.
 */
public class ReferenceMapper {

  private final List<String> argNames = new ArrayList<String>();
  private final Map<String, JField> fields = new HashMap<String, JField>();
  private final Map<String, JMethod> methods = new HashMap<String, JMethod>();
  private final Map<String, JField> sourceFields = new HashMap<String, JField>();
  private final Map<String, JMethod> sourceMethods = new HashMap<String, JMethod>();
  private final Map<String, JReferenceType> sourceTypes = new HashMap<String, JReferenceType>();
  private final StringInterner stringInterner = StringInterner.get();
  private final Map<String, JType> types = new HashMap<String, JType>();

  {
    put(JPrimitiveType.BOOLEAN, JPrimitiveType.BYTE, JPrimitiveType.CHAR, JPrimitiveType.DOUBLE,
        JPrimitiveType.FLOAT, JPrimitiveType.INT, JPrimitiveType.LONG, JPrimitiveType.SHORT,
        JPrimitiveType.VOID, JNullType.INSTANCE);
  }

  public void clearSource() {
    sourceFields.clear();
    sourceMethods.clear();
    sourceTypes.clear();
  }

  public JField get(FieldBinding binding) {
    binding = binding.original();
    String key = signature(binding);
    JField sourceField = sourceFields.get(key);
    if (sourceField != null) {
      assert !sourceField.isExternal();
      return sourceField;
    }
    JField field = fields.get(key);
    if (field == null) {
      field = createField(binding);
      assert field.isExternal();
      fields.put(key, field);
    }
    return field;
  }

  public JMethod get(MethodBinding binding) {
    binding = binding.original();
    String key = signature(binding);
    JMethod sourceMethod = sourceMethods.get(key);
    if (sourceMethod != null) {
      assert !sourceMethod.isExternal();
      return sourceMethod;
    }
      if (key.contains(".onClick")) {
          boolean xx = true;
      }
    JMethod method = methods.get(key);
    if (method == null) {
      if (binding.isConstructor()) {
        method = createConstructor(SourceOrigin.UNKNOWN, binding);
      } else {
        method = createMethod(SourceOrigin.UNKNOWN, binding, null);
      }
      assert method.isExternal();
      methods.put(key, method);
    }
    return method;
  }

  public JType get(TypeBinding binding) {
    binding = binding.erasure();
    String key = signature(binding);
    JReferenceType sourceType = sourceTypes.get(key);

    if (key.endsWith("/ClickEvent")) {
        boolean xx = true;
    }
    if (sourceType != null) {
      assert !sourceType.isExternal();
      return sourceType;
    }

    JType type = types.get(key);
    if (type != null) {
      assert type instanceof JPrimitiveType || type == JNullType.INSTANCE || type.isExternal();
      return type;
    }
    assert !(binding instanceof BaseTypeBinding);

    if (binding instanceof ArrayBinding) {
      ArrayBinding arrayBinding = (ArrayBinding) binding;
      JArrayType arrayType = new JArrayType(get(arrayBinding.elementsType()));
      if (arrayType.isExternal()) {
        types.put(key, arrayType);
      } else {
        if (key.endsWith("/ClickEvent")) {
            boolean xx = true;
        }
        sourceTypes.put(key, arrayType);
      }
      return arrayType;
    } else {
      ReferenceBinding refBinding = (ReferenceBinding) binding;
      JDeclaredType declType = createType(refBinding);
      try {
        if (declType instanceof JClassType) {
          ReferenceBinding superclass = refBinding.superclass();
          if (superclass != null && superclass.isValidBinding()) {
            ((JClassType) declType).setSuperClass((JClassType) get(superclass));
          }
        }
        ReferenceBinding[] superInterfaces = refBinding.superInterfaces();
        if (superInterfaces != null) {
          for (ReferenceBinding intf : superInterfaces) {
            if (intf.isValidBinding()) {
              declType.addImplements((JInterfaceType) get(intf));
            }
          }
        }
      } catch (AbortCompilation ignored) {
        /*
         * The currently-compiling unit has no errors; however, we're running
         * into a case where it references something with a bad hierarchy. This
         * doesn't cause an error in the current unit, but it does mean we run
         * into a wall here trying to construct the hierarchy. Catch the error
         * so that compilation can proceed; the error units themselves will
         * eventually cause the full compile to error out.
         */
      }
      // Emulate clinit method for super clinit calls.
      JMethod clinit =
          new JMethod(SourceOrigin.UNKNOWN, "$clinit", declType, JPrimitiveType.VOID, false, true,
              true, AccessModifier.PRIVATE);
      clinit.freezeParamTypes();
      clinit.setSynthetic();
      declType.addMethod(clinit);
      declType.setExternal(true);
      if (key.endsWith("/ClickEvent")) {
          boolean xx = true;
      }
      types.put(key, declType);
      return declType;
    }
  }

  public void setField(FieldBinding binding, JField field) {
    String key = signature(binding);
    if (!sourceFields.containsKey(key)) {
      sourceFields.put(key, field);
    }
  }

  public void setMethod(MethodBinding binding, JMethod method) {
    String key = signature(binding);
    if (!sourceMethods.containsKey(key)) {
      sourceMethods.put(key, method);
    }
  }

  public void setSourceType(SourceTypeBinding binding, JDeclaredType type) {
    String key = signature(binding);
    if (key.endsWith("/ClickEvent")) {
        boolean xx = true;
    }
    if (!sourceTypes.containsKey(key)) {
        sourceTypes.put(key, type);
    }
  }

  JMethod createConstructor(SourceInfo info, MethodBinding b) {
    JDeclaredType enclosingType = (JDeclaredType) get(b.declaringClass);
    JMethod method = new JConstructor(info, (JClassType) enclosingType);
    enclosingType.addMethod(method);

    /*
     * Don't need to synthesize enum intrinsic args because enum ctors can only
     * be called locally.
     */

    int argPosition = 0;

    ReferenceBinding declaringClass = b.declaringClass;
    if (declaringClass.isNestedType() && !declaringClass.isStatic()) {
      // add synthetic args for outer this
      if (declaringClass.syntheticEnclosingInstanceTypes() != null) {
        for (ReferenceBinding argType : declaringClass.syntheticEnclosingInstanceTypes()) {
          createParameter(info, argType, method, argPosition++);
        }
      }
    }

    // User args.
    argPosition = mapParameters(info, method, b, argPosition);

    if (declaringClass.isNestedType() && !declaringClass.isStatic()) {
      // add synthetic args for locals
      if (declaringClass.syntheticOuterLocalVariables() != null) {
        for (SyntheticArgumentBinding arg : declaringClass.syntheticOuterLocalVariables()) {
          createParameter(info, arg.type, method, argPosition++);
        }
      }
    }

    mapExceptions(method, b);
    if (b.isSynthetic()) {
      method.setSynthetic();
    }
    return method;
  }

  JMethod createMethod(SourceInfo info, MethodBinding b, String[] paramNames) {
    JDeclaredType enclosingType = (JDeclaredType) get(b.declaringClass);
    JMethod method =
        new JMethod(info, intern(b.selector), enclosingType, get(b.returnType), b.isAbstract(), b
            .isStatic(), b.isFinal(), AccessModifier.fromMethodBinding(b));
    enclosingType.addMethod(method);
    if (paramNames == null) {
      mapParameters(info, method, b, 0);
    } else {
      mapParameters(info, method, b, paramNames);
    }
    mapExceptions(method, b);
    if (b.isSynthetic()) {
      method.setSynthetic();
    }
    return method;
  }

  private JField createField(FieldBinding binding) {
    JDeclaredType enclosingType = (JDeclaredType) get(binding.declaringClass);
    JField field =
        new JField(SourceOrigin.UNKNOWN, intern(binding.name), enclosingType, get(binding.type),
            binding.isStatic(), GwtAstBuilder.getFieldDisposition(binding));
    enclosingType.addField(field);
    return field;
  }

  private JParameter createParameter(SourceInfo info, TypeBinding paramType,
      JMethod enclosingMethod, int argPosition) {
    JType type = get(paramType);
    ensureArgNames(argPosition);
    JParameter param =
        new JParameter(info, argNames.get(argPosition), type, true, false, enclosingMethod);
    enclosingMethod.addParam(param);
    return param;
  }

  private JParameter createParameter(SourceInfo info, TypeBinding paramType,
      JMethod enclosingMethod, String name) {
    JParameter param = new JParameter(info, name, get(paramType), true, false, enclosingMethod);
    enclosingMethod.addParam(param);
    return param;
  }

  private JDeclaredType createType(ReferenceBinding binding) {
    String name = GwtAstBuilder.dotify(binding.compoundName);
    SourceInfo info = SourceOrigin.UNKNOWN;
    if (binding.isClass()) {
      return new JClassType(info, name, binding.isAbstract(), binding.isFinal());
    } else if (binding.isInterface() || binding.isAnnotationType()) {
      return new JInterfaceType(info, name);
    } else if (binding.isEnum()) {
      if (binding.isAnonymousType()) {
        // Don't model an enum subclass as a JEnumType.
        return new JClassType(info, name, false, true);
      } else {
        return new JEnumType(info, name, binding.isAbstract());
      }
    } else {
      throw new InternalCompilerException("ReferenceBinding is not a class, interface, or enum.");
    }
  }

  private void ensureArgNames(int required) {
    for (int i = argNames.size(); i <= required; ++i) {
      argNames.add(intern("arg" + i));
    }
  }

  private String intern(char[] cs) {
    return intern(String.valueOf(cs));
  }

  private String intern(String s) {
    return stringInterner.intern(s);
  }

  private void mapExceptions(JMethod method, MethodBinding binding) {
    for (ReferenceBinding thrownBinding : binding.thrownExceptions) {
      JClassType type = (JClassType) get(thrownBinding);
      method.addThrownException(type);
    }
  }

  private int mapParameters(SourceInfo info, JMethod method, MethodBinding binding, int argPosition) {
    if (binding.parameters != null) {
      ensureArgNames(argPosition + binding.parameters.length);
      for (TypeBinding argType : binding.parameters) {
        createParameter(info, argType, method, argNames.get(argPosition++));
      }
    }
    method.freezeParamTypes();
    return argPosition;
  }

  private void mapParameters(SourceInfo info, JMethod method, MethodBinding binding,
      String[] paramNames) {
    if (binding.parameters != null) {
      int i = 0;
      for (TypeBinding argType : binding.parameters) {
        createParameter(info, argType, method, paramNames[i++]);
      }
    }
    method.freezeParamTypes();
  }

  private void put(JType... baseTypes) {
    for (JType type : baseTypes) {
      types.put(type.getName(), type);
    }
  }

  private String signature(FieldBinding binding) {
    StringBuilder sb = new StringBuilder();
    sb.append(binding.declaringClass.constantPoolName());
    sb.append('.');
    sb.append(binding.name);
    sb.append(':');
    sb.append(binding.type.signature());
    return sb.toString();
  }

  private String signature(MethodBinding binding) {
    StringBuilder sb = new StringBuilder();
    sb.append(binding.declaringClass.constantPoolName());
    sb.append('.');
    sb.append(binding.selector);
    sb.append('(');
    for (TypeBinding paramType : binding.parameters) {
      sb.append(paramType.signature());
    }
    sb.append(')');
    sb.append(binding.returnType.signature());
    return sb.toString();
  }

  private String signature(TypeBinding binding) {
    if (binding.isBaseType()) {
      return String.valueOf(binding.sourceName());
    } else {
      return String.valueOf(binding.constantPoolName());
    }
  }
}
