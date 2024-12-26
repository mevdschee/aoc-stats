import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Analyze {

	public static void main(String[] args) throws IOException {
		int[] years = { 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024 };
		int days = 25;
		int positions = 100;
		int stars = 2;
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
		for (int year : years) {
			for (int day = 1; day <= days; day++) {
				String filename = "input/" + year + "_day_" + day + ".html";
				if (!Paths.get(filename).toFile().exists()) {
					continue;
				}
				String input = new String(Files.readAllBytes(Paths.get(filename))).trim();
				Pattern p = Pattern.compile(
						"position\">([ 0-9]+)\\)<.*?time\">[a-zA-Z]+ [0-9]+  ([0-9:]+)<.*?userphoto\">(.*?>([^><]+)<.*?)/div>");
				Matcher m = p.matcher(input);
				int i = 0;
				while (m.find()) {
					int position = i % positions + 1;
					if (position != Integer.parseInt(m.group(1).trim())) {
						System.out.println("Parse error pos" + m.group());
						System.exit(1);
					}
					int star = 2 - i / positions;
					String[] parts = m.group(2).split(":");
					if (parts.length != 3) {
						System.out.println("Parse error time");
						System.exit(1);
					}
					int seconds = Integer.parseInt(parts[0]) * 3600;
					seconds += Integer.parseInt(parts[1]) * 60;
					seconds += Integer.parseInt(parts[2]);
					String prefix = String.format("%08X ", m.group(3).hashCode());
					String suffix = m.group(3).contains("(AoC++)") ? " (AoC++)" : "";
					String name = prefix + java.text.Normalizer.normalize(m.group(4), java.text.Normalizer.Form.NFD)
							.replaceAll("[^#\\(\\)\\[\\]a-zA-Z0-9 '-.]", "") + suffix;
					csv.append(year + "," + day + "," + star + "," + position + "," + name + "," + seconds + "\n");
					tsv.append(year + "\t" + day + "\t" + star + "\t" + position + "\t" + name + "\t" + seconds + "\n");
					sql.append("INSERT INTO `scores` (`year`, `day`, `stars`, `position`, `name`, `seconds`) VALUES ");
					sql.append("(" + year + "," + day + "," + star + "," + position + ",\"" + name + "\"," + seconds
							+ ");\n");
					i++;
				}
				if (i != positions * stars) {
					System.out.println("Parse error stars");
					System.exit(1);
				}
			}
		}
		Files.write(Paths.get("output/scores.csv"), csv.toString().getBytes());
		Files.write(Paths.get("output/scores.tsv"), tsv.toString().getBytes());
		Files.write(Paths.get("output/scores.sql"), sql.toString().getBytes());
	}

}
