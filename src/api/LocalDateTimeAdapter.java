package api;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(JsonWriter writer, LocalDateTime datetime) throws IOException {
        if (datetime == null) {
            writer.value("");
            return;
        }
        writer.value(datetime.format(FORMATTER));
    }

    @Override
    public LocalDateTime read(JsonReader reader) throws IOException {
        String dateTimeStr = reader.nextString();
        if (dateTimeStr.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, FORMATTER);
    }
}