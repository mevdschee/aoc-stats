import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONObject;

public class Cheats {

	public static void main(String[] args) throws IOException {
		String csv = new String(Files.readAllBytes(Paths.get("output/diff.csv"))).trim();
		String input = new String(Files.readAllBytes(Paths.get("input/361157.json"))).trim();
		JSONObject json = new JSONObject(input).getJSONObject("members");
		String[] days = csv.split("\n");
		for (int day = 1; day < days.length; day++) {
			String[] fields = days[day].split(",");
			int min = Integer.parseInt(fields[2]);
			int avg = Integer.parseInt(fields[3]);
			int count = Integer.parseInt(fields[4]);
			String fastest = fields[5];
			for (String id : json.keySet()) {
				JSONObject member = json.getJSONObject(id);
				String name = member.getString("name");
				JSONObject scores = member.getJSONObject("completion_day_level");
				String dayKey = String.valueOf(day);
				if (scores.has(dayKey)) {
					JSONObject dayScores = scores.getJSONObject(dayKey);
					if (dayScores.has("1") && dayScores.has("2")) {
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX'00'");
						String next = String.format("%02d", day + 1);
						long t1 = Long.parseLong(dayScores.getJSONObject("1").getString("get_star_ts"))*1000;
						long t2 = Long.parseLong(dayScores.getJSONObject("2").getString("get_star_ts"))*1000;
						long limit = 0;
						try {
							limit = format.parse("2018-12-" + next + "T00:00:00-0500").getTime();
						} catch (ParseException e) {
							System.out.println("Parse error dates");
							System.exit(1);
						}
						float diff = (t2 - t1) / 1000;
						if (diff / min < 2 && diff < avg / 1) {
							if (!name.equals("XebiaAoC")) {
								System.out.println("'" + name + "' answered " + ((t2 > limit) ? "(on other day) " : "")
									+ "in " + (int) diff + " on day " + day + " where " + min + " by '" + fastest
									+ "' was best and " + avg + " was average of " + count + " players");
							}
						}
					}

				}
			}
		}
	}

}
