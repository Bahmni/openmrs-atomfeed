package org.openmrs.module.atomfeed.advice;

import org.ict4h.atomfeed.server.repository.AllEventRecordsQueue;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsQueueJdbcImpl;
import org.ict4h.atomfeed.server.service.EventService;
import org.ict4h.atomfeed.server.service.EventServiceImpl;
import org.openmrs.api.context.Context;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.SQLException;
import java.util.List;

public class PersonRelationshipAdvice extends BaseAdvice {
    private static final String CATEGORY = "relationship";
    private static final String TITLE = "Relationship";
    private static final String SAVE_RELATIONSHIP_METHOD = "saveRelationship";
    private static final String RAISE_RELATIONSHIP_EVENT_GLOBAL_PROPERTY = "atomfeed.publish.eventsForPatientRelationshipChange";
    private static final String RELATIONSHIP_EVENT_URL_PATTERN_GLOBAL_PROPERTY = "atomfeed.event.urlPatternForPatientRelationshipChange";
    private static final String DEFAULT_RELATIONSHIP_URL_PATTERN = "/openmrs/ws/rest/v1/relationship/%s";
    private final AtomFeedSpringTransactionManager atomFeedSpringTransactionManager;
    private final EventService eventService;

    public PersonRelationshipAdvice() throws SQLException {
        atomFeedSpringTransactionManager = new AtomFeedSpringTransactionManager(getSpringPlatformTransactionManager());
        AllEventRecordsQueue allEventRecordsQueue = new AllEventRecordsQueueJdbcImpl(atomFeedSpringTransactionManager);
        this.eventService = new EventServiceImpl(allEventRecordsQueue);
    }

    @Override
    protected String getDefaultUrlTemplate() {
        return DEFAULT_RELATIONSHIP_URL_PATTERN;
    }

    @Override
    protected String getMethodName() {
        return SAVE_RELATIONSHIP_METHOD;
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
        return RAISE_RELATIONSHIP_EVENT_GLOBAL_PROPERTY;
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
        return RELATIONSHIP_EVENT_URL_PATTERN_GLOBAL_PROPERTY;
    }

    private PlatformTransactionManager getSpringPlatformTransactionManager() {
        List<PlatformTransactionManager> platformTransactionManagers = Context.getRegisteredComponents(PlatformTransactionManager.class);
        return platformTransactionManagers.get(0);
    }
}
