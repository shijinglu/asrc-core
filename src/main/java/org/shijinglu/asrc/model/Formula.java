package org.shijinglu.asrc.model;


import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Formula {

    public enum Category {
        NAIVE("naive"),
        SEGMENT("segment"),
        TREATMENT("treatment");

        private final String desc;
        Category(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return desc;
        }
    }


    public abstract static class Rule {
        private final String ruleStr;
        private final IData data;

        protected Rule(String ruleStr, IData data) {
            this.ruleStr = ruleStr;
            this.data = data;
        }

        abstract boolean match(Map<String, IData> context);
    }

    private final Rule rule;
    private final Category category;
    private final String name;
    private final boolean fallthrough;
    private final List<IAction> actions;
    private final List<Formula> formulas;

    /**
     * Constructor for naive allocation which is basically a string
     * @param key a string whose config will be overriden
     * @param val value to override
     */
    public Formula(String key, String val) {
        this.rule = null;
        this.category = Category.NAIVE;
        this.name = null;
        this.fallthrough = false;
        this.actions = Collections.emptyList();
        this.formulas = Collections.emptyList();
    }

    public Formula(Category category, String name, Rule rule, boolean fallthrough, List<IAction> actions, List<Formula> formulas) {
        this.category = category;
        this.name = name;
        this.rule = rule;
        this.fallthrough = fallthrough;
        this.actions = actions;
        this.formulas = formulas;
    }

    /**
     * Given context, obtain the corresponding data.
     * @return null if no matching formula is found.
     */
    public IData derive(Map<String, IData> context) {
        if (this.category == Category.NAIVE) {
            return this.rule.data;
        }
        IData res = null;
        for (Formula al : this.formulas) {
            res = al.derive(context);
            for (IAction action : this.actions) {
                action.run(this, al, res);
            }
            if (res != null && !al.fallthrough) {
                return  res;
            }
        }
        return res;
    }
}
