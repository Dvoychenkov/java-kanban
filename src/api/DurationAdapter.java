package api;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter writer, Duration duration) throws IOException {
        writer.value(duration.toMinutes());
    }

    @Override
    public Duration read(JsonReader reader) throws IOException {
        String durationStr = reader.nextString();
        if (durationStr.isBlank()) {
            return null;
        }
        return Duration.ofMinutes(Long.parseLong(durationStr));
    }
}