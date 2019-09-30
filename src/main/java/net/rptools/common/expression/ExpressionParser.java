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

import net.rptools.common.expression.function.*;
import net.rptools.parser.Expression;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.transform.RegexpStringTransformer;
import net.rptools.parser.transform.StringLiteralTransformer;

public class ExpressionParser {
  private static String[][] DICE_PATTERNS =
      new String[][] {
        // Comments
        new String[] {"//.*", ""},

        // Color hex strings #FFF or #FFFFFF or #FFFFFFFF (with alpha)
        new String[] {
          "(?<![0-9A-Za-z])#([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])(?![0-9A-Za-z])",
          "0x$1$1$2$2$3$3"
        },
        new String[] {
          "(?<![0-9A-Za-z])#([0-9A-Fa-f]{6,6}(?:[0-9A-Fa-f]{2,2})?)(?![0-9A-Za-z])", "0x$1"
        },

        // drop
        new String[] {"\\b(\\d+)[dD](\\d+)[dD](\\d+)\\b", "drop($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[dD](\\d+)\\b", "drop(1, $1, $2)"},

        // drop highest
        new String[] {"\\b(\\d+)[dD](\\d+)[dD][hH](\\d+)\\b", "dropHighest($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[dD][hH](\\d+)\\b", "dropHighest(1, $1, $2)"},

        // keep
        new String[] {"\\b(\\d+)[dD](\\d+)[kK](\\d+)\\b", "keep($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[kK](\\d+)\\b", "keep(1, $1, $2)"},

        // keep lowest
        new String[] {"\\b(\\d+)[dD](\\d+)[kK][lL](\\d+)\\b", "keepLowest($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[kK][lL](\\d+)\\b", "keepLowest(1, $1, $2)"},

        // re-roll
        new String[] {"\\b(\\d+)[dD](\\d+)[rR](\\d+)\\b", "reroll($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[rR](\\d+)\\b", "reroll(1, $1, $2)"},

        // count success
        new String[] {"\\b(\\d+)[dD](\\d+)[sS](\\d+)\\b", "success($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[sS](\\d+)\\b", "success(1, $1, $2)"},

        // count success while exploding
        new String[] {"\\b(\\d+)[dD](\\d+)[eE][sS](\\d+)\\b", "explodingSuccess($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[eE][sS](\\d+)\\b", "explodingSuccess(1, $1, $2)"},
        new String[] {"\\b(\\d+)[eE][sS](\\d+)\\b", "explodingSuccess($1, 6, $2)"},

        // show max while exploding
        new String[] {"\\b(\\d+)[dD](\\d+)[oO]\\b", "openTest($1, $2)"},
        new String[] {"\\b[dD](\\d+)[oO]\\b", "openTest(1, $1)"},
        new String[] {"\\b(\\d+)[oO]\\b", "openTest($1, 6)"},

        // explode
        new String[] {"\\b(\\d+)[dD](\\d+)[eE]\\b", "explode($1, $2)"},
        new String[] {"\\b[dD](\\d+)[eE]\\b", "explode(1, $1)"},

        // hero
        new String[] {"\\b(\\d+[.]\\d+)[dD](\\d+)[hH]\\b", "hero($1, $2)"},
        new String[] {"\\b(\\d+)[dD](\\d+)[hH]\\b", "hero($1, $2)"},
        new String[] {"\\b[dD](\\d+)[hH]\\b", "hero(1, $1)"},
        new String[] {"\\b(\\d+[.]\\d+)[dD](\\d+)[bB]\\b", "herobody($1, $2)"},
        new String[] {"\\b(\\d+)[dD](\\d+)[bB]\\b", "herobody($1, $2)"},
        new String[] {"\\b[dD](\\d+)[bB]\\b", "herobody(1, $1)"},

        // hero killing
        new String[] {"\\b(\\d+[.]\\d+)[dD](\\d+)[hH][kK]([-+]\\d+)\\b", "herokilling($1, $2, $3)"},
        new String[] {"\\b(\\d+[.]\\d+)[dD](\\d+)[hH][kK]\\b", "herokilling($1, $2, 0)"},
        new String[] {"\\b(\\d+)[dD](\\d+)[hH][kK]([-+]\\d+)\\b", "herokilling($1, $2, $3)"},
        new String[] {"\\b(\\d+)[dD](\\d+)[hH][kK]\\b", "herokilling($1, $2, 0)"},
        new String[] {"\\b[dD](\\d+)[hH][kK]([-+]\\d+)\\b", "herokilling(1, $1, $3)"},
        new String[] {"\\b[dD](\\d+)[hH][kK]\\b", "herokilling(1, $1, 0)"},

        // hero killing2
        new String[] {
          "\\b(\\d+[.]\\d+)[dD](\\d+)[hH][kK][2]([-+]\\d+)\\b", "herokilling2($1, $2, $3)"
        },
        new String[] {"\\b(\\d+[.]\\d+)[dD](\\d+)[hH][kK][2]\\b", "herokilling2($1, $2, 0)"},
        new String[] {"\\b(\\d+)[dD](\\d+)[hH][kK][2]([-+]\\d+)\\b", "herokilling2($1, $2, $3)"},
        new String[] {"\\b(\\d+)[dD](\\d+)[hH][kK][2]\\b", "herokilling2($1, $2, 0)"},
        new String[] {"\\b[dD](\\d+)[hH][kK][2]([-+]\\d+)\\b", "herokilling2(1, $1, $3)"},
        new String[] {"\\b[dD](\\d+)[hH][kK][2]\\b", "herokilling2(1, $1, 0)"},

        // hero killing multiplier
        new String[] {"\\b(\\d+)[dD](\\d+)[hH][mM]([-+]\\d+)\\b", "heromultiplier($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[hH][mM]([-+]\\d+)\\b", "heromultiplier(1, $1, $2)"},
        new String[] {"\\b(\\d+)[dD](\\d+)[hH][mM]\\b", "heromultiplier($1, $2, 0)"},
        new String[] {"\\b[dD](\\d+)[hH][mM]\\b", "heromultiplier(1, $1, 0)"},
        new String[] {"\\b(\\d+)[hH][mM]\\b", "heromultiplier(0, 0, $1)"},

        // dice
        new String[] {"\\b(\\d+)[dD](\\d+)\\b", "roll($1, $2)"},
        new String[] {"\\b[dD](\\d+)\\b", "roll(1, $1)"},

        // Fudge dice
        new String[] {"\\b(\\d+)[dD][fF]\\b", "fudge($1)"},
        new String[] {"\\b[dD][fF]\\b", "fudge(1)"},

        // Ubiquity dice
        new String[] {"\\b(\\d+)[dD][uU]\\b", "ubiquity($1)"},
        new String[] {"\\b[dD][uU]\\b", "ubiquity(1)"},

        // Shadowrun 4 Edge or Exploding Test
        new String[] {"\\b(\\d+)[sS][rR]4[eE][gG](\\d+)\\b", "sr4e($1, $2)"},
        new String[] {"\\b(\\d+)[sS][rR]4[eE]\\b", "sr4e($1)"},

        // Shadowrun 4 Normal Test
        new String[] {"\\b(\\d+)[sS][rR]4[gG](\\d+)\\b", "sr4($1, $2)"},
        new String[] {"\\b(\\d+)[sS][rR]4\\b", "sr4($1)"},

        // Subtract X with minimum of Y
        new String[] {
          "\\b(\\d+)[dD](\\d+)[sS](\\d+)[lL](\\d+)\\b", "rollSubWithLower($1, $2, $3, $4)"
        },
        new String[] {"\\b[dD](\\d+)[sS](\\d+)[lL](\\d+)\\b", "rollSubWithLower(1, $1, $2, $3)"},

        // Add X with maximum of Y
        new String[] {
          "\\b(\\d+)[dD](\\d+)[aA](\\d+)[uU](\\d+)\\b", "rollAddWithUpper($1, $2, $3, $4)"
        },
        new String[] {"\\b[dD](\\d+)[aA](\\d+)[uU](\\d+)\\b", "rollAddWithUpper(1, $1, $2, $3)"},

        // Roll with a minimum value per roll (e.g. treat 1s as 2s)
        new String[] {"\\b(\\d+)[dD](\\d+)[lL](\\d+)\\b", "rollWithLower($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[lL](\\d+)\\b", "rollWithLower(1, $1, $2)"},

        // Roll with a maximum value per roll (e.g. treat 6s as 5s)
        new String[] {"\\b(\\d+)[dD](\\d+)[uU](\\d+)\\b", "rollWithUpper($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[uU](\\d+)\\b", "rollWithUpper(1, $1, $2)"},

        // Dragon Quest
        new String[] {"\\b(\\d+)[dD](\\d+)[qQ]#([+-]?\\d+)\\b", "rollAddWithLower($1, $2, $3, 1)"},
        new String[] {"\\b[dD](\\d+)[qQ]#([+-]?\\d+)\\b", "rollAddWithLower(1, $1, $2, 1)"},
        new String[] {"\\b(\\d+)[dD](\\d+)[qQ]\\b", "rollAddWithLower($1, $2, 0, 1)"},
        new String[] {"\\b[dD](\\d+)[qQ]\\b", "rollAddWithLower(1, $1, 0, 1)"}
      };

