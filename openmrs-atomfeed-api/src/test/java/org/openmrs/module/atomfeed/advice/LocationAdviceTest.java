package org.openmrs.module.atomfeed.advice;

import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.Location;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class, Method.class})
public class LocationAdviceTest {

    @Mock
    private AdministrationService administrationService;
    @Mock
    private PlatformTransactionManager platformTransactionManager;

    @Mock
    private EventService eventService;

    private LocationAdvice locationAdvice;

    @Before
    public void setUp() throws SQLException {
        mockStatic(Context.class);
        AtomFeedSpringTransactionManager transactionManager = new AtomFeedSpringTransactionManager(platformTransactionManager);
        PowerMockito.when(Context.getAdministrationService()).thenReturn(administrationService);
        this.locationAdvice = new LocationAdvice(eventService, transactionManager);
    }

    @Test
    public void shouldCheckNameOfTheMethod() throws Throwable {
        Method method = this.getClass().getMethod("abcd");
        Location returnValue = new Location();
        locationAdvice.afterReturning(returnValue, method, null, null);
        verify(eventService, never()).notify(any(Event.class));
    }


    @Test
    public void shouldRaiseEventForLocation() throws Throwable {
        Method method = this.getClass().getMethod("saveLocation");
        Location returnValue = new Location();
        locationAdvice.afterReturning(returnValue, method, null, null);
        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventService).notify(eventArgumentCaptor.capture());
        Event event = eventArgumentCaptor.getValue();
        assertNotNull(event.getUuid());
        assertEquals("Location", event.getTitle());
        assertEquals("location", event.getCategory());
        assertTrue(event.getContents().matches("/openmrs/ws/rest/v1/location/.*?v=full"));
    }

    @Test
    public void shouldNotRaiseEventForLocationWhenGlobalPropertyIsConfiguredAsFalse() throws Throwable {
        Method method = this.getClass().getMethod("saveLocation");
        Location returnValue = new Location();
        when(administrationService.getGlobalProperty("atomfeed.publish.eventsForLocation")).thenReturn("false");
        locationAdvice.afterReturning(returnValue, method, null, null);
        verify(eventService, never()).notify(any(Event.class));
    }

    @Test
    public void shouldPickUrlTemplateFromGlobalProperty() throws Throwable {
        Method method = this.getClass().getMethod("saveLocation");
        Location returnValue = new Location();
        when(administrationService.getGlobalProperty("atomfeed.publish.urlTemplateForLocation")).thenReturn("/ws/Fhir/{uuid}");

        locationAdvice.afterReturning(returnValue, method, null, null);
        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventService).notify(eventArgumentCaptor.capture());
        Event event = eventArgumentCaptor.getValue();
        assertTrue(event.getContents().matches("/ws/Fhir/.*"));

    }


    public void abcd() {}

    public void saveLocation() {
    }
}