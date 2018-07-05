package com.olkywade.olkybot.service;

import com.github.messenger4j.Messenger;
import com.github.messenger4j.common.WebviewHeightRatio;
import com.github.messenger4j.exception.MessengerApiException;
import com.github.messenger4j.exception.MessengerIOException;
import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.MessagingType;
import com.github.messenger4j.send.NotificationType;
import com.github.messenger4j.send.SenderActionPayload;
import com.github.messenger4j.send.message.RichMediaMessage;
import com.github.messenger4j.send.message.TemplateMessage;
import com.github.messenger4j.send.message.TextMessage;
import com.github.messenger4j.send.message.quickreply.LocationQuickReply;
import com.github.messenger4j.send.message.quickreply.QuickReply;
import com.github.messenger4j.send.message.quickreply.TextQuickReply;
import com.github.messenger4j.send.message.richmedia.UrlRichMediaAsset;
import com.github.messenger4j.send.message.template.ButtonTemplate;
import com.github.messenger4j.send.message.template.GenericTemplate;
import com.github.messenger4j.send.message.template.ListTemplate;
import com.github.messenger4j.send.message.template.ReceiptTemplate;
import com.github.messenger4j.send.message.template.button.*;
import com.github.messenger4j.send.message.template.common.Element;
import com.github.messenger4j.send.message.template.receipt.Address;
import com.github.messenger4j.send.message.template.receipt.Adjustment;
import com.github.messenger4j.send.message.template.receipt.Item;
import com.github.messenger4j.send.message.template.receipt.Summary;
import com.github.messenger4j.send.recipient.IdRecipient;
import com.github.messenger4j.send.senderaction.SenderAction;
import com.github.messenger4j.userprofile.UserProfile;
import com.github.messenger4j.webhook.Event;
import com.github.messenger4j.webhook.event.*;
import com.github.messenger4j.webhook.event.attachment.Attachment;
import com.github.messenger4j.webhook.event.attachment.LocationAttachment;
import com.github.messenger4j.webhook.event.attachment.RichMediaAttachment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.messenger4j.send.message.richmedia.RichMediaAsset.Type.*;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@Slf4j
@Service
public class CallbackHandlerServiceImpl implements CallbackHandlerService {
    private final Messenger messenger;
    private static final String RESOURCE_URL = "https://raw.githubusercontent.com/fbsamples/messenger-platform-samples/master/node/public";

    public CallbackHandlerServiceImpl(Messenger messenger) {
        this.messenger = messenger;
    }

    private void sendUserDetails(String recipientId) throws MessengerApiException, MessengerIOException {
        final UserProfile userProfile = this.messenger.queryUserProfile(recipientId);
        sendTextMessage(recipientId, String.format("Your name is %s and you are %s", userProfile.firstName(), userProfile.gender()));
        log.info("User Profile Picture: {}", userProfile.profilePicture());
    }

    private void sendImageMessage(String recipientId) throws MessengerApiException, MessengerIOException, MalformedURLException {
        final UrlRichMediaAsset richMediaAsset = UrlRichMediaAsset.create(IMAGE, new URL(RESOURCE_URL + "/assets/rift.png"));
        sendRichMediaMessage(recipientId, richMediaAsset);
    }

    private void sendRichMediaMessage(String recipientId, UrlRichMediaAsset richMediaAsset) throws MessengerApiException, MessengerIOException {
        final RichMediaMessage richMediaMessage = RichMediaMessage.create(richMediaAsset);
        final MessagePayload messagePayload = MessagePayload.create(recipientId, MessagingType.RESPONSE, richMediaMessage);
        this.messenger.send(messagePayload);
    }

    private void sendGifMessage(String recipientId) throws MessengerApiException, MessengerIOException, MalformedURLException {
        final UrlRichMediaAsset richMediaAsset = UrlRichMediaAsset.create(IMAGE, new URL("https://media.giphy.com/media/11sBLVxNs7v6WA/giphy.gif"));
        sendRichMediaMessage(recipientId, richMediaAsset);
    }

    private void sendAudioMessage(String recipientId) throws MessengerApiException, MessengerIOException, MalformedURLException {
        final UrlRichMediaAsset richMediaAsset = UrlRichMediaAsset.create(AUDIO, new URL(RESOURCE_URL + "/assets/sample.mp3"));
        sendRichMediaMessage(recipientId, richMediaAsset);
    }

    private void sendVideoMessage(String recipientId) throws MessengerApiException, MessengerIOException, MalformedURLException {
        final UrlRichMediaAsset richMediaAsset = UrlRichMediaAsset.create(VIDEO, new URL(RESOURCE_URL + "/assets/allofus480.mov"));
        sendRichMediaMessage(recipientId, richMediaAsset);
    }

    private void sendFileMessage(String recipientId) throws MessengerApiException, MessengerIOException, MalformedURLException {
        final UrlRichMediaAsset richMediaAsset = UrlRichMediaAsset.create(FILE, new URL(RESOURCE_URL + "/assets/test.txt"));
        sendRichMediaMessage(recipientId, richMediaAsset);
    }

    private void sendButtonMessage(String recipientId) throws MessengerApiException, MessengerIOException, MalformedURLException {
        final List<Button> buttons = Arrays.asList(
                UrlButton.create("Open Web URL", new URL("https://www.oculus.com/en-us/rift/"), of(WebviewHeightRatio.COMPACT), of(false), empty(), empty()),
                PostbackButton.create("Trigger Postback", "DEVELOPER_DEFINED_PAYLOAD"), CallButton.create("Call Phone Number", "+16505551234")
        );

        final ButtonTemplate buttonTemplate = ButtonTemplate.create("Tap a button", buttons);
        final TemplateMessage templateMessage = TemplateMessage.create(buttonTemplate);
        final MessagePayload messagePayload = MessagePayload.create(recipientId, MessagingType.RESPONSE, templateMessage);
        this.messenger.send(messagePayload);
    }

    private void sendGenericMessage(String recipientId) throws MessengerApiException, MessengerIOException, MalformedURLException {
        List<Button> riftButtons = new ArrayList<>();
        riftButtons.add(UrlButton.create("Open Web URL", new URL("https://www.oculus.com/en-us/rift/")));
        riftButtons.add(PostbackButton.create("Call Postback", "Payload for first bubble"));

        List<Button> touchButtons = new ArrayList<>();
        touchButtons.add(UrlButton.create("Open Web URL", new URL("https://www.oculus.com/en-us/touch/")));
        touchButtons.add(PostbackButton.create("Call Postback", "Payload for second bubble"));

        final List<Element> elements = new ArrayList<>();

        elements.add(
                Element.create("rift", of("Next-generation virtual reality"), of(new URL("https://www.oculus.com/en-us/rift/")), empty(), of(riftButtons)));
        elements.add(Element.create("touch", of("Your Hands, Now in VR"), of(new URL("https://www.oculus.com/en-us/touch/")), empty(), of(touchButtons)));

        final GenericTemplate genericTemplate = GenericTemplate.create(elements);
        final TemplateMessage templateMessage = TemplateMessage.create(genericTemplate);
        final MessagePayload messagePayload = MessagePayload.create(recipientId, MessagingType.RESPONSE, templateMessage);
        this.messenger.send(messagePayload);
    }

    private void sendListMessageMessage(String recipientId) throws MessengerApiException, MessengerIOException, MalformedURLException {
        List<Button> riftButtons = new ArrayList<>();
        riftButtons.add(UrlButton.create("Open Web URL", new URL("https://www.oculus.com/en-us/rift/")));

        List<Button> touchButtons = new ArrayList<>();
        touchButtons.add(UrlButton.create("Open Web URL", new URL("https://www.oculus.com/en-us/touch/")));

        final List<Element> elements = new ArrayList<>();

        elements.add(
                Element.create("rift", of("Next-generation virtual reality"), of(new URL("https://www.oculus.com/en-us/rift/")), empty(), of(riftButtons)));
        elements.add(Element.create("touch", of("Your Hands, Now in VR"), of(new URL("https://www.oculus.com/en-us/touch/")), empty(), of(touchButtons)));

        final ListTemplate listTemplate = ListTemplate.create(elements);
        final TemplateMessage templateMessage = TemplateMessage.create(listTemplate);
        final MessagePayload messagePayload = MessagePayload.create(recipientId, MessagingType.RESPONSE, templateMessage);
        this.messenger.send(messagePayload);
    }

