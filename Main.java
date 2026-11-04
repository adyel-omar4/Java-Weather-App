
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Displays a message followed by a new line.
    private static void line(String message) {
        System.out.print(message + System.lineSeparator());
    }

    // Finds the position immediately after a JSON key's colon.
    private static int findValueStart(String json, String key) {

        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key) + "\"\\s*:"
        );

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            return -1;
        }

        return matcher.end();
    }

    // Finds a closing bracket or brace, ignoring delimiters inside strings.
    private static int findClosingDelimiter(
            String json, int start, char opening, char closing) {

        int depth = 0;
        boolean insideString = false;
        boolean escaped = false;

        for (int i = start; i < json.length(); i++) {

            char c = json.charAt(i);

            if (insideString) {

                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    insideString = false;
                }

                continue;
            }

            if (c == '"') {
                insideString = true;

            } else if (c == opening) {
                depth++;

            } else if (c == closing) {
                depth--;

                if (depth == 0) {
                    return i;
                }
            }
        }

        return -1;
    }

    // Extracts a JSON object.
    private static String getObject(String json, String key) {

        int position = findValueStart(json, key);

        if (position == -1) {
            return "";
        }

        while (position < json.length()
                && Character.isWhitespace(json.charAt(position))) {
            position++;
        }

        if (position >= json.length() || json.charAt(position) != '{') {
            return "";
        }

        int end = findClosingDelimiter(json, position, '{', '}');

        if (end == -1) {
            return "";
        }

        return json.substring(position, end + 1);
    }

    // Decodes common JSON string escape sequences.
    private static String decodeJsonString(String value) {

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {

            char c = value.charAt(i);

            if (c != '\\' || i + 1 >= value.length()) {
                result.append(c);
                continue;
            }

            char next = value.charAt(++i);

            switch (next) {

                case '"' -> result.append('"');
                case '\\' -> result.append('\\');
                case '/' -> result.append('/');
                case 'b' -> result.append('\b');
                case 'f' -> result.append('\f');
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                case 't' -> result.append('\t');

                case 'u' -> {

                    if (i + 4 < value.length()) {

                        String hex = value.substring(i + 1, i + 5);

                        try {
                            result.append((char) Integer.parseInt(hex, 16));
                            i += 4;

                        } catch (NumberFormatException e) {
                            result.append("\\u").append(hex);
                            i += 4;
                        }
                    }
                }

                default -> result.append(next);
            }
        }

        return result.toString();
    }

    // Extracts a JSON string.
    private static String getString(String json, String key) {

        int position = findValueStart(json, key);

        if (position == -1) {
            return "";
        }

        while (position < json.length()
                && Character.isWhitespace(json.charAt(position))) {
            position++;
        }

        if (position >= json.length() || json.charAt(position) != '"') {
            return "";
        }

        int start = ++position;
        boolean escaped = false;

        while (position < json.length()) {

            char c = json.charAt(position);

            if (escaped) {
                escaped = false;

            } else if (c == '\\') {
                escaped = true;

            } else if (c == '"') {

                return decodeJsonString(
                        json.substring(start, position)
                );
            }

            position++;
        }

        return "";
    }

    // Extracts a numeric JSON value.
    private static String getNumber(String json, String key) {

        int position = findValueStart(json, key);

        if (position == -1) {
            return "";
        }

        String remaining = json.substring(position).stripLeading();

        Matcher matcher = Pattern.compile(
                "-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?"
        ).matcher(remaining);

        if (matcher.find() && matcher.start() == 0) {
            return matcher.group();
        }

        return "";
    }

    // Splits a JSON array into individual scalar values.
    private static List<String> parseArray(String array) {

        List<String> values = new ArrayList<>();

        if (array.isEmpty()) {
            return values;
        }

        StringBuilder element = new StringBuilder();

        boolean insideString = false;
        boolean escaped = false;

        for (int i = 0; i < array.length(); i++) {

            char c = array.charAt(i);

            if (insideString) {

                element.append(c);

                if (escaped) {
                    escaped = false;

                } else if (c == '\\') {
                    escaped = true;

                } else if (c == '"') {
                    insideString = false;
                }

            } else if (c == '"') {

                insideString = true;
                element.append(c);

            } else if (c == ',') {

                values.add(cleanArrayElement(element.toString()));
                element.setLength(0);

            } else {

                element.append(c);
            }
        }

        if (!element.isEmpty()) {
            values.add(cleanArrayElement(element.toString()));
        }

        return values;
    }

    // Removes unnecessary whitespace and decodes string elements.
    private static String cleanArrayElement(String element) {

        String value = element.trim();

        if (value.length() >= 2
                && value.startsWith("\"")
                && value.endsWith("\"")) {

            return decodeJsonString(
                    value.substring(1, value.length() - 1)
            );
        }

        return value;
    }

    // Extracts a JSON array.
    private static List<String> getArray(String json, String key) {

        int position = findValueStart(json, key);

        if (position == -1) {
            return new ArrayList<>();
        }

        while (position < json.length()
                && Character.isWhitespace(json.charAt(position))) {
            position++;
        }

        if (position >= json.length() || json.charAt(position) != '[') {
            return new ArrayList<>();
        }

        int end = findClosingDelimiter(json, position, '[', ']');

        if (end == -1) {
            return new ArrayList<>();
        }

        String content = json.substring(position + 1, end);

        return parseArray(content);
    }

    // Extracts the first object from the "results" JSON array.
    private static String getFirstObject(String json) {

        int position = findValueStart(json, "results");

        if (position == -1) {
            return "";
        }

        while (position < json.length()
                && Character.isWhitespace(json.charAt(position))) {
            position++;
        }

        if (position >= json.length() || json.charAt(position) != '[') {
            return "";
        }

        int end = findClosingDelimiter(json, position, '[', ']');

        if (end == -1) {
            return "";
        }

        String content = json.substring(position + 1, end).trim();

        if (!content.startsWith("{")) {
            return "";
        }

        int objectEnd = findClosingDelimiter(content, 0, '{', '}');

        if (objectEnd == -1) {
            return "";
        }

        return content.substring(0, objectEnd + 1);
    }

    // Sends an HTTP GET request.
    private static String sendRequest(String url)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new IOException(
                    "HTTP error: " + response.statusCode()
            );
        }

        return response.body();
    }

    // Finds a city's coordinates using Open-Meteo geocoding.
    private static String getLocation(String city)
            throws IOException, InterruptedException {

        String encodedCity = URLEncoder.encode(
                city,
                StandardCharsets.UTF_8
        );

        String url = "https://geocoding-api.open-meteo.com/v1/search"
                + "?name=" + encodedCity
                + "&count=1"
                + "&language=en"
                + "&format=json";

        String response = sendRequest(url);

        return getFirstObject(response);
    }

    // Converts WMO weather codes into readable descriptions.
    private static String getWeather(int code) {

        return switch (code) {

            case 0 -> "Clear sky";
            case 1 -> "Mainly clear";
            case 2 -> "Partly cloudy";
            case 3 -> "Overcast";

            case 45, 48 -> "Fog";

            case 51, 53, 55 -> "Drizzle";
            case 56, 57 -> "Freezing drizzle";

            case 61, 63, 65 -> "Rain";
            case 66, 67 -> "Freezing rain";

            case 71, 73, 75 -> "Snow";
            case 77 -> "Snow grains";

            case 80, 81, 82 -> "Rain showers";
            case 85, 86 -> "Snow showers";

            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with hail";

            default -> "Unknown condition";
        };
    }

    // Finds the first hourly forecast at or after the current local time.
    private static int findForecastStart(List<String> times) {

        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < times.size(); i++) {

            try {

                LocalDateTime forecastTime = LocalDateTime.parse(
                        times.get(i),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                );

                if (!forecastTime.isBefore(now)) {
                    return i;
                }

            } catch (Exception ignored) {
                // Skip invalid time values.
            }
        }

        return 0;
    }

    // Finds the number of entries available in all forecast arrays.
    private static int getAvailableCount(List<List<String>> arrays) {

        int min = Integer.MAX_VALUE;

        for (List<String> array : arrays) {
            min = Math.min(min, array.size());
        }

        return min == Integer.MAX_VALUE ? 0 : min;
    }

    // Safely converts a string to a double.
    private static double toDouble(String value) {

        try {
            return Double.parseDouble(value);

        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    // Formats numeric values.
    private static String formatValue(String value) {

        double number = toDouble(value);

        if (Double.isNaN(number)) {
            return "--";
        }

        return String.format("%.1f", number);
    }

    // Displays current weather conditions.
    private static void displayCurrentWeather(
            String city,
            String current,
            String timezone) {

        String temperature = getNumber(current, "temperature_2m");
        String feelsLike = getNumber(current, "apparent_temperature");
        String humidity = getNumber(current, "relative_humidity_2m");
        String wind = getNumber(current, "wind_speed_10m");
        String weatherCode = getNumber(current, "weather_code");
        String isDay = getNumber(current, "is_day");

        line("\n====================================");
        line("       CURRENT WEATHER");
        line("====================================");

        line("City: " + city);
        line("Timezone: " + timezone);

        line("------------------------------------");

        line("Temperature : " + formatValue(temperature) + " °C");
        line("Feels like  : " + formatValue(feelsLike) + " °C");

        line("Humidity    : "
                + (humidity.isEmpty() ? "--" : humidity) + " %");

        line("Wind speed  : " + formatValue(wind) + " km/h");

        if (!weatherCode.isEmpty()) {
            line("Condition   : "
                    + getWeather((int) toDouble(weatherCode)));
        }

        if (!isDay.isEmpty()) {
            line("Time period : "
                    + (isDay.equals("1") ? "Day" : "Night"));
        }

        line("====================================");
    }

    // Displays the next 12 hours of weather data.
    private static void displayHourlyForecast(
            List<String> times,
            List<String> temperatures,
            List<String> precipitation,
            List<String> rain,
            List<String> showers,
            List<String> snowfall,
            List<String> wind,
            List<String> uv,
            List<String> isDay,
            List<String> weatherCodes) {

        List<List<String>> arrays = List.of(
                times,
                temperatures,
                precipitation,
                rain,
                showers,
                snowfall,
                wind,
                uv,
                isDay,
                weatherCodes
        );

        int availableCount = getAvailableCount(arrays);

        if (availableCount == 0) {
            line("No hourly forecast available.");
            return;
        }

        int start = findForecastStart(times);
        int end = Math.min(start + 12, availableCount);

        line("\nHOURLY FORECAST");
        line("---------------------------------------------------------------");

        System.out.printf(
                "%-7s %-6s %-7s %-6s %-7s %-6s %-7s %-5s %-6s%n",
                "TIME", "TEMP", "TOTAL", "RAIN",
                "SHOWERS", "SNOW", "WIND", "UV", "PERIOD"
        );

        line("---------------------------------------------------------------");

        for (int i = start; i < end; i++) {

            String time = times.get(i);

            if (time.length() >= 16) {
                time = time.substring(11, 16);
            }

            String period = isDay.get(i).equals("1") ? "Day" : "Night";

            System.out.printf(
                    "%-7s %-6s %-7s %-6s %-7s %-6s %-7s %-5s %-6s%n",
                    time,
                    formatValue(temperatures.get(i)),
                    formatValue(precipitation.get(i)),
                    formatValue(rain.get(i)),
                    formatValue(showers.get(i)),
                    formatValue(snowfall.get(i)),
                    formatValue(wind.get(i)),
                    formatValue(uv.get(i)),
                    period
            );

            int code = (int) toDouble(weatherCodes.get(i));

            System.out.printf(
                    "        Condition: %s (WMO %s)%n",
                    getWeather(code),
                    weatherCodes.get(i)
            );
        }

        line("---------------------------------------------------------------");
        line("TEMP = °C | TOTAL / RAIN / SHOWERS = mm");
        line("SNOW = cm | WIND = km/h");
    }

    // Displays an ASCII temperature chart.
    private static void displayChart(
            List<String> times,
            List<String> temperatures) {

        int count = Math.min(12, Math.min(
                times.size(),
                temperatures.size()
        ));

        int start = findForecastStart(times);
        int end = Math.min(start + count, times.size());

        if (start >= end) {
            line("\nNo chart data available.");
            return;
        }

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        for (int i = start; i < end; i++) {

            double temperature = toDouble(temperatures.get(i));

            if (!Double.isNaN(temperature)) {
                min = Math.min(min, temperature);
                max = Math.max(max, temperature);
            }
        }

        if (min == Double.MAX_VALUE) {
            line("\nNo valid temperature data.");
            return;
        }

        line("\nTEMPERATURE CHART");
        line("-----------------------------------------------");

        for (int i = start; i < end; i++) {

            double temperature = toDouble(temperatures.get(i));

            if (Double.isNaN(temperature)) {
                continue;
            }

            String time = times.get(i);

            if (time.length() >= 16) {
                time = time.substring(11, 16);
            }

            int barLength = 1;

            if (max > min) {
                barLength = 1 + (int) (
                        ((temperature - min) / (max - min)) * 23
                );
            }

            System.out.printf("%-6s | ", time);
            System.out.print("#".repeat(barLength));
            System.out.printf(" %.1f °C%n", temperature);
        }

        line("-----------------------------------------------");
    }

    // Searches for and displays weather for one city.
    private static void searchCity(String city)
            throws IOException, InterruptedException {

        line("\nSearching for \"" + city + "\"...");

        String location = getLocation(city);

        if (location.isEmpty()) {

            line("\nSorry, there is no city named \"" + city + "\".");
            line("Please check the spelling and try again.");

            return;
        }

        String locationName = getString(location, "name");
        String country = getString(location, "country");
        String latitude = getNumber(location, "latitude");
        String longitude = getNumber(location, "longitude");

        if (latitude.isEmpty() || longitude.isEmpty()) {
            line("Could not retrieve city coordinates.");
            return;
        }

        String displayCity = locationName;

        if (!country.isEmpty()) {
            displayCity += ", " + country;
        }

        line("Location found: " + displayCity);
        line("Latitude: " + latitude);
        line("Longitude: " + longitude);

        String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + latitude
                + "&longitude=" + longitude
                + "&current=temperature_2m,relative_humidity_2m,"
                + "apparent_temperature,is_day,precipitation,rain,"
                + "showers,snowfall,weather_code,wind_speed_10m"
                + "&hourly=temperature_2m,precipitation,rain,"
                + "showers,snowfall,weather_code,wind_speed_10m,"
                + "uv_index,is_day"
                + "&temperature_unit=celsius"
                + "&wind_speed_unit=kmh"
                + "&precipitation_unit=mm"
                + "&timeformat=iso8601"
                + "&timezone=auto"
                + "&forecast_days=2";

        line("\nRetrieving weather forecast...");

        String weatherData = sendRequest(url);

        String current = getObject(weatherData, "current");
        String hourly = getObject(weatherData, "hourly");

        if (current.isEmpty() || hourly.isEmpty()) {
            line("Weather data is incomplete.");
            return;
        }

        String timezone = getString(weatherData, "timezone");

        displayCurrentWeather(
                displayCity,
                current,
                timezone
        );

        List<String> times = getArray(hourly, "time");

        List<String> temperatures =
                getArray(hourly, "temperature_2m");

        List<String> precipitation =
                getArray(hourly, "precipitation");

        List<String> rain =
                getArray(hourly, "rain");

        List<String> showers =
                getArray(hourly, "showers");

        List<String> snowfall =
                getArray(hourly, "snowfall");

        List<String> wind =
                getArray(hourly, "wind_speed_10m");

        List<String> uv =
                getArray(hourly, "uv_index");

        List<String> isDay =
                getArray(hourly, "is_day");

        List<String> weatherCodes =
                getArray(hourly, "weather_code");

        displayHourlyForecast(
                times,
                temperatures,
                precipitation,
                rain,
                showers,
                snowfall,
                wind,
                uv,
                isDay,
                weatherCodes
        );

        displayChart(times, temperatures);

        line("\nWeather forecast completed.");
    }

    // Main program: allows continuous city searches.
    public static void main(String[] args) {

        try (Scanner scanner = new Scanner(System.in)) {

            line("====================================");
            line("       JAVA WEATHER PROJECT");
            line("====================================");

            while (true) {

                System.out.print(
                        "\nEnter a city (q to quit, ESC to exit): "
                );

                String city = scanner.nextLine().trim();

                // Exit conditions.
                if (city.equalsIgnoreCase("q")
                        || city.equalsIgnoreCase("quit")
                        || city.equalsIgnoreCase("esc")) {

                    line("\nWeather application closed.");
                    break;
                }

                // Empty input.
                if (city.isEmpty()) {
                    line("City name cannot be empty.");
                    continue;
                }

                try {

                    searchCity(city);

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                    line("Request was interrupted.");
                    break;

                } catch (IOException e) {

                    line("Network or API error: " + e.getMessage());

                } catch (Exception e) {

                    line("Unexpected error: " + e.getMessage());
                }

                line("\n------------------------------------");
                line("You can search for another city.");
                line("------------------------------------");
            }

        } catch (Exception e) {

            line("Application error: " + e.getMessage());
        }
    }
}
