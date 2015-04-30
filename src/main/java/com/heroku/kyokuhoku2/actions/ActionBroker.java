package com.heroku.kyokuhoku2.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActionBroker extends RouteBuilder {

    @Autowired
    BeanFactory factory;
    Set<Class> actionQueue = Collections.synchronizedSet(new LinkedHashSet<>(Arrays.asList(new Class[]{CacheSeiyuCategoryMemberAction.class})));
    public static final String ENTRY_ENDPOINT = "seda:action.broker.entry";

    @Override
    public void configure() throws Exception {
        from("timer:action.broker.poll?period=10s")
                .filter(new ActionBrokerQueuePredicate())
                .routingSlip(simple("${header.actionEndpoint}"))
                .filter(simple("${header.doneAction}"))
                .to("seda:action.broker.done");

        from(ENTRY_ENDPOINT).process(new ActionBrokerEntryProcessor());

        from("seda:action.broker.done").process(new ActionBrokerDoneProcessor());
    }

    private class ActionBrokerQueuePredicate implements Predicate {

        @Override
        public boolean matches(Exchange exchange) {
            if (actionQueue.isEmpty()) {
                return false;
            } else {
                Iterator<Class> iterator = actionQueue.iterator();
                Class target = null;
                while (iterator.hasNext()) {
                    Class next = iterator.next();
                    Action action = (Action) factory.getBean(next);
                    if (action.isReadyToAction()) {
                        target = next;
                        exchange.getIn().setHeader("actionClass", next);
                        exchange.getIn().setHeader("actionEndpoint", action.defaultActionEndpoint());
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
    }

    private class ActionBrokerEntryProcessor implements Processor {

        @Override
        public void process(Exchange exchange) throws Exception {
            Set<Class> clazzes = exchange.getIn().getBody(Set.class);
            for (Class clazz : clazzes) {
                if (!actionQueue.contains(clazz)) {
                    actionQueue.add(clazz);
                }
            }
        }
    }

    private class ActionBrokerDoneProcessor implements Processor {

        @Override
        public void process(Exchange exchange) throws Exception {
            Class actionClass = exchange.getIn().getHeader("actionClass", Class.class);
            actionQueue.remove(actionClass);
        }
    }
}
