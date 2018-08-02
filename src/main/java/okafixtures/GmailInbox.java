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
import com.sun.istack.internal.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

public class GmailInbox {

    private static final Logger l = LoggerFactory.getLogger(GmailInbox.class);

    private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FOLDER = "credentials"; // Directory to store user credentials.
    private static final String OUR_USER = "me";
    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved credentials/ folder.
     */
    private static final List<String> SCOPES = Arrays.asList(GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_READONLY, GmailScopes.GMAIL_COMPOSE);
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

    public void createLabel(String labelName) {
        try {
            Label newLabel = new Label().setName(labelName)
                    .setLabelListVisibility("labelShow")
                    .setMessageListVisibility("show");
            service.users().labels().create(OUR_USER, newLabel).execute();
        } catch (IOException e) {
            l.error("Failed to create new label", e);
        }
    }

    public void deleteLabel(String labelName) {
        try {
            List<Label> labels = getRawLabels();
            if (labels.isEmpty()) {
                l.warn("No labels found.");
            } else {
                for (Label label : labels) {
                    if (label.getName().equals(labelName)) {
                        service.users().labels().delete(OUR_USER, label.getId()).execute();
                        return;
                    }
                }
            }
        } catch (IOException e) {
            l.error("Failed to delete label", e);
        }

    }

    public void addLabelToMessage(List<Message> msgs, List<String> label) {
        setLabelsOnMessage(msgs, label, null);

    }

    public void removeLabelsFromMessage(List<Message> msgs, List<String> label) {
        setLabelsOnMessage(msgs, null, label);
    }

    private void setLabelsOnMessage(List<Message> msgIds, List<String> addLabel, List<String> removeLabel) {
        ArrayList<String> msgIdStrings = new ArrayList<>();
        msgIds.forEach(msg -> msgIdStrings.add(msg.getId()));
        setLabelsOnMessageIds(msgIdStrings, addLabel, removeLabel);
    }

    private void setLabelsOnMessageIds(List<String> msgIds, List<String> addLabel, List<String> removeLabel) {
        try {
            BatchModifyMessagesRequest mod = new BatchModifyMessagesRequest()
                    .setIds(msgIds)
                    .setAddLabelIds(addLabel)
                    .setRemoveLabelIds(removeLabel);
            service.users().messages().batchModify(OUR_USER, mod).execute();
        } catch (IOException e) {
            l.error("Failed to update labels", e);
        }
    }

    private List<Label> getRawLabels() {
        try {

            ListLabelsResponse listResponse = service.users().labels().list(OUR_USER).execute();
            return listResponse.getLabels();

        } catch (IOException e) {
            l.error("Failed to list labels", e);
        }
        return new ArrayList<>();
    }

    @NotNull
    public List<String> listLabels() {
        ArrayList<String> returnLabels = new ArrayList<>();
        List<Label> labels = getRawLabels();
        if (labels.isEmpty()) {
            l.warn("No labels found.");
        } else {
            l.trace("Labels:");
            for (Label label : labels) {
                l.trace("- %s\n", label.getName());
                returnLabels.add(label.getName());
            }
        }
        return returnLabels;
    }

    @Nullable
    private Message getMessageMeta(Message msg) {
        try {
            return service.users().messages().get(OUR_USER, msg.getId()).setFormat("metadata").execute();
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
            ArrayList<Message> messages = new ArrayList<>();
            ListMessagesResponse listResponse;
            listResponse = service.users().messages().list(OUR_USER).execute();

            while (listResponse.getMessages() != null) {
                messages.addAll(listResponse.getMessages());
                if (listResponse.getNextPageToken() != null) {
                    String pageToken = listResponse.getNextPageToken();
                    listResponse = service.users().messages().list(OUR_USER).setPageToken(pageToken).execute();
                } else break;
            }
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
            ArrayList<Message> messages = new ArrayList<>();
            while (listResponse.getMessages() != null) {
                messages.addAll(listResponse.getMessages());
                if (listResponse.getNextPageToken() != null) {
                    String pageToken = listResponse.getNextPageToken();
                    listResponse = service.users().messages().list(OUR_USER).setPageToken(pageToken).execute();
                } else break;
            }
            if (messages.isEmpty()) {
                return null;
            }
            return messages;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    private Message getFullMessage(Message msg) {
        try {
            return service.users().messages().get(OUR_USER, msg.getId()).setFormat("full").execute();
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

    public String getMessageBody(Message msg) {
        if (msg.getPayload() == null) {
            msg = getFullMessage(msg);
        }

        //think that this will only work for messages, not sure if attachments will be in the parts aswell.
        return new String(Base64.getDecoder().decode(msg.getPayload().getParts().get(0).getBody().getData()));

    }
}

