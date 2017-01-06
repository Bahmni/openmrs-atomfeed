package org.openmrs.module.atomfeed.advice;

import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsQueueJdbcImpl;
import org.ict4h.atomfeed.server.service.EventService;
import org.ict4h.atomfeed.server.service.EventServiceImpl;
import org.openmrs.api.context.Context;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.SQLException;
import java.util.List;

public class PatientProgramAdvice extends BaseAdvice {
    private static final String CATEGORY = "programenrollment";
    private static final String TITLE = "Progam Enrollment";
    private static final String SAVE_PATIENT_PROGRAM_METHOD = "savePatientProgram";
    private static final String RAISE_PATIENT_PROGRAM_EVENT_GLOBAL_PROPERTY = "atomfeed.publish.eventsForPatientProgramStateChange";
    private static final String PATIENT_PROGRAM_EVENT_URL_PATTERN_GLOBAL_PROPERTY = "atomfeed.event.urlPatternForProgramStateChange";
    private static final String DEFAULT_PATIENT_PROGRAM_URL_PATTERN = "/openmrs/ws/rest/v1/programenrollment/{uuid}?v=full";
    private AtomFeedSpringTransactionManager atomFeedSpringTransactionManager;
    private EventService eventService;
    private final Object eventServiceMonitor = new Object();
    private final Object txManagerMonitor = new Object();

    public PatientProgramAdvice() throws SQLException {

    }

    @Override
    protected String getDefaultUrlTemplate() {
        return DEFAULT_PATIENT_PROGRAM_URL_PATTERN;
    }

    @Override
    protected String getMethodName() {
        return SAVE_PATIENT_PROGRAM_METHOD;
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
        return RAISE_PATIENT_PROGRAM_EVENT_GLOBAL_PROPERTY;
    }

    @Override
    protected EventService getEventService() {
        if (eventService == null) {                // Single Checked
            synchronized (eventServiceMonitor) {
                if (eventService == null) {        // Double checked
                    this.eventService = new EventServiceImpl(new AllEventRecordsQueueJdbcImpl(getAFTxManager()));
                }
            }
        }
        return this.eventService;
    }

    @Override
    protected AtomFeedSpringTransactionManager getAFTxManager() {
        if (this.atomFeedSpringTransactionManager == null) {
            synchronized (txManagerMonitor) {
                if (this.atomFeedSpringTransactionManager == null) {
                    this.atomFeedSpringTransactionManager = new AtomFeedSpringTransactionManager(getSpringPlatformTransactionManager());
                }
            }
        }
        return this.atomFeedSpringTransactionManager;
    }

    @Override
    protected String getUrlTemplateGlobalProperty() {
        return PATIENT_PROGRAM_EVENT_URL_PATTERN_GLOBAL_PROPERTY;
    }

    private PlatformTransactionManager getSpringPlatformTransactionManager() {
        List<PlatformTransactionManager> platformTransactionManagers = Context.getRegisteredComponents(PlatformTransactionManager.class);
        return platformTransactionManagers.get(0);
    }
}
