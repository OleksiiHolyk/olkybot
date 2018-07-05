package com.olkywade.olkybot.service;

import com.github.messenger4j.webhook.Event;
import com.github.messenger4j.webhook.event.*;

public interface CallbackHandlerService {
    void handleTextMessageEvent(TextMessageEvent event);

    void handleAttachmentMessageEvent(AttachmentMessageEvent attachmentMessageEvent);

    void handleQuickReplyMessageEvent(QuickReplyMessageEvent quickReplyMessageEvent);

    void handlePostbackEvent(PostbackEvent postbackEvent);

    void handleAccountLinkingEvent(AccountLinkingEvent accountLinkingEvent);

    void handleOptInEvent(OptInEvent optInEvent);

    void handleMessageEchoEvent(MessageEchoEvent messageEchoEvent);

    void handleMessageDeliveredEvent(MessageDeliveredEvent messageDeliveredEvent);

    void handleMessageReadEvent(MessageReadEvent messageReadEvent);

    void handleFallbackEvent(Event event);

}
