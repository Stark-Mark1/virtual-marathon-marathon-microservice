package com.virtualmarathon.marathon;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Constants {
    public static LocalDateTime getCurrentTime(){
        return LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    }
}
