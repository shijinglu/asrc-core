package org.shijinglu.asrc.core;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.shijinglu.lure.Expr;
import org.shijinglu.lure.core.BoolData;
import org.shijinglu.lure.core.DoubleData;
import org.shijinglu.lure.core.IntData;
import org.shijinglu.lure.core.StringData;
import org.shijinglu.lure.extensions.IData;

public class Formula implements IFormula {

    public enum FIELDS {
        KEY("key"),
        VALUE("value"),
        CATEGORY("category"),
        RULE("rule"),
        FALTHROUGH("fallthrough"),
        ACTIONS("action");
        private final String name;

        FIELDS(String name) {
            this.name = name;
        }

        static FIELDS from(String desc) {
            for (FIELDS x : FIELDS.values()) {
                if (x.name.equalsIgnoreCase(desc)) {
                    return x;
                }
            }
            throw new IllegalArgumentException(String.format("'%s' is not a valid FIELDS", desc));
        }
    }

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

        static Category from(String desc) {
            if (desc == null) {
                return NAIVE;
            }
            for (Category c : Category.values()) {
                if (c.desc.equalsIgnoreCase(desc)) {
                    return c;
                }
            }
            throw new IllegalArgumentException(String.format("'%s' is not a valid Category", desc));
        }
    }

    public static class Rule {
        private final Optional<String> ruleStr;
        private final Optional<Expr> expr;

        Rule(String ruleStr) {
            this.ruleStr = Optional.ofNullable(ruleStr);
            this.expr = this.ruleStr.map(Expr::compile);
        }

        /**
         * Call LURE to check if given context satisfies the rule. In case ruleStr is empty, return
         * true.
         */
        boolean match(Map<String, IData> context) {
            return this.expr.map(xp -> xp.eval(context)).orElse(true);
        }

        @Override
        public String toString() {
            return this.ruleStr.orElse("");
        }
    }

    protected final String key;
    protected final Optional<IData> data;
    protected final Rule rule;
    protected final Category category;
    protected final boolean fallthrough;
    protected final Optional<Action> action;
    protected final List<Formula> formulas;

    public String getKey() {
        return key;
    }

    /** Test if current formula is the terminal node of the eval tree. */
    public boolean isLeaf() {
        return formulas.isEmpty();
    }

    /**
     * Constructor for naive allocation which is basically a string
     *
     * @param key a string whose config will be overriden
     * @param val data to override
     */
    public Formula(String key, IData val) {
        this.key = key;
        this.rule = new Rule(null);
        this.data = Optional.ofNullable(val);
        this.category = Category.NAIVE;
        this.fallthrough = false;
        this.action = Optional.empty();
        this.formulas = Collections.emptyList();
    }

    public Formula(
            Category category,
            String key,
            Optional<IData> data,
            Rule rule,
            boolean fallthrough,
            Optional<Action> action,
            List<Formula> formulas) {
        this.category = category;
        this.key = key;
        this.data = data;
        this.rule = rule;
        this.fallthrough = fallthrough;
        this.action = action;
        this.formulas = formulas;
    }

    static IData parseData(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return new StringData((String) value);
        }
        if (value instanceof Integer) {
            return new IntData((Integer) value);
        }
        if (value instanceof Number) {
            return new DoubleData(((Number) value).doubleValue());
        }
        if (value instanceof Boolean) {
            return new BoolData((Boolean) value);
        }
        return new StringData(value.toString());
    }

    static Action parseAction(Object action) {
        return action == null ? null : Action.from(action.toString());
    }

    /**
     * Parse {@code Formula} given raw Map (e.g. a JSON object).
     *
     * @param rawObj
     * @return
     */
    public static Optional<Formula> parse(Map rawObj) {
        Object keyObj = rawObj.get(FIELDS.KEY.name);
        if (!(keyObj instanceof String)) {
            throw new IllegalArgumentException(
                    "Failed to parse Formula from '" + rawObj + "': key must be a valid string");
        }

        Object valueObj = rawObj.get(FIELDS.VALUE.name);
        if (valueObj == null) {
            throw new IllegalArgumentException(
                    "Failed to parse Formula from '" + rawObj + "': data must not be null");
        }

        Object ruleObj = rawObj.get(FIELDS.RULE.name);
        Object categoryObj = rawObj.get(FIELDS.CATEGORY.name);
        Object fallthroughObj = rawObj.get(FIELDS.FALTHROUGH.name);
        Object actionObj = rawObj.get(FIELDS.ACTIONS.name);

        Rule rule =
                (ruleObj == null || ruleObj.toString().isEmpty())
                        ? new Rule(null)
                        : new Rule(ruleObj.toString());
        Category category = Category.from((String) categoryObj);
        boolean fallthrough =
                fallthroughObj == null ? false : Boolean.valueOf(fallthroughObj.toString());
        Action action = parseAction(actionObj);

        if (!(valueObj instanceof List)) {
            // leafs, data contains data
            return Optional.of(
                    new Formula(
                            category,
                            (String) keyObj,
                            Optional.ofNullable(parseData(valueObj)),
                            rule,
                            fallthrough,
                            Optional.ofNullable(action),
                            Collections.emptyList()));
        }

        ImmutableList.Builder<Formula> builder = ImmutableList.builder();
        for (Object element : (List) valueObj) {
            Formula.parse((Map) element).ifPresent(builder::add);
        }

        // non-leafs, data needs to be resolved.
        return Optional.of(
                new Formula(
                        category,
                        (String) keyObj,
                        Optional.empty(),
                        rule,
                        fallthrough,
                        Optional.ofNullable(action),
                        builder.build()));
    }

    /**
     * Given context, obtain the corresponding data: - if rule matches, recursively find sub matches
     * and return corresponding data, if no sub match found, return current - if rule does not
     * match, return empty.
     *
     * @return data object if a match is found, none otherwise.
     */
    public Optional<IData> derive(Map<String, IData> context) {
        if (!this.rule.match(context)) {
            return Optional.empty();
        }
        Optional<IData> res = this.data;
        for (Formula formula : this.formulas) {
            res = formula.derive(context);
            res.ifPresent(d -> this.action.ifPresent(ac -> ac.run(this, formula, d)));
            if (res.isPresent() && !formula.fallthrough) {
                return res;
            }
        }
        return res;
    }
}
