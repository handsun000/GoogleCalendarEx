package com.example.googlecalendar.controller;

import java.util.*;
import javax.servlet.http.HttpServletRequest;

import static com.example.googlecalendar.oauth.GoogleAuthorize.*;

import com.example.googlecalendar.oauth.GoogleAuthorize;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar.Events;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Controller
public class GoogleCalendarController {

    private final static Log logger = LogFactory.getLog(GoogleCalendarController.class);
    private static final String APPLICATION_NAME = "";
    private static com.google.api.services.calendar.Calendar client;

//    private Set<Event> events = new HashSet<>();

    final DateTime date1 = new DateTime("2017-05-05T16:30:00.000+05:30");
    final DateTime date2 = new DateTime(new Date());

    Credential credential;

    @Value("${google.client.redirectUri}")
    private String redirectURI;

    @Autowired
    TemplateEngine htmlTemplateEngine;

    @Autowired
    GoogleAuthorize googleAuthorize;

//    public void setEvents(Set<Event> events) {
//        this.events = events;
//    }

    @RequestMapping(value = "/login/google", method = RequestMethod.GET)
    public RedirectView googleConnectionStatus(HttpServletRequest request) throws Exception {
        return new RedirectView(googleAuthorize.authorize());
    }

    @RequestMapping(value = "/login/google", method = RequestMethod.GET, params = "code")
    public ResponseEntity<String> oauth2Callback(@RequestParam(value = "code") String code) {
        com.google.api.services.calendar.model.Events eventList;
        final Context ctx = new Context();

        String message;
        String[] mes = new String[2];

        try {
            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectURI).execute();
            credential = flow.createAndStoreCredential(response, "userID");
            client = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME).build();
            Events events = client.events();
            eventList = events.list("primary").setTimeMin(date1).setTimeMax(date2).execute();
            message = eventList.getSummary();
            for(int i = 0; i<eventList.getItems().get(0).getAttendees().size(); i++) {
                mes[i] = eventList.getItems().get(0).getAttendees().get(i).getEmail();
                ctx.setVariable("attendees"+i, mes[i]);
            }

            ctx.setVariable("name", message);

            System.out.println("My:" + eventList.getItems().get(0).getAttendees().size());
        } catch (Exception e) {
            logger.warn("Exception while handling OAuth2 callback (" + e.getMessage() + ")."
                    + " Redirecting to google connection status page.");
            message = "Exception while handling OAuth2 callback (" + e.getMessage() + ")."
                    + " Redirecting to google connection status page.";
        }
        final String htmlContent = this.htmlTemplateEngine.process("index.html", ctx);

        System.out.println("cal message:" + message);
        return ResponseEntity.ok().body(htmlContent);
    }

//    public Set<Event> getEvents() throws IOException {
//        return this.events;
//    }
}