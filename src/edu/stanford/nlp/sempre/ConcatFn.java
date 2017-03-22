package edu.stanford.nlp.sempre;

import fig.basic.LispTree;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Takes two strings and returns their concatenation.
 *
 * @author Percy Liang
 */
public class ConcatFn extends SemanticFn {
  String delim;
  boolean reverse;
  boolean append;

  public ConcatFn() { }

  public ConcatFn(String delim) {
    this.delim = delim;
  }

  public void init(LispTree tree) {
    super.init(tree);
    if (tree.children.size() > 2) {
      if (!tree.child(1).value.equals("reverse") && !tree.child(1).value.equals("append"))
        throw new RuntimeException("Illegal ConcatFn option " + tree.child(1).value);
      if (tree.child(1).value.equals("reverse"))
        reverse = true;
      else if (tree.child(1).value.equals("append"))
        append = true;
      delim = tree.child(2).value;
    } else {
      delim = tree.child(1).value;
    }
  }

  public DerivationStream call(Example ex, final Callable c) {
    return new SingleDerivationStream() {
      @Override
      public Derivation createDerivation() {
        StringBuilder out = new StringBuilder();
        
        int size = c.getChildren().size();
        int begin = !reverse ? 0 : size - 1;
        int end = !reverse ? size : -1;
        int delta = !reverse ? 1 : -1;
        for (int i = begin; i != end; i += delta) {
          if (i != begin) out.append(delim);
          String s = c.childStringValue(i);
          Formula f = c.child(i).getFormula();
          if (s == null && f instanceof JoinFormula) {
            JavaExecutor executor = new JavaExecutor();
            try {
              JavaExecutor.Response r = executor.execute(f, ex.context);
              s = r.value.toString();
              Pattern pattern = Pattern.compile("\\(string \"(.*)\"\\)");
              Matcher matcher = pattern.matcher(s);
              if (matcher.find()) s = matcher.group(1);
            } catch (NullPointerException e) {}
          }
          out.append(s);
        }
        if (append)
          out.append(delim);
        
        return new Derivation.Builder()
            .withCallable(c)
            .withStringFormulaFrom(out.toString())
            .createDerivation();
      }
    };
  }
}
