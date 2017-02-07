package org.openmrs.module.atomfeed.advice;

import org.ict4h.atomfeed.server.repository.AllEventRecordsQueue;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsQueueJdbcImpl;
import org.ict4h.atomfeed.server.service.EventService;
import org.ict4h.atomfeed.server.service.EventServiceImpl;
import org.openmrs.api.context.Context;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

public class LocationAdvice extends BaseAdvice {
    private static final String CATEGORY = "location";
    private static final String TITLE = "Location";
    private static final String SAVE_METHOD = "saveLocation";
    private static final String TEMPLATE = "/openmrs/ws/rest/v1/location/{uuid}?v=full";
    private static final String eventRaiseFlagGP = "atomfeed.publish.eventsForLocation";
    private static final String urlTemplateGP = "atomfeed.publish.urlTemplateForLocation";
    private final AtomFeedSpringTransactionManager atomFeedSpringTransactionManager;
    private final EventService eventService;

    public LocationAdvice() {
        atomFeedSpringTransactionManager = new AtomFeedSpringTransactionManager(getSpringPlatformTransactionManager());
        AllEventRecordsQueue allEventRecordsQueue = new AllEventRecordsQueueJdbcImpl(atomFeedSpringTransactionManager);

        this.eventService = new EventServiceImpl(allEventRecordsQueue);
    }

    public LocationAdvice(EventService eventService,AtomFeedSpringTransactionManager transactionManager) {
        this.atomFeedSpringTransactionManager = transactionManager;
        this.eventService = eventService;
    }

    @Override
    protected String getDefaultUrlTemplate() {
        return TEMPLATE;
    }

    @Override
    protected String getMethodName() {
        return SAVE_METHOD;
    }

    @Override
    protected String getEventTitle() {
        return TITLE;
    }

    @Override
    protected String getEventCategory() {
        return CATEGORY;
    }



    @Override
    protected String getEventRaiseFlagGlobalProperty() {
        return eventRaiseFlagGP;
    }

    @Override
    protected EventService getEventService() {
        return eventService;
    }

    @Override
    protected AtomFeedSpringTransactionManager getAFTxManager() {
        return atomFeedSpringTransactionManager;
    }

    @Override
    protected String getUrlTemplateGlobalProperty() {
        return urlTemplateGP;
    }

    private PlatformTransactionManager getSpringPlatformTransactionManager() {
        List<PlatformTransactionManager> platformTransactionManagers = Context.getRegisteredComponents(PlatformTransactionManager.class);
        return platformTransactionManagers.get(0);
    }
}
