package com.techvvs.inventory.util

import org.springframework.stereotype.Component
import org.springframework.ui.Model
import org.thymeleaf.util.StringUtils

@Component
class ModelMessageUtil {

    void addMessagesToModel(Model model, Map<String, List<String>> messages) {
        if (model != null && messages != null) {
            messages.each { msgType, msgList ->
                msgList.each { messageValue ->
                    addMessage(model, msgType, messageValue)
                }
            }
        }
    }

    void addMessage(Model model, String msgType, String messageValue) {
        if (model != null && !StringUtils.isEmpty(msgType) && !StringUtils.isEmpty(messageValue)) {
            if (model.getAttribute(msgType) != null) {
                String exitingMsg = model.getAttribute(msgType)
                model.addAttribute(msgType, exitingMsg + " | " + messageValue)
            } else {
                model.addAttribute(msgType, messageValue)
            }
        }
    }
}
