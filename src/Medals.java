import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

public class Medals {

	public static void main(String[] args) throws IOException {
		String[] lines = new String(Files.readAllBytes(Paths.get("output/scores.tsv"))).trim().split("\n");
		LinkedHashMap<String, LinkedHashMap<String, ArrayList<Integer>>> medalList = new LinkedHashMap<>();
		boolean header = true;
		for (String line : lines) {
			if (header) {
				header = false;
				continue;
			}
			String[] fields = line.split("\t");
			String year = fields[0];
			int star = Integer.parseInt(fields[2]);
			int position = Integer.parseInt(fields[3]);
			if (star != 2 || position > 3) {
				continue;
			}
			String name = fields[4];
			if (!medalList.containsKey(year)) {
				medalList.put(year, new LinkedHashMap<>());
			}
			if (!medalList.get(year).containsKey(name)) {
				medalList.get(year).put(name, new ArrayList<>());
			}
			medalList.get(year).get(name).add(position);
		}

		String json = "{";
		int i = 0;
		for (String year : medalList.keySet()) {
			json += (i > 0 ? "," : "") + "\"" + year + "\":{";

			LinkedList<Entry<String, ArrayList<Integer>>> list = new LinkedList<>(medalList.get(year).entrySet());
			Collections.sort(list, (Entry<String, ArrayList<Integer>> o1, Entry<String, ArrayList<Integer>> o2) -> {
				if (o2.getValue().size() == o1.getValue().size()) {
					int score1 = 0;
					for (int j = 0; j < o1.getValue().size(); j++) {
						score1 += 4 - o1.getValue().get(j);
					}
					int score2 = 0;
					for (int j = 0; j < o2.getValue().size(); j++) {
						score2 += 4 - o2.getValue().get(j);
					}
					return Integer.compare(score2, score1);
				} else {
					return Integer.compare(o2.getValue().size(), o1.getValue().size());
				}
			});

			LinkedHashMap<String, ArrayList<Integer>> sorted = new LinkedHashMap<>();
			for (Entry<String, ArrayList<Integer>> entry : list) {
				sorted.put(entry.getKey(), entry.getValue());
			}

			LinkedHashMap<String, HashMap<Integer, String>> positions = new LinkedHashMap<>();
			header = true;
			int max = 0;
			for (String line : lines) {
				if (header) {
					header = false;
					continue;
				}
				String[] fields = line.split("\t");
				int day = Integer.parseInt(fields[1]);
				int star = Integer.parseInt(fields[2]);
				int position = Integer.parseInt(fields[3]);
				if (!fields[0].equals(year) || star != 2) {
					continue;
				}
				String name = fields[4];
				if (!medalList.get(year).containsKey(name)) {
					continue;
				}
				int seconds = Integer.parseInt(fields[5]);
				if (!positions.containsKey(name)) {
					positions.put(name, new HashMap<>());
				}
				positions.get(name).put(day, "[" + position + "," + seconds + "]");
				max = Math.max(max, day);
			}

			int j = 0;
			for (String name : sorted.keySet()) {
				HashMap<Integer, String> medals = positions.get(name);
				json += (j > 0 ? "," : "") + "\"" + name + "\":[";
				for (int day = 1; day <= max; day++) {
					json += (day > 1 ? "," : "");
					if (medals.containsKey(day)) {
						json += medals.get(day);
					} else {
						json += "null";
					}
				}
				json += "]";
				j++;
			}
			json += "}";
			i++;
		}
		json += "}";

		Files.write(Paths.get("output/medals.json"), json.toString().getBytes());
	}

}
