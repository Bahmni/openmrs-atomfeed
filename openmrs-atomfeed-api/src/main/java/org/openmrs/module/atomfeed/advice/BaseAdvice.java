package org.openmrs.module.atomfeed.advice;

import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventService;
import org.ict4h.atomfeed.transaction.AFTransactionWorkWithoutResult;
import org.joda.time.DateTime;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.UUID;

public abstract class BaseAdvice implements AfterReturningAdvice {

    protected abstract String getDefaultUrlTemplate();

    protected abstract String getMethodName();

    protected abstract String getEventTitle();

    protected abstract String getEventCategory();

    protected abstract String getEventRaiseFlagGlobalProperty();

    protected abstract EventService getEventService();

    protected abstract AtomFeedSpringTransactionManager getAFTxManager();

    protected abstract String getUrlTemplateGlobalProperty();


    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        if (method.getName().equals(getMethodName()) && shouldRaiseEvent()) {

            String uuid = ((OpenmrsObject) returnValue).getUuid();
            String url = getUrlPattern().replace("{uuid}", uuid);
            final Event event = new Event(UUID.randomUUID().toString(), getEventTitle(), DateTime.now(), (URI) null,
                    url, getEventCategory());

            getAFTxManager().executeWithTransaction(
                    new AFTransactionWorkWithoutResult() {
                        @Override
                        protected void doInTransaction() {
                            getEventService().notify(event);
                        }

                        @Override
                        public PropagationDefinition getTxPropagationDefinition() {
                            return PropagationDefinition.PROPAGATION_REQUIRED;
                        }
                    }
            );
        }
    }

    private String getUrlPattern() {
        String urlPattern = Context.getAdministrationService().getGlobalProperty(getUrlTemplateGlobalProperty());
        if (urlPattern == null || urlPattern.equals("")) {
            return getDefaultUrlTemplate();
        }
        return urlPattern;
    }

    private boolean shouldRaiseEvent() {
        String raiseEvent = Context.getAdministrationService().getGlobalProperty(getEventRaiseFlagGlobalProperty());
        return raiseEvent == null || Boolean.valueOf(raiseEvent);
    }

}
