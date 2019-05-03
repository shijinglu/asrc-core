package org.shijinglu.asrc.core;

import java.util.Map;
import java.util.Optional;
import org.shijinglu.lure.extensions.IData;

public interface IFormula {
    Optional<IData> derive(Map<String, IData> context);
}
