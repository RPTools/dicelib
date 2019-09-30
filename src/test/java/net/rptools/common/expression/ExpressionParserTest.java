/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.common.expression;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import net.rptools.parser.ParserException;

public class ExpressionParserTest extends TestCase {

  public void testEvaluate() throws ParserException {
    Result result = new ExpressionParser().evaluate("100+4d1*10");

    assertNotNull(result);
    assertEquals("100+4d1*10", result.getExpression());
    assertEquals("100 + 4 * 10", result.getDetailExpression());
    assertEquals(new BigDecimal(140), (BigDecimal) result.getValue());
  }

  public void testEvaluate_Explode() throws ParserException {
    RunData.setSeed(10423L);
    Result result = new ExpressionParser().evaluate("100+10d6e+1");

    assertEquals(new BigDecimal(164), result.getValue());
  }

  public void testEvaluate_Drop() throws ParserException {
    RunData.setSeed(10423L);
    Result result = new ExpressionParser().evaluate("100+10d6d2+1");

    assertEquals(new BigDecimal(138), result.getValue());
  }

  public void testEvaluate_Keep() throws ParserException {
    RunData.setSeed(10423L);
    Result result = new ExpressionParser().evaluate("100+10d6k8+1");

    assertEquals(new BigDecimal(138), result.getValue());
  }

  public void testEvaluate_CountSuccess() throws ParserException {
    RunData.setSeed(10423L);
    Result result = new ExpressionParser().evaluate("100+10d6s4+1");

    assertEquals(new BigDecimal(109), result.getValue());
  }

  public void testEvaluate_ExplodingSuccess() throws ParserException {
    RunData.setSeed(10423L);
    Result result = new ExpressionParser().evaluate("10d4es6");
    assertEquals("10d4es6", result.getExpression());
    assertEquals("explodingSuccess(10, 4, 6)", result.getDetailExpression());
    assertEquals("Dice: 1, 2, 2, 1, 2, 7, 1, 7, 2, 3, Successes: 2", result.getValue());
    RunData.setSeed(10423L);

    result = new ExpressionParser().evaluate("10es9");
    assertEquals("10es9", result.getExpression());
    assertEquals("explodingSuccess(10, 6, 9)", result.getDetailExpression());
    assertEquals("Dice: 4, 4, 4, 3, 16, 5, 1, 4, 14, 8, Successes: 2", result.getValue());
  }

  public void testEvaluate_OpenTest() throws ParserException {
    RunData.setSeed(10423L);
    Result result = new ExpressionParser().evaluate("10d4o");
    assertEquals("10d4o", result.getExpression());
    assertEquals("openTest(10, 4)", result.getDetailExpression());
    assertEquals("Dice: 1, 2, 2, 1, 2, 7, 1, 7, 2, 3, Maximum: 7", result.getValue());

    RunData.setSeed(10423L);
    result = new ExpressionParser().evaluate("10o");
    assertEquals("10o", result.getExpression());
    assertEquals("openTest(10, 6)", result.getDetailExpression());
    assertEquals("Dice: 4, 4, 4, 3, 16, 5, 1, 4, 14, 8, Maximum: 16", result.getValue());
  }

  public void testEvaluate_SR4Success() throws ParserException {
    RunData.setSeed(10523L);
    Result result = new ExpressionParser().evaluate("5sr4");
    assertEquals("5sr4", result.getExpression());
    assertEquals("sr4(5)", result.getDetailExpression());
    assertEquals("Hits: 1 Ones: 1  Results: 3 1 4 6 3 ", result.getValue());
  }

  public void testEvaluate_SR4GremlinSuccess() throws ParserException {
    RunData.setSeed(10523L);
    Result result = new ExpressionParser().evaluate("5sr4g2");
    assertEquals("5sr4g2", result.getExpression());
    assertEquals("sr4(5, 2)", result.getDetailExpression());
    assertEquals("Hits: 1 Ones: 1 *Glitch*  Results: 3 1 4 6 3 ", result.getValue());
  }

  public void testEvaluate_SR4ExplodingSuccess() throws ParserException {
    RunData.setSeed(10523L);
    Result result = new ExpressionParser().evaluate("5sr4e");
    assertEquals("5sr4e", result.getExpression());
    assertEquals("sr4e(5)", result.getDetailExpression());
    assertEquals("Hits: 1 Ones: 2  Results: 3 1 4 6 3 1 ", result.getValue());
  }

  public void testEvaluate_SR4ExplodingGremlinSuccess() throws ParserException {
    RunData.setSeed(10523L);
    Result result = new ExpressionParser().evaluate("5sr4eg2");
    assertEquals("5sr4eg2", result.getExpression());
    assertEquals("sr4e(5, 2)", result.getDetailExpression());
    assertEquals("Hits: 1 Ones: 2 *Glitch*  Results: 3 1 4 6 3 1 ", result.getValue());
  }