    private void sendReceiptMessage(String recipientId) throws MessengerApiException, MessengerIOException, MalformedURLException {
        final String uniqueReceiptId = "order-" + Math.floor(Math.random() * 1000);

        final List<Item> items = new ArrayList<>();

        items.add(Item.create("Oculus Rift", 599.00f, of("Includes: headset, sensor, remote"), of(1), of("USD"),
                of(new URL(RESOURCE_URL + "/assets/riftsq.png"))));
        items.add(Item.create("Samsung Gear VR", 99.99f, of("Frost White"), of(1), of("USD"), of(new URL(RESOURCE_URL + "/assets/gearvrsq.png"))));

        final ReceiptTemplate receiptTemplate = ReceiptTemplate
                .create("Peter Chang", uniqueReceiptId, "Visa 1234", "USD", Summary.create(626.66f, of(698.99f), of(57.67f), of(20.00f)),
                        of(Address.create("1 Hacker Way", "Menlo Park", "94025", "CA", "US")), of(items),
                        of(Arrays.asList(Adjustment.create("New Customer Discount", -50f), Adjustment.create("$100 Off Coupon", -100f))),
                        of("The Boring Company"), of(new URL("https://www.boringcompany.com/")), of(true), of(Instant.ofEpochMilli(1428444852L)));

        final TemplateMessage templateMessage = TemplateMessage.create(receiptTemplate);
        final MessagePayload messagePayload = MessagePayload.create(recipientId, MessagingType.RESPONSE, templateMessage);
        this.messenger.send(messagePayload);
    }

    private void sendQuickReply(String recipientId) throws MessengerApiException, MessengerIOException {
        List<QuickReply> quickReplies = new ArrayList<>();

        quickReplies.add(TextQuickReply.create("Action", "DEVELOPER_DEFINED_PAYLOAD_FOR_PICKING_ACTION"));
        quickReplies.add(TextQuickReply.create("Comedy", "DEVELOPER_DEFINED_PAYLOAD_FOR_PICKING_COMEDY"));
        quickReplies.add(TextQuickReply.create("Drama", "DEVELOPER_DEFINED_PAYLOAD_FOR_PICKING_DRAMA"));
        quickReplies.add(LocationQuickReply.create());

        TextMessage message = TextMessage.create("What's your favorite movie genre?", of(quickReplies), empty());
        messenger.send(MessagePayload.create(recipientId, MessagingType.RESPONSE, message));
    }

    private void sendReadReceipt(String recipientId) throws MessengerApiException, MessengerIOException {
        this.messenger.send(SenderActionPayload.create(recipientId, SenderAction.MARK_SEEN));
    }

    private void sendTypingOn(String recipientId) throws MessengerApiException, MessengerIOException {
        this.messenger.send(SenderActionPayload.create(recipientId, SenderAction.TYPING_ON));
    }

    private void sendTypingOff(String recipientId) throws MessengerApiException, MessengerIOException {
        this.messenger.send(SenderActionPayload.create(recipientId, SenderAction.TYPING_OFF));
    }

    private void sendAccountLinking(String recipientId) throws MessengerApiException, MessengerIOException, MalformedURLException {
        // Mandatory https
        final LogInButton buttonIn = LogInButton.create(new URL("https://<YOUR_REST_CALLBACK_URL>"));
        final LogOutButton buttonOut = LogOutButton.create();

        final List<Button> buttons = Arrays.asList(buttonIn, buttonOut);
        final ButtonTemplate buttonTemplate = ButtonTemplate.create("Log in to see an account linking callback", buttons);

        final TemplateMessage templateMessage = TemplateMessage.create(buttonTemplate);
        final MessagePayload messagePayload = MessagePayload.create(recipientId, MessagingType.RESPONSE, templateMessage);
        this.messenger.send(messagePayload);
    }

