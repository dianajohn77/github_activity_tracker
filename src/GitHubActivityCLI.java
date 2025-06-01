import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class GitHubActivityCLI {
    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                System.out.println("Usage <username>");
                return;
            }
            String username = args[0];
            String urlString = "https://api.github.com/users/" + username.trim() + "/events";
            URI uri = URI.create(urlString);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            int responseCode = conn.getResponseCode();
            BufferedReader reader;
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            reader.close();
            printResponse(response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printResponse(String response)  {
        System.out.println("Output: ");
        try {
            JSONArray jsonArray = new JSONArray(response);
            String type;
            String message;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                type = jsonObject.getString("type");
                JSONObject payload = jsonObject.getJSONObject("payload");
                JSONObject repo = jsonObject.getJSONObject("repo");
                switch (type) {
                    case "PushEvent":
                        message = "Pushed " + payload.get("size") + " commits to " + repo.getString("name");
                        break;
                    case "PullRequestEvent":
                        message = "Pull request for " + repo.getString("name");
                        break;
                    case "WatchEvent":
                        message = "Starred " + repo.getString("name");
                        break;
                    case "IssuesEvent":
                        message = payload.getString("action").substring(0, 1).toUpperCase()
                                + payload.getString("action").substring(1) + " an issue in " + repo.getString("name");
                        break;
                    default:
                        message = type + " in " + repo.getString("name");
                        break;
                }
                System.out.println(message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}