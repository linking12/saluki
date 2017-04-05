package com.quancheng.saluki.gateway.controller;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

public final class RedirectMessageHelper {

    private RedirectMessageHelper(){
    }

    public static void addErrorMessage(RedirectAttributes attributes, String errorMessage) {
        addMessage(attributes, "dangerMessages", errorMessage);
    }

    public static void addMessage(RedirectAttributes attributes, String message) {
        addMessage(attributes, "infoMessages", message);
    }

    public static void addWarningMessage(RedirectAttributes attributes, String message) {
        addMessage(attributes, "warningMessages", message);
    }

    public static void addSuccessMessage(RedirectAttributes attributes, String message) {
        addMessage(attributes, "successMessages", message);
    }

    @SuppressWarnings("unchecked")
    public static void addMessage(RedirectAttributes attributes, String type, String message) {
        if (attributes.getFlashAttributes().keySet().contains(type)) {
            ((List<String>) attributes.getFlashAttributes().get(type)).add(message);
        } else {
            List<String> messageList = new ArrayList<>();
            messageList.add(message);
            attributes.addFlashAttribute(type, messageList);
        }
    }
}
