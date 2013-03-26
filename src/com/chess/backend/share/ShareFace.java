package com.chess.backend.share;

import java.text.SimpleDateFormat;


public interface ShareFace {

    String getMailSubject();

    String composeTwitterMessage();

    String composeMailMessage();

    String composeSmsMessage(SimpleDateFormat inputTimeFormat, SimpleDateFormat outputTimeFormat);

    String getCaption();

    String getDescription();

    String getPicture();

    String getLink();

    String getName();
}
