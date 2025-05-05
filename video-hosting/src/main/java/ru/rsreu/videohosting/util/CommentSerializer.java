package ru.rsreu.videohosting.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import ru.rsreu.videohosting.entity.Comment;

import java.io.IOException;
import java.io.StringWriter;

public class CommentSerializer extends JsonSerializer<Comment> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void serialize(Comment value,
                          JsonGenerator gen,
                          SerializerProvider serializers)
            throws IOException, JsonProcessingException {

        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, value);
        gen.writeFieldName(writer.toString());
    }
}
