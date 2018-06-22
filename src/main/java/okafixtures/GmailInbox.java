package okafixtures;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.*;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GmailInbox {

    private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FOLDER = "credentials"; // Directory to store user credentials.
    private static final String OUR_USER = "me";
    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved credentials/ folder.
     */
    private static final List<String> SCOPES = Arrays.asList(GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_READONLY, GmailScopes.GMAIL_METADATA);
    private static final String CLIENT_SECRET_DIR = "client_secret.json";
    private static final List<String> HEADERS = Arrays.asList("Subject", "From");
    private final Gmail service;

    public GmailInbox() throws GeneralSecurityException, IOException {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If there is no client_secret.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, okafixtures.AppCredentials.CLIENT_ID, okafixtures.AppCredentials.CLIENT_SECRET, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(CREDENTIALS_FOLDER)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public static boolean clearCredentials() {
        return new File(CREDENTIALS_FOLDER).delete();
    }

    public List<String> listLabels() throws IOException {
        ArrayList<String> returnLabels = new ArrayList<>();
        ListLabelsResponse listResponse = service.users().labels().list(OUR_USER).execute();
        List<Label> labels = listResponse.getLabels();
        if (labels.isEmpty()) {
            System.out.println("No labels found.");
        } else {
            System.out.println("Labels:");
            for (Label label : labels) {
                System.out.printf("- %s\n", label.getName());
                returnLabels.add(label.getName());
            }
        }
        return returnLabels;
    }

    public void getEmailSubjects() throws IOException {
        ListMessagesResponse listResponse = service.users().messages().list(OUR_USER).execute();
        List<Message> messages = listResponse.getMessages();
        if (messages.isEmpty()) {
            System.out.println("no Messages");

        } else {
            System.out.println("Messages:");
            for (Message msg : messages) {
                System.out.println(msg.toPrettyString() + ":");
                Message meta = getMessageMeta(msg);
                if (meta != null) {
                    MessagePart payload = meta.getPayload();
                    if (payload != null) {
                        List<MessagePartHeader> headers = payload.getHeaders();
                        for (MessagePartHeader header : headers) {
                            switch (header.getName()) {
                                case "Subject":
                                case "From":
                                    System.out.println(header.getName() + ":" + header.getValue());
                            }
                        }
                    }
                }
            }
        }
    }

    @Nullable
    private Message getMessageMeta(Message msg) {
        try {
            return service.users().messages().get(OUR_USER, msg.getId()).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getMessageHeader(Message msg, String header) {
        try {
            Message headers = service.users().messages().get(OUR_USER, msg.getId()).setMetadataHeaders(Collections.singletonList(header)).execute();
            MessagePart payload = headers.getPayload();
            if (payload != null) {
                for (MessagePartHeader messageHeader : payload.getHeaders()) {
                    if (messageHeader.getName().equals(header)) {
                        return messageHeader.getValue();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public List<Message> getAllMessages() {
        try {
            ListMessagesResponse listResponse = service.users().messages().list(OUR_USER).execute();
            List<Message> messages = listResponse.getMessages();
            if (messages.isEmpty()) {
                return null;
            }
            return messages;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public List<Message> getMessagesByQuery(String query) {
        try {
            ListMessagesResponse listResponse = service.users().messages().list(OUR_USER).setQ(query).execute();
            List<Message> messages = listResponse.getMessages();
            if (messages.isEmpty()) {
                return null;
            }
            return messages;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public String getMessageSubject(Message msg) {
        return getMessageHeader(msg, "Subject");
    }

    public String getMessageFrom(Message msg) {
        return getMessageHeader(msg, "From");

    }
}