  public void testEvaluate_HeroRoll() throws ParserException {
    RunData.setSeed(10423L);
    ExpressionParser parser = new ExpressionParser();

    Result result = parser.evaluate("4.5d6h");
    assertEquals(new BigDecimal(18), result.getValue());

    result = parser.evaluate("4.5d6b");
    assertEquals(new BigDecimal(5), result.getValue());

    RunData.setSeed(10423L);
    parser = new ExpressionParser();

    result = parser.evaluate("4d6h");
    assertEquals(new BigDecimal(15), result.getValue());

    result = parser.evaluate("4d6b");
    assertEquals(new BigDecimal(4), result.getValue());
  }

  public void testEvaluate_FudgeRoll() throws ParserException {
    RunData.setSeed(10423L);
    ExpressionParser parser = new ExpressionParser();

    Result result = parser.evaluate("dF");
    assertEquals(new BigDecimal(-1), result.getValue());

    result = parser.evaluate("4df");
    assertEquals(new BigDecimal(0), result.getValue());

    // Don't parse df in the middle of things
    parser.getParser().getVariableResolver().setVariable("asdfg", new BigDecimal(10));
    result = parser.evaluate("asdfg");
    assertEquals(new BigDecimal(10), result.getValue());
  }

  public void testEvaluate_UbiquityRoll() throws ParserException {
    RunData.setSeed(10423L);
    ExpressionParser parser = new ExpressionParser();

    Result result = parser.evaluate("dU");
    assertEquals(new BigDecimal(0), result.getValue());

    result = parser.evaluate("10du");
    assertEquals(new BigDecimal(4), result.getValue());

    // Don't parse a uf in the middle of other things
    parser.getParser().getVariableResolver().setVariable("asufg", new BigDecimal(10));
    result = parser.evaluate("asufg");
    assertEquals(new BigDecimal(10), result.getValue());
  }

  public void testEvaluate_ColorHex() throws ParserException {
    RunData.setSeed(10423L);
    ExpressionParser parser = new ExpressionParser();

    Result result = parser.evaluate("#FF0000");
    assertEquals(new BigDecimal(new BigInteger("FF0000", 16)), result.getValue());

    result = parser.evaluate("#00FF0000");
    assertEquals(new BigDecimal(new BigInteger("FF0000", 16)), result.getValue());

    result = parser.evaluate("#FF0");
    assertEquals(new BigDecimal(new BigInteger("FFFF00", 16)), result.getValue());
  }

  public void testEvaluate_If() throws ParserException {
    ExpressionParser parser = new ExpressionParser();

    evaluateExpression(parser, "if(10 > 2, 10, 2)", new BigDecimal(10));
    evaluateExpression(parser, "if(10 < 2, 10, 2)", new BigDecimal(2));
    evaluateStringExpression(parser, "if(10 < 2, 's1', 's2')", "s2");
    evaluateStringExpression(parser, "if(10 > 2, 's1', 's2')", "s1");
  }

  public void testEvaluate_Multiline() throws ParserException {
    RunData.setSeed(10423L);
    ExpressionParser parser = new ExpressionParser();

    evaluateExpression(parser, "10 + \r\n d6 + \n 2", new BigDecimal(16));

    String s = "10 + // Constant expression\n" + "2 + // Another bit\n" + "d20 // The roll\n";

    evaluateExpression(parser, s, new BigDecimal(26));
  }

  public void testMultilineRegex() {
    String str1 = "one two three";
    String str2 = "one two\nthree";

    Pattern p1 = Pattern.compile("^one(.*)three$");
    Pattern p2 = Pattern.compile("one(.*)three", Pattern.MULTILINE);

    Matcher m1 = p1.matcher(str1);
    Matcher m2 = p2.matcher(str2);

    System.out.println(m1.matches());
    System.out.println(m2.matches());
  }

  public void testNoTransformInStrings() throws ParserException {
    ExpressionParser parser = new ExpressionParser();

    evaluateStringExpression(parser, "'10' + 'd10'", "10d10");
  }

  public void testVariableRegexOverlaps() throws ParserException {
    ExpressionParser parser = new ExpressionParser();
    parser.getParser().setVariable("food10", new BigDecimal(10));

    evaluateExpression(parser, "food10 + 10", new BigDecimal(20));
  }

  private void evaluateExpression(ExpressionParser p, String expression, BigDecimal answer)
      throws ParserException {
    Result result = p.evaluate(expression);
    assertTrue(
        String.format(
            "%s evaluated incorrectly expected <%s> but was <%s>",
            expression, answer, result.getValue()),
        answer.compareTo((BigDecimal) result.getValue()) == 0);
  }

  private void evaluateStringExpression(ExpressionParser p, String expression, String answer)
      throws ParserException {
    Result result = p.evaluate(expression);

    assertEquals(answer, result.getValue());
  }
}