    @Override
    public void handleTextMessageEvent(TextMessageEvent event) {
        log.debug("Received TextMessageEvent: {}", event);

        final String messageId = event.messageId();
        final String messageText = event.text();
        final String senderId = event.senderId();
        final Instant timestamp = event.timestamp();

        log.info("Received message '{}' with text '{}' from user '{}' at '{}'", messageId, messageText, senderId, timestamp);

        try {
            switch (messageText.toLowerCase()) {
                case "user":
                    sendUserDetails(senderId);
                    break;

                case "image":
                    sendImageMessage(senderId);
                    break;

                case "gif":
                    sendGifMessage(senderId);
                    break;

                case "audio":
                    sendAudioMessage(senderId);
                    break;

                case "video":
                    sendVideoMessage(senderId);
                    break;

                case "file":
                    sendFileMessage(senderId);
                    break;

                case "button":
                    sendButtonMessage(senderId);
                    break;

                case "generic":
                    sendGenericMessage(senderId);
                    break;

                case "list":
                    sendListMessageMessage(senderId);
                    break;

                case "receipt":
                    sendReceiptMessage(senderId);
                    break;

                case "quick reply":
                    sendQuickReply(senderId);
                    break;

                case "read receipt":
                    sendReadReceipt(senderId);
                    break;

                case "typing on":
                    sendTypingOn(senderId);
                    break;

                case "typing off":
                    sendTypingOff(senderId);
                    break;

                case "account linking":
                    sendAccountLinking(senderId);
                    break;

                default:
                    sendTextMessage(senderId, messageText);
            }
        } catch (MessengerApiException | MessengerIOException | MalformedURLException e) {
            handleSendException(e);
        }
    }

    @Override
    public void handleAttachmentMessageEvent(AttachmentMessageEvent event) {
        log.debug("Handling QuickReplyMessageEvent");
        final String senderId = event.senderId();
        log.debug("senderId: {}", senderId);
        for (Attachment attachment : event.attachments()) {
            if (attachment.isRichMediaAttachment()) {
                final RichMediaAttachment richMediaAttachment = attachment.asRichMediaAttachment();
                final RichMediaAttachment.Type type = richMediaAttachment.type();
                final URL url = richMediaAttachment.url();
                log.debug("Received rich media attachment of type '{}' with url: {}", type, url);
                final String text = String.format("Media %s received (url: %s)", type.name(), url);
                sendTextMessage(senderId, text);
            } else if (attachment.isLocationAttachment()) {
                final LocationAttachment locationAttachment = attachment.asLocationAttachment();
                final double longitude = locationAttachment.longitude();
                final double latitude = locationAttachment.latitude();
                log.debug("Received location information (long: {}, lat: {})", longitude, latitude);
                final String text = String.format("Location received (long: %s, lat: %s)", String.valueOf(longitude), String.valueOf(latitude));
                sendTextMessage(senderId, text);
            }
        }
    }

    @Override
    public void handleQuickReplyMessageEvent(QuickReplyMessageEvent event) {
        log.debug("Handling QuickReplyMessageEvent");
        final String payload = event.payload();
        log.debug("payload: {}", payload);
        final String senderId = event.senderId();
        log.debug("senderId: {}", senderId);
        final String messageId = event.messageId();
        log.debug("messageId: {}", messageId);
        log.info("Received quick reply for message '{}' with payload '{}'", messageId, payload);
        sendTextMessage(senderId, "Quick reply tapped");
    }

    @Override
    public void handlePostbackEvent(PostbackEvent event) {
        log.debug("Handling PostbackEvent");
        final String payload = event.payload().orElse("empty payload");
        log.debug("payload: {}", payload);
        final String senderId = event.senderId();
        log.debug("senderId: {}", senderId);
        final Instant timestamp = event.timestamp();
        log.debug("timestamp: {}", timestamp);
        log.info("Received postback for user '{}' and page '{}' with payload '{}' at '{}'", senderId, senderId, payload, timestamp);
        sendTextMessage(senderId, "Postback event tapped");
    }

