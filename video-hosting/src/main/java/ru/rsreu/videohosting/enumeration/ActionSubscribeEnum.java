package ru.rsreu.videohosting.enumeration;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum ActionSubscribeEnum {
    @JsonProperty("SUBSCRIBE")
    SUBSCRIBE,
    @JsonProperty("UNSUBSCRIBE")
    UNSUBSCRIBE
}
