package com.heroku.kyokuhoku2.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.apache.camel.Body;
import org.apache.camel.Header;
import org.apache.camel.Headers;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActionBroker extends RouteBuilder {

    @Autowired
    @Getter
    BeanFactory factory;
    private final Set<Class> actionQueue = Collections.synchronizedSet(new LinkedHashSet<>(Arrays.asList(new Class[]{CacheSeiyuCategoryMemberAction.class})));
    public static final String ENTRY_ENDPOINT = "seda:action.broker.entry";
    public String timerEndpoint = "timer:action.broker.poll?period=10s";
    public String doneEndpoint = "seda:action.broker.done";

    @Override
    public void configure() throws Exception {
        from(ENTRY_ENDPOINT).bean(this, "entryActionQueue");

        from(timerEndpoint)
                .filter().method(this, "hasActionQueue")
                .routingSlip(simple("${header.actionEndpoint}"))
                .filter(simple("${header.doneAction}"))
                .to(doneEndpoint);

        from(doneEndpoint).bean(this, "doneActionQueue");
    }

    public boolean hasActionQueue(@Headers Map headers) {
        if (actionQueue.isEmpty()) {
            return false;
        } else {
            Iterator<Class> iterator = actionQueue.iterator();
            Class target = null;
            while (iterator.hasNext()) {
                Class next = iterator.next();
                Action action = (Action) getFactory().getBean(next);
                if (action.isReadyToAction()) {
                    target = next;
                    headers.put("actionClass", next);
                    headers.put("actionEndpoint", action.getDefaultActionEndpoint());
                    break;
                }
            }
            if (target != null) {
                actionQueue.remove(target);
                actionQueue.add(target);
                return true;
            }
            return false;
        }
    }

    public void entryActionQueue(@Body Set<Class> clazzes) {
        for (Class clazz : clazzes) {
            if (!actionQueue.contains(clazz)) {
                actionQueue.add(clazz);
            }
        }
    }

    public void doneActionQueue(@Header(value = "actionClass") Class actionClass) {
        actionQueue.remove(actionClass);
    }
}
