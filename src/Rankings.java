import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.json.JSONObject;

public class Rankings {

	public static void main(String[] args) throws IOException, ParseException {
		String input = new String(Files.readAllBytes(Paths.get("output/scores.tsv"))).trim();
		String[] lines = input.split("\n");
		StringBuilder csv = new StringBuilder();
		csv.append("year,day,position,points,name\n");
		StringBuilder tsv = new StringBuilder();
		tsv.append("year\tday\tposition\tpoints\tname\n");
		StringBuilder sql = new StringBuilder();
		sql.append("DROP TABLE IF EXISTS `scores`;\n");
		sql.append("CREATE TABLE `scores` (\n");
		sql.append("  `id` int(11) NOT NULL AUTO_INCREMENT,\n");
		sql.append("  `year` int(11) NOT NULL,\n");
		sql.append("  `day` int(11) NOT NULL,\n");
		sql.append("  `position` int(11) NOT NULL,\n");
		sql.append("  `points` int(11) NOT NULL,\n");
		sql.append("  `name` varchar(255) NOT NULL,\n");
		sql.append("  PRIMARY KEY (`id`)\n");
		sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;\n\n");
		int event = 2019;
		int owner_id = 0;
		JSONObject json = new JSONObject();
		json.put("event", String.valueOf(event));
		json.put("owner_id", String.valueOf(owner_id));
		json.put("members", new JSONObject());
		HashMap<String, Integer> scores = new HashMap<>();
		for (int i = 1; i < lines.length; i++) {
			String[] score = lines[i].split("\t");
			int year = Integer.parseInt(score[0]);
			String day = score[1];
			String stars = score[2];
			int position = Integer.parseInt(score[3]);
			String name = score[4];
			int seconds = Integer.parseInt(score[5]);
			int points = 101 - position;
			String userId = name.split(" ", 2)[0];
			if (year != event) {
				continue;
			}
			if (!json.getJSONObject("members").has(userId)) {
				JSONObject member = new JSONObject();
				JSONObject completion = new JSONObject();
				member.put("completion_day_level", completion);
				member.put("name", name.split(" ", 2)[1]);
				member.put("local_score", 0);
				member.put("global_score", 0);
				member.put("id", userId);
				member.put("stars", 0);
				json.getJSONObject("members").put(userId, member);
			}
			JSONObject member = json.getJSONObject("members").getJSONObject(userId);
			JSONObject completion = member.getJSONObject("completion_day_level");
			if (!completion.has(day)) {
				completion.put(day, new JSONObject());
			}
			JSONObject d = completion.getJSONObject(day);
			JSONObject ts = new JSONObject();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX'00'");
			SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX'00'");
			String zeroday = String.format("%02d", Integer.parseInt(day));
			long start = format.parse("2017-12-" + zeroday + "T00:00:00-0500").getTime();
			String time = outFormat.format(new Date(start + seconds * 1000 + position));
			ts.put("get_star_ts", time);
			d.put(stars, ts);
			long last_ts = 0;
			if (member.has("last_star_ts")) {
				last_ts = format.parse(member.getString("last_star_ts")).getTime();
			}
			member.put("last_star_ts", format.format(Math.max(last_ts, start + seconds)));
			member.put("local_score", member.getInt("local_score") + points);
			member.put("global_score", member.getInt("global_score") + points);
			member.put("stars", member.getInt("stars") + 1);
			scores.put(name, member.getInt("global_score"));

			// sort
			if (position == 100 && stars.equals("1")) {
				LinkedList<Entry<String, Integer>> list = new LinkedList<>(scores.entrySet());
				Collections.sort(list, (Entry<String, Integer> o1, Entry<String, Integer> o2) -> {
					return Integer.compare(o2.getValue(), o1.getValue());
				});
				for (int j = 0; j < list.size(); j++) {
					Entry<String, Integer> entry = list.get(j);
					String uname = entry.getKey();
					int upoints = entry.getValue();
					int uposition = j + 1;
					csv.append(year + "," + day + "," + uposition + "," + upoints + "," + uname + "\n");
					tsv.append(year + "\t" + day + "\t" + uposition + "\t" + upoints + "\t" + uname + "\n");
					sql.append("INSERT INTO `scores` (`year`, `day`, `position`, `points`, `name`) VALUES ");
					sql.append("(" + year + "," + day + "," + uposition + "," + upoints + ",\"" + uname + "\");\n");
				}
			}
		}
		Files.write(Paths.get("output/rankings.csv"), csv.toString().getBytes());
		Files.write(Paths.get("output/rankings.tsv"), tsv.toString().getBytes());
		Files.write(Paths.get("output/rankings.sql"), sql.toString().getBytes());
		Files.write(Paths.get("output/scores.json"), json.toString().getBytes());
	}
}