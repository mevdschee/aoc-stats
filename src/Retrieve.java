import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;

public class Retrieve {

	public static void main(String[] args) throws IOException {

		int[] years = { 2015, 2016, 2017, 2018, 2019 };
		int days = 25;
		if (!Paths.get("input").toFile().exists()) {
			if (!Paths.get("input").toFile().mkdir()) {
				System.out.println("Create dir error");
				System.exit(1);
			}
		}
		Calendar c = Calendar.getInstance();
		for (int year : years) {
			for (int day = 1; day <= days; day++) {
				if (day>c.get(Calendar.DAY_OF_MONTH) && Calendar.DECEMBER == c.get(Calendar.MONTH) && year == c.get(Calendar.YEAR)) {
					break;
				}
				String filename = "input/" + year + "_day_" + day + ".html";
				if (Paths.get(filename).toFile().exists()) {
					System.out.println("Skipping: " + filename);
					continue;
				}
				String url = "https://adventofcode.com/" + year + "/leaderboard/day/" + day;
				StringBuilder result = new StringBuilder();
				URLConnection con = (new URL(url)).openConnection();
				BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String line;
				while ((line = rd.readLine()) != null) {
					result.append(line + "\n");
				}
				rd.close();
				Files.write(Paths.get(filename), result.toString().getBytes());
				System.out.print("Written: " + filename);
				for (int i = 0; i < 10; i++) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// ignore
					}
					System.out.print(".");
					System.out.flush();
				}
			}
		}
		System.out.println("done");
	}

};