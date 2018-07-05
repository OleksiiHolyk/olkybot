package com.olkywade.olkybot.controller;

import com.github.messenger4j.Messenger;
import com.github.messenger4j.exception.MessengerVerificationException;
import com.olkywade.olkybot.service.CallbackHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.github.messenger4j.Messenger.*;
import static java.util.Optional.of;


@Slf4j
@RestController
@RequestMapping("/callback")
public class MainController {
    private final Messenger messenger;
    private final CallbackHandlerService callbackHandlerService;

    public MainController(Messenger messenger, CallbackHandlerService callbackHandlerService) {
        this.messenger = messenger;
        this.callbackHandlerService = callbackHandlerService;
    }


    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> verifyWebhook(@RequestParam(MODE_REQUEST_PARAM_NAME) final String mode,
                                                @RequestParam(VERIFY_TOKEN_REQUEST_PARAM_NAME) final String verifyToken, @RequestParam(CHALLENGE_REQUEST_PARAM_NAME) final String challenge) {
        log.debug("Received Webhook verification request - mode: {} | verifyToken: {} | challenge: {}", mode, verifyToken, challenge);
        try {
            this.messenger.verifyWebhook(mode, verifyToken);
            return ResponseEntity.ok(challenge);
        } catch (MessengerVerificationException e) {
            log.warn("Webhook verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> handleCallback(@RequestBody final String payload, @RequestHeader(SIGNATURE_HEADER_NAME) final String signature) {
        log.debug("Received Messenger Platform callback - payload: {} | signature: {}", payload, signature);


        try {
            this.messenger.onReceiveEvents(payload, of(signature), event -> {
                if (event.isTextMessageEvent()) {
                    callbackHandlerService.handleTextMessageEvent(event.asTextMessageEvent());
                } else if (event.isAttachmentMessageEvent()) {
                    callbackHandlerService.handleAttachmentMessageEvent(event.asAttachmentMessageEvent());
                } else if (event.isQuickReplyMessageEvent()) {
                    callbackHandlerService.handleQuickReplyMessageEvent(event.asQuickReplyMessageEvent());
                } else if (event.isPostbackEvent()) {
                    callbackHandlerService.handlePostbackEvent(event.asPostbackEvent());
                } else if (event.isAccountLinkingEvent()) {
                    callbackHandlerService.handleAccountLinkingEvent(event.asAccountLinkingEvent());
                } else if (event.isOptInEvent()) {
                    callbackHandlerService.handleOptInEvent(event.asOptInEvent());
                } else if (event.isMessageEchoEvent()) {
                    callbackHandlerService.handleMessageEchoEvent(event.asMessageEchoEvent());
                } else if (event.isMessageDeliveredEvent()) {
                    callbackHandlerService.handleMessageDeliveredEvent(event.asMessageDeliveredEvent());
                } else if (event.isMessageReadEvent()) {
                    callbackHandlerService.handleMessageReadEvent(event.asMessageReadEvent());
                } else {
                    callbackHandlerService.handleFallbackEvent(event);
                }
            });
            log.debug("Processed callback payload successfully");
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (MessengerVerificationException e) {
            log.warn("Processing of callback payload failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }


}
