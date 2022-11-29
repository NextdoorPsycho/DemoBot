package com.volmit.demobot.util.io.data;

import com.volmit.demobot.util.io.DataType;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true, fluent = true)
public class Message implements DataType {
    private long id = 0;
    private double experience = 0.0f;// soon to implement (most used chats etc...)

}
