package edu.stanford.nlp.sempre;

import fig.basic.LispTree;
import fig.basic.LogInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Keep tokens that are not markers.
 */
public class FilterMarkerFn extends SemanticFn {
  String mode;

  public void init(LispTree tree) {
    super.init(tree);
    mode = tree.child(1).value;
    if (!mode.equals("accept") && !mode.equals("reject"))
      throw new RuntimeException("Illegal mode for FilterMarkerFn: " + tree.child(1).value);
  }

  public DerivationStream call(final Example ex, final Callable c) {
    return new SingleDerivationStream() {
      @Override
      public Derivation createDerivation() {
        if (isMarker(ex, c) && mode.equals("reject")) {
          return null;
        } else {
          return new Derivation.Builder()
              .withCallable(c)
              .withFormulaFrom(c.child(0))
              .createDerivation();
        }
      }
    };
  }

  private boolean isMarker(Example ex, Callable c) {
    if (c.getEnd() - c.getStart() != 1) return false;
    String token = ex.token(c.getStart());
    return token.length() >= 2 && token.substring(0, 2).equals("r_");
  }
}