  private final Parser parser;

  public ExpressionParser() {
    this(DICE_PATTERNS);
  }

  public ExpressionParser(VariableResolver resolver) {
    this(DICE_PATTERNS, resolver);
  }

  public ExpressionParser(String[][] regexpTransforms) {
    this(regexpTransforms, null);
  }

  public ExpressionParser(String[][] regexpTransforms, VariableResolver resolver) {
    parser = new Parser(resolver, true);

    parser.addFunction(new CountSuccessDice());
    parser.addFunction(new DropRoll());
    parser.addFunction(new ExplodeDice());
    parser.addFunction(new KeepRoll());
    parser.addFunction(new RerollDice());
    parser.addFunction(new HeroRoll());
    parser.addFunction(new HeroKillingRoll());
    parser.addFunction(new FudgeRoll());
    parser.addFunction(new UbiquityRoll());
    parser.addFunction(new ShadowRun4Dice());
    parser.addFunction(new ShadowRun4ExplodeDice());
    parser.addFunction(new Roll());
    parser.addFunction(new ExplodingSuccessDice());
    parser.addFunction(new OpenTestDice());
    parser.addFunction(new RollWithBounds());
    parser.addFunction(new DropHighestRoll());
    parser.addFunction(new KeepLowestRoll());

    parser.addFunction(new If());

    StringLiteralTransformer slt = new StringLiteralTransformer();

    parser.addTransformer(slt.getRemoveTransformer());
    parser.addTransformer(new RegexpStringTransformer(regexpTransforms));
    parser.addTransformer(slt.getReplaceTransformer());
  }

  public Parser getParser() {
    return parser;
  }

  public Result evaluate(String expression) throws ParserException {
    Result ret = new Result(expression);
    RunData oldData = RunData.hasCurrent() ? RunData.getCurrent() : null;
    try {
      RunData newRunData = new RunData(ret);
      RunData.setCurrent(newRunData);

      synchronized (parser) {
        Expression xp = parser.parseExpression(expression);
        Expression dxp = xp.getDeterministicExpression();
        ret.setDetailExpression(dxp.format());
        ret.setValue(dxp.evaluate());
        ret.setRolled(newRunData.getRolled());
      }
    } finally {
      RunData.setCurrent(oldData);
    }

    return ret;
  }
}
