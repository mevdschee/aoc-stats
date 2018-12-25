import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Fastest {

	public static void main(String[] args) throws IOException {
		int[] years = { 2018 };
		int days = 25;
		int positions = 100;
		int stars = 2;
		StringBuilder csv = new StringBuilder();
		csv.append("year,day,min,avg,count,fastest\n");
		HashMap<String, Integer> users;
		HashMap<String, Integer> times;
		for (int year : years) {
			for (int day = 1; day <= days; day++) {
				String filename = "input/" + year + "_day_" + day + ".html";
				if (!Paths.get(filename).toFile().exists()) {
					continue;
				}
				String input = new String(Files.readAllBytes(Paths.get(filename))).trim();
				Pattern p = Pattern
						.compile("time\">[a-zA-Z]+ [0-9]+  ([0-9:]+)<.*?userphoto\">(.*?>([^><]+)<.*?)/div>");
				Matcher m = p.matcher(input);
				int i = 0;
				users = new HashMap<>();
				times = new HashMap<>();
				while (m.find()) {
					int star = 2 - i / positions;
					String[] parts = m.group(1).split(":");
					if (parts.length != 3) {
						System.out.println("Parse error time");
						System.exit(1);
					}
					int seconds = Integer.parseInt(parts[0]) * 3600;
					seconds += Integer.parseInt(parts[1]) * 60;
					seconds += Integer.parseInt(parts[2]);
					String prefix = String.format("%08X ", m.group(2).hashCode());
					String suffix = m.group(2).contains("(AoC++)") ? " (AoC++)" : "";
					String name = prefix + java.text.Normalizer.normalize(m.group(3), java.text.Normalizer.Form.NFD)
							.replaceAll("[^#\\(\\)\\[\\]a-zA-Z0-9 '-.]", "") + suffix;
					if (star == 2) {
						users.put(name, seconds);
					} else {
						if (users.containsKey(name)) {
							times.put(name, users.get(name) - seconds);
						}
					}
					i++;
				}
				if (i != positions * stars) {
					System.out.println("Parse error stars");
					System.exit(1);
				}
				int sum = 0;
				int min = Integer.MAX_VALUE;
				int count = 0;
				String fastest = "";
				for (String name : times.keySet()) {
					int num = times.get(name);
					sum += num;
					count++;
					min = Math.min(min, num);
					if (num == min) {
						fastest = name;
					}
				}
				int avg = sum / count;
				csv.append(year + "," + day + "," + min + "," + avg + "," + count + "," + fastest + "\n");
			}
		}
		Files.write(Paths.get("output/diff.csv"), csv.toString().getBytes());
	}

}