    @Override
    public void handleAccountLinkingEvent(AccountLinkingEvent event) {
        log.debug("Handling AccountLinkingEvent");
        final String senderId = event.senderId();
        log.debug("senderId: {}", senderId);
        final AccountLinkingEvent.Status accountLinkingStatus = event.status();
        log.debug("accountLinkingStatus: {}", accountLinkingStatus);
        final String authorizationCode = event.authorizationCode().orElse("Empty authorization code!!!"); //You can throw an Exception
        log.debug("authorizationCode: {}", authorizationCode);
        log.info("Received account linking event for user '{}' with status '{}' and auth code '{}'", senderId, accountLinkingStatus, authorizationCode);
        sendTextMessage(senderId, "AccountLinking event tapped");
    }

    @Override
    public void handleOptInEvent(OptInEvent event) {
        log.debug("Handling OptInEvent");
        final String senderId = event.senderId();
        log.debug("senderId: {}", senderId);
        final String recipientId = event.recipientId();
        log.debug("recipientId: {}", recipientId);
        final String passThroughParam = event.refPayload().orElse("empty payload");
        log.debug("passThroughParam: {}", passThroughParam);
        final Instant timestamp = event.timestamp();
        log.debug("timestamp: {}", timestamp);

        log.info("Received authentication for user '{}' and page '{}' with pass through param '{}' at '{}'", senderId, recipientId, passThroughParam,
                timestamp);
        sendTextMessage(senderId, "Authentication successful");
    }

    @Override
    public void handleMessageEchoEvent(MessageEchoEvent event) {
        log.debug("Handling MessageEchoEvent");
        final String senderId = event.senderId();
        log.debug("senderId: {}", senderId);
        final String recipientId = event.recipientId();
        log.debug("recipientId: {}", recipientId);
        final String messageId = event.messageId();
        log.debug("messageId: {}", messageId);
        final Instant timestamp = event.timestamp();
        log.debug("timestamp: {}", timestamp);

        log.info("Received echo for message '{}' that has been sent to recipient '{}' by sender '{}' at '{}'", messageId, recipientId, senderId, timestamp);
        sendTextMessage(senderId, "MessageEchoEvent tapped");
    }

    @Override
    public void handleMessageDeliveredEvent(MessageDeliveredEvent event) {
        log.debug("Handling MessageDeliveredEvent");
        final String senderId = event.senderId();
        log.debug("senderId: {}", senderId);
        final List<String> messageIds = event.messageIds().orElse(Collections.emptyList());
        final Instant watermark = event.watermark();
        log.debug("watermark: {}", watermark);

        messageIds.forEach(messageId -> {
            log.info("Received delivery confirmation for message '{}'", messageId);
        });

        log.info("All messages before '{}' were delivered to user '{}'", watermark, senderId);
    }

    @Override
    public void handleMessageReadEvent(MessageReadEvent event) {
        log.debug("Handling MessageReadEvent");
        final String senderId = event.senderId();
        log.debug("senderId: {}", senderId);
        final Instant watermark = event.watermark();
        log.debug("watermark: {}", watermark);

        log.info("All messages before '{}' were read by user '{}'", watermark, senderId);
    }

    @Override
    public void handleFallbackEvent(Event event) {
        log.debug("Handling FallbackEvent");
        final String senderId = event.senderId();
        log.debug("senderId: {}", senderId);

        log.info("Received unsupported message from user '{}'", senderId);
    }

    private void sendTextMessage(String recipientId, String text) {
        try {
            final IdRecipient recipient = IdRecipient.create(recipientId);
            final NotificationType notificationType = NotificationType.REGULAR;
            final String metadata = "DEVELOPER_DEFINED_METADATA";

            final TextMessage textMessage = TextMessage.create(text, empty(), of(metadata));
            final MessagePayload messagePayload = MessagePayload.create(recipient, MessagingType.RESPONSE, textMessage,
                    of(notificationType), empty());
            this.messenger.send(messagePayload);
        } catch (MessengerApiException | MessengerIOException e) {
            handleSendException(e);
        }
    }

    private void handleSendException(Exception e) {
        log.error("Message could not be sent. An unexpected error occurred.", e);
    }
}
