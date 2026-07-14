package com.jiubuntu.wms.global.infrastructure;

public interface EmailSender {

    void send(String to, String subject, String body);

}
