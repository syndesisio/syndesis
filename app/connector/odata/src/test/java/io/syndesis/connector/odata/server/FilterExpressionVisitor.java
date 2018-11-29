/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.odata.server;

import java.util.List;
import java.util.Locale;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;

/**
 * Taken from the tutorial:
 * https://olingo.apache.org/doc/odata4/tutorials/read/tutorial_read.html
 */
public class FilterExpressionVisitor implements ExpressionVisitor<Object> {

    private Entity currentEntity;

  public FilterExpressionVisitor(Entity currentEntity) {
    this.currentEntity = currentEntity;
    }

  @Override
  public Object visitMember(final Member member) throws ExpressionVisitException, ODataApplicationException {
    // To keeps things simple, this tutorial allows only primitive properties.
    // We have faith that the java type of Edm.Int32 is Integer

    final List<UriResource> uriResourceParts = member.getResourcePath().getUriResourceParts();

    // Make sure that the resource path of the property contains only a single segment and a primitive property
    // has been addressed. We can be sure, that the property exists because the UriParser checks if the
    // property has been defined in service metadata document.

    if(uriResourceParts.size() == 1 && uriResourceParts.get(0) instanceof UriResourcePrimitiveProperty) {
      UriResourcePrimitiveProperty uriResourceProperty = (UriResourcePrimitiveProperty) uriResourceParts.get(0);
      return currentEntity.getProperty(uriResourceProperty.getProperty().getName()).getValue();
    } else {
      // The OData specification allows in addition complex properties and navigation properties
      // with a target cardinality 0..1 or 1.
      // This means any combination can occur e.g. Supplier/Address/City
      //  -> Navigation properties  Supplier
      //  -> Complex Property       Address
      //  -> Primitive Property     City
      // For such cases the resource path returns a list of UriResourceParts
      throw new ODataApplicationException("Only primitive properties are implemented in filter expressions",
          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }
  }

  @Override
  public Object visitLiteral(Literal literal) throws ExpressionVisitException, ODataApplicationException {
    // To keep this tutorial simple, our filter expression visitor supports only Edm.Int32 and Edm.String
    // In real world scenarios it can be difficult to guess the type of an literal.
    // We can be sure, that the literal is a valid OData literal because the URI Parser checks
    // the lexicographical structure

    // String literals start and end with an single quotation mark
    String literalAsString = literal.getText();
    if(literal.getType() instanceof EdmString) {
      String stringLiteral = "";
      if(literal.getText().length() > 2) {
        stringLiteral = literalAsString.substring(1, literalAsString.length() - 1);
      }

      return stringLiteral;
    } else {
      // Try to convert the literal into an Java Integer
      try {
        return Integer.parseInt(literalAsString);
      } catch(NumberFormatException e) {
        throw new ODataApplicationException("Only Edm.Int32 and Edm.String literals are implemented",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
      }
    }
  }

  @Override
  public Object visitUnaryOperator(UnaryOperatorKind operator, Object operand)
      throws ExpressionVisitException, ODataApplicationException {
    // OData allows two different unary operators. We have to take care, that the type of the operand fits to
    // operand

    if(operator == UnaryOperatorKind.NOT && operand instanceof Boolean) {
      // 1.) boolean negation
      return !(Boolean) operand;
    } else if(operator == UnaryOperatorKind.MINUS && operand instanceof Integer){
      // 2.) arithmetic minus
      return -(Integer) operand;
    }

    // Operation not processed, throw an exception
    throw new ODataApplicationException("Invalid type for unary operator",
        HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
  }

    @Override
    public Object visitBinaryOperator(BinaryOperatorKind operator, Object left, Object right)
        throws ExpressionVisitException, ODataApplicationException {

      // Binary Operators are split up in three different kinds. Up to the kind of the operator it can be applied
      // to different types
      //   - Arithmetic operations like add, minus, modulo, etc. are allowed on numeric types like Edm.Int32
      //   - Logical operations are allowed on numeric types and also Edm.String
      //   - Boolean operations like and, or are allowed on Edm.Boolean
      // A detailed explanation can be found in OData Version 4.0 Part 2: URL Conventions

    if (operator == BinaryOperatorKind.ADD
        || operator == BinaryOperatorKind.MOD
        || operator == BinaryOperatorKind.MUL
        || operator == BinaryOperatorKind.DIV
        || operator == BinaryOperatorKind.SUB) {
      return evaluateArithmeticOperation(operator, left, right);
    } else if (operator == BinaryOperatorKind.EQ
        || operator == BinaryOperatorKind.NE
        || operator == BinaryOperatorKind.GE
        || operator == BinaryOperatorKind.GT
        || operator == BinaryOperatorKind.LE
        || operator == BinaryOperatorKind.LT) {
      return evaluateComparisonOperation(operator, left, right);
    } else if (operator == BinaryOperatorKind.AND
        || operator == BinaryOperatorKind.OR) {
      return evaluateBooleanOperation(operator, left, right);
      } else {
        throw new ODataApplicationException("Binary operation " + operator.name() + " is not implemented",
          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
      }
  }

    private Object evaluateBooleanOperation(BinaryOperatorKind operator, Object left, Object right)
        throws ODataApplicationException {

      // First check that both operands are of type Boolean
      if(left instanceof Boolean && right instanceof Boolean) {
        Boolean valueLeft = (Boolean) left;
        Boolean valueRight = (Boolean) right;

        // Than calculate the result value
        if(operator == BinaryOperatorKind.AND) {
          return valueLeft && valueRight;
        } else {
          // OR
          return valueLeft || valueRight;
        }
      } else {
        throw new ODataApplicationException("Boolean operations needs two numeric operands",
          HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
      }
    }

  private Object evaluateComparisonOperation(BinaryOperatorKind operator, Object left, Object right)
      throws ODataApplicationException {

    // All types in our tutorial supports all logical operations, but we have to make sure that the types are equals
    if(left.getClass().equals(right.getClass()) && left instanceof Comparable) {
      // Luckily all used types String, Boolean and also Integer support the interface Comparable
      int result;
      if(left instanceof Integer) {
        result = ((Comparable<Integer>) (Integer)left).compareTo((Integer) right);
      } else if(left instanceof String) {
        result = ((Comparable<String>) (String)left).compareTo((String) right);
      } else if(left instanceof Boolean) {
        result = ((Comparable<Boolean>) (Boolean)left).compareTo((Boolean) right);
      } else {
        throw new ODataApplicationException("Class " + left.getClass().getCanonicalName() + " not expected",
            HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
      }

      if (operator == BinaryOperatorKind.EQ) {
        return result == 0;
      } else if (operator == BinaryOperatorKind.NE) {
        return result != 0;
      } else if (operator == BinaryOperatorKind.GE) {
        return result >= 0;
      } else if (operator == BinaryOperatorKind.GT) {
        return result > 0;
      } else if (operator == BinaryOperatorKind.LE) {
        return result <= 0;
      } else {
        // BinaryOperatorKind.LT
        return result < 0;
      }

    } else {
      throw new ODataApplicationException("Comparision needs two equal types",
          HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
    }
  }

  private Object evaluateArithmeticOperation(BinaryOperatorKind operator, Object left,
      Object right) throws ODataApplicationException {

      // First check if the type of both operands is numerical
      if(left instanceof Integer && right instanceof Integer) {
        Integer valueLeft = (Integer) left;
        Integer valueRight = (Integer) right;

        // Than calculate the result value
        if(operator == BinaryOperatorKind.ADD) {
          return valueLeft + valueRight;
        } else if(operator == BinaryOperatorKind.SUB) {
          return valueLeft - valueRight;
        } else if(operator == BinaryOperatorKind.MUL) {
          return valueLeft * valueRight;
        } else if(operator == BinaryOperatorKind.DIV) {
          return valueLeft / valueRight;
        } else {
          // BinaryOperatorKind,MOD
          return valueLeft % valueRight;
        }
      } else {
        throw new ODataApplicationException("Arithmetic operations needs two numeric operands",
            HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
      }
  }

  @Override
    public Object visitMethodCall(MethodKind methodCall, List<Object> parameters)
        throws ExpressionVisitException, ODataApplicationException {

    // To keep this tutorial small and simple, we implement only one method call
    if(methodCall == MethodKind.CONTAINS) {
      // "Contains" gets two parameters, both have to be of type String
      // e.g. /Products?$filter=contains(Description, '1024 MB')
      //
      // First the method visistMember is called, which returns the current String value of the property.
      // After that the method visitLiteral is called with the string literal '1024 MB',
      // which returns a String
      //
      // Both String values are passed to visitMethodCall.
      if(parameters.get(0) instanceof String && parameters.get(1) instanceof String) {
        String valueParam1 = (String) parameters.get(0);
        String valueParam2 = (String) parameters.get(1);

        return valueParam1.contains(valueParam2);
      } else {
        throw new ODataApplicationException("Contains needs two parametes of type Edm.String",
            HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
      }
    } else {
      throw new ODataApplicationException("Method call " + methodCall + " not implemented",
          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }
    }

    @Override
    public Object visitTypeLiteral(EdmType type) throws ExpressionVisitException, ODataApplicationException {
      throw new ODataApplicationException("Type literals are not implemented",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

     @Override
      public Object visitAlias(String aliasName) throws ExpressionVisitException, ODataApplicationException {
       throw new ODataApplicationException("Aliases are not implemented",
         HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
     }

      @Override
      public Object visitEnum(EdmEnumType type, List<String> enumValues)
          throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Enums are not implemented",
          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
      }

      @Override
      public Object visitLambdaExpression(String lambdaFunction, String lambdaVariable, Expression expression)
          throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Lamdba expressions are not implemented",
          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
      }

      @Override
      public Object visitLambdaReference(String variableName)
          throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Lamdba references are not implemented",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
      }
}
