package org.sinoc.shell.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class RpcEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context,
                           AnnotatedTypeMetadata metadata) {

        return RpcEnabledCondition.matches();
    }

    public static boolean matches() {
        return HarmonyProperties.DEFAULT.isRpcEnabled();
    }
}
