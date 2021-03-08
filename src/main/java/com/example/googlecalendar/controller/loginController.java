package com.example.googlecalendar.controller;

import com.example.googlecalendar.oauth.GoogleAuthorize;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

import static com.example.googlecalendar.oauth.GoogleAuthorize.flow;
import static com.example.googlecalendar.oauth.GoogleAuthorize.httpTransport;

@Controller
public class loginController {

    private final static Log logger = LogFactory.getLog(GoogleCalendarController.class);
    private static final String APPLICATION_NAME = "";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    public static com.google.api.services.calendar.Calendar client;

    @Autowired
    GoogleAuthorize googleAuthorize;

    Credential credential;

    @Value("${google.client.redirectUri}")
    private String redirectURI;

    @RequestMapping(value = "/login/google", method = RequestMethod.GET)
    public RedirectView googleConnectionStatus(HttpServletRequest request) throws Exception {
        return new RedirectView(googleAuthorize.authorize());
    }

    @RequestMapping(value = "/login/google", method = RequestMethod.GET, params = "code")
    public RedirectView oauth2Callback(@RequestParam(value = "code") String code) {

        try {
            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectURI).execute();
            credential = flow.createAndStoreCredential(response, "userID");
            client = new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME).build();
        } catch (Exception e) {
            logger.warn("Exception while handling OAuth2 callback (" + e.getMessage() + ")."
                    + " Redirecting to google connection status page.");
        }

        return new RedirectView("/google");
    }
}
