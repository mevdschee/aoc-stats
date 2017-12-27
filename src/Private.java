import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.json.JSONObject;

public class Private {

	public static void main(String[] args) throws IOException {
		String input = new String(Files.readAllBytes(Paths.get("input/69394.json"))).trim();
		JSONObject event = new JSONObject(input);
		int year = Integer.parseInt(event.getString("event"));
		JSONObject members = event.getJSONObject("members");
		int days = 25;
		int stars = 2;

		LinkedHashMap<String, HashMap<String, LinkedHashMap<String, String>>> data = new LinkedHashMap<>();
		for (int day = 1; day <= days; day++) {
			String dayKey = String.valueOf(day);
			if (!data.containsKey(dayKey)) {
				data.put(dayKey, new HashMap<>());
			}
			for (String id : members.keySet()) {
				JSONObject member = members.getJSONObject(id);
				String prefix = String.format("%08X ", member.getInt("id"));
				String name = prefix
						+ java.text.Normalizer.normalize(member.getString("name"), java.text.Normalizer.Form.NFD)
								.replaceAll("[^#\\(\\)\\[\\]a-zA-Z0-9 '-.]", "");
				JSONObject scores = member.getJSONObject("completion_day_level");
				if (scores.has(dayKey)) {
					JSONObject dayScores = scores.getJSONObject(dayKey);
					for (int star = 1; star <= stars; star++) {
						String starKey = String.valueOf(star);
						if (!data.get(dayKey).containsKey(starKey)) {
							data.get(dayKey).put(starKey, new LinkedHashMap<>());
						}
						if (dayScores.has(starKey)) {
							String time = dayScores.getJSONObject(starKey).getString("get_star_ts");
							data.get(dayKey).get(starKey).put(name, time);
						}
					}
				}
			}
			for (int star = 1; star <= stars; star++) {
				String starKey = String.valueOf(star);
				if (!data.get(dayKey).containsKey(starKey)) {
					data.get(dayKey).put(starKey, new LinkedHashMap<>());
				}
				LinkedHashMap<String, String> scores = data.get(dayKey).get(starKey);
				LinkedList<Entry<String, String>> list = new LinkedList<>(scores.entrySet());
				Collections.sort(list,
						(Entry<String, String> o1, Entry<String, String> o2) -> o1.getValue().compareTo(o2.getValue()));

				LinkedHashMap<String, String> sorted = new LinkedHashMap<>();
				for (Entry<String, String> entry : list) {
					sorted.put(entry.getKey(), entry.getValue());
				}

				data.get(dayKey).put(starKey, sorted);
			}
		}

		StringBuilder csv = new StringBuilder();
		csv.append("year,day,stars,position,name,seconds\n");
		StringBuilder tsv = new StringBuilder();
		tsv.append("year\tday\tstars\tposition\tname\tseconds\n");
		StringBuilder sql = new StringBuilder();
		sql.append("DROP TABLE IF EXISTS `scores`;\n");
		sql.append("CREATE TABLE `scores` (\n");
		sql.append("  `id` int(11) NOT NULL AUTO_INCREMENT,\n");
		sql.append("  `year` int(11) NOT NULL,\n");
		sql.append("  `day` int(11) NOT NULL,\n");
		sql.append("  `stars` int(11) NOT NULL,\n");
		sql.append("  `position` int(11) NOT NULL,\n");
		sql.append("  `name` varchar(255) NOT NULL,\n");
		sql.append("  `seconds` int(11) NOT NULL,\n");
		sql.append("  PRIMARY KEY (`id`)\n");
		sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;\n\n");
		for (int day = 1; day <= days; day++) {
			String dayKey = String.valueOf(day);
			for (int star = stars; star > 0; star--) {
				String starKey = String.valueOf(star);
				int position = 1;
				LinkedHashMap<String, String> scores = data.get(dayKey).get(starKey);
				for (String name : scores.keySet()) {
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX'00'");
					String time1 = String.format("%04d-12-%02dT00:00:00-0500", year, day);
					String time2 = data.get(dayKey).get(starKey).get(name);
					long t1 = 0;
					long t2 = 0;
					try {
						t1 = format.parse(time1).getTime();
						t2 = format.parse(time2).getTime();
					} catch (ParseException e) {
						System.out.println("Parse error dates");
						System.exit(1);
					}
					int seconds = (int) ((t2 - t1) / 1000);
					csv.append(year + "," + day + "," + star + "," + position + "," + name + "," + seconds + "\n");
					tsv.append(year + "\t" + day + "\t" + star + "\t" + position + "\t" + name + "\t" + seconds + "\n");
					sql.append("INSERT INTO `scores` (`year`, `day`, `stars`, `position`, `name`, `seconds`) VALUES ");
					sql.append("(" + year + "," + day + "," + star + "," + position + ",\"" + name + "\"," + seconds
							+ ");\n");

					position++;
				}
			}
		}
		Files.write(Paths.get("output/pscores.csv"), csv.toString().getBytes());
		Files.write(Paths.get("output/pscores.tsv"), tsv.toString().getBytes());
		Files.write(Paths.get("output/pscores.sql"), sql.toString().getBytes());
	}

}
