package com.example.googlecalendar.controller;

import java.io.*;
import java.util.*;

import static com.example.googlecalendar.controller.loginController.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar.Events;
import com.google.api.services.calendar.model.Event;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.spring5.ISpringTemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Controller
public class GoogleCalendarController {

    private final static Log logger = LogFactory.getLog(GoogleCalendarController.class);

    @Autowired
    TemplateEngine htmlTemplateEngine;

    private Set<Event> events = new HashSet<>();

    final DateTime date1 = new DateTime("2017-05-05T16:30:00.000+05:30");
    final DateTime date2 = new DateTime(new Date());

    public void setEvents(Set<Event> events) {
        this.events = events;
    }

    private ISpringTemplateEngine templateEngine(ITemplateResolver templateResolver) {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.addDialect(new Java8TimeDialect());
        engine.setTemplateResolver(templateResolver);
        return engine;
    }

    @RequestMapping(value = "/google", method = RequestMethod.GET)
    public ResponseEntity<String> Calendar() {

        com.google.api.services.calendar.model.Events eventList;
        final Context ctx = new Context();

        String message;

        try {
        Events events = client.events();
        eventList = events.list("primary").setTimeMin(date1).setTimeMax(date2).execute();
        message = eventList.getSummary();
        ctx.setVariable("name", message);
        ctx.setVariable("standardDate", new Date());

        System.out.println("My:" + eventList.getItems());
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

    public Set<Event> getEvents() throws IOException {
        return this.events;
    }
}