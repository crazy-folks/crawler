import com.recruiterbox.*;

public class TestCrawl {
	public static void main(String[] args) {
		String url = "";
		//url = "http://e-mailid.blogspot.in";
		url=args[0];
		Crawl c = new Crawl();
		c.setPrintSummaryEnabled(true);

		try {
			c.crawlPage(url, "D:\\emailids.csv");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
