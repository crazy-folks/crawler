package com.recruiterbox;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class Crawl {

	private Set<String> URLProcessed = new HashSet<String>();
	private Set<String> emailIDsFound = new HashSet<String>();
	private String inputCrawlURL = null;
	private String inputURLHost = null;
	private boolean printSummaryEnabled = false;
	BufferedWriter bw = null;

	/**
	 * This returns the status of property PrintSummaryEnabled
	 * 
	 * @return boolean
	 * 			PrintSummaryEnabled flag.
	 */
	public boolean isPrintSummaryEnabled()
	{
		return printSummaryEnabled;
	}

	/**
	 * Sets the value for the flag PrintSummaryEnabled If true the summary will
	 * be printed in the standard output
	 * 
	 * @param printSummaryEnabled
	 */
	public void setPrintSummaryEnabled(boolean printSummaryEnabled) {
		this.printSummaryEnabled = printSummaryEnabled;
	}

	private String extractEmail(String content) {
		content = RemoveTag(content);
		String email = null;
		String regex = "(\\w+)(\\.\\w+)*@(\\w+\\.)(\\w+)(\\.\\w+)*";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			email = matcher.group();
			if (!isValidEmailAddress(email)) {
				email = null;
			}
		}
		return email;
	}

	private boolean isValidEmailAddress(String emailAddress) {
		String expression = "^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}";
		CharSequence inputStr = emailAddress;
		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		return matcher.matches();
	}

	private String RemoveTag(String html) {
		html = html.replaceAll("</.*?>", " ");
		html = html.replaceAll("&nbsp;", " ");
		html = html.replaceAll("&amp;", " ");
		html = html.replaceAll(";", " ");
		html = html.replaceAll("\t", " ");
		html = html.replaceAll("\r", " ");
		html = html.replaceAll("'", " ");
		html = html.replaceAll("\"", " ");
		html = html.replaceAll("<", " ");
		html = html.replaceAll("/>", " ");
		html = html.replaceAll(">", " ");
		html = html.replaceAll("=", " ");
		html = html.replaceAll("mailto:", " ");
		return html;
	}

	private boolean isURL(String url) {
		if (url == null) {
			return false;
		}
		// Assigning the url format regular expression
		String urlPattern = "^http(s{0,1})://[a-zA-Z0-9_/\\-\\.]+\\.([A-Za-z/]{2,5})[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*";
		return url.matches(urlPattern);
	}

	private String trimTrailingSlash(String url) {
		while (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		return url;
	}

	private void printSummary() {
		System.out.println("SUMMARY");
		System.out.println("=======");
		System.out.println("PROCESSED URL : " + inputCrawlURL);
		System.out.println("PROCESSED PAGE LINKS : "
				+ (URLProcessed.size() - 1));
		System.out.println("FOUND EMAIL IDs : " + emailIDsFound.size());
	}

	/**
	 * Crawls the email IDs from given URL and writes into the given file in CSV
	 * format. Format of CSV file will be HomeURL,PageLink,email-ID
	 * 
	 * @param url
	 *            Home URL to Crawl
	 * @param fileToWrite
	 *            File in which the output to be written
	 * 
	 * @throws Exception
	 */
	public void crawlPage(String url, String fileToWrite) throws Exception {
		File outputFile = new File(fileToWrite);
		crawlPage(url, outputFile);
	}

	/**
	 * Crawls the email IDs from given list of home URLs and writes into the given file in CSV
	 * format. Format of CSV file will be HomeURL,PageLink,email-ID
	 * 
	 * @param listOfURLs
	 *            Home URLs to Crawl
	 * @param outputFile
	 *            File in which the output to be written
	 * 
	 * @throws Exception
	 */
	public void crawlPage(List<String> listOfURLs, File outputFile)
			throws Exception {
		try {
			bw = new BufferedWriter(new FileWriter(outputFile));
		} catch (FileNotFoundException fnfe) {
			throw new FileNotFoundException(fnfe.getMessage());
		}

		for (String url : listOfURLs) {
			crawlPage(url, outputFile);
		}
		bw.close();
	}

	/**
	 * Crawls the email IDs from given URL and writes into the given file in CSV
	 * format. Format of CSV file will be HomeURL,PageLink,email-ID
	 * 
	 * @param url
	 *            Home URL to Crawl
	 * @param outputFile
	 *            File in which the output to be written
	 * 
	 * @throws Exception
	 */

	public void crawlPage(String url, File outputFile) throws Exception {
		boolean closeBufferedWriter = false;
		if (bw == null) {
			try {
				bw = new BufferedWriter(new FileWriter(outputFile));
			} catch (FileNotFoundException fnfe) {
				throw new FileNotFoundException(fnfe.getMessage());
			}
			closeBufferedWriter = true;
		}
		clearData();
		inputCrawlURL = url;
		url = trimTrailingSlash(url.trim());
		if (!url.startsWith("http://", 0) && !url.startsWith("https://", 0)) {
			url = "http://" + url;
		}

		if (isURL(url)) {
			URL webURL;

			try {
				webURL = new URL(url);
				inputURLHost = webURL.getHost();
				processCrawlRequest(url, webURL);

			} catch (MalformedURLException e) {
				e.printStackTrace();
				System.exit(0);
			}
			if (printSummaryEnabled) {
				printSummary();
			}
		} else {
			System.out.println("Not valid url : " + inputCrawlURL);
		}
		if(closeBufferedWriter) {
			bw.close();
		}
	}

	private void processCrawlRequest(String pageurl, URL url) {

		try {

			URLProcessed.add(pageurl);
			// System.out.println("Processing " + Pageurl);
			URLConnection yc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			String inputLine = null;
			String previousToken = "";
			while ((inputLine = in.readLine()) != null) {
				inputLine = RemoveTag(inputLine);
				// System.out.println(inputLine);

				StringTokenizer st = new StringTokenizer(inputLine, " ");

				while (st.hasMoreTokens()) {
					String key = trimTrailingSlash(st.nextToken().trim());

					// If the previous tag is href and not detected as URL then
					// it should be a relavite URL
					if ("href".equalsIgnoreCase(previousToken) && !isURL(key)) {
						if (!key.startsWith("//")) {
							if (!key.startsWith("/")) {
								key = "/" + key;
							}
							if (url.getPath() == null) {
								key = pageurl + key;
							} else {
								String pathWOFile = pageurl.substring(0,
										pageurl.lastIndexOf("/"));
								key = pathWOFile + key;
							}
						}
					}

					if (isURL(key)) {
						if (!URLProcessed.contains(key)) {
							URL pageLink = null;
							try {
								pageLink = new URL(key);
							} catch (MalformedURLException mue) {
								// Skipping the URL link crawling processing
								continue;
							}

							if (inputURLHost.equals(pageLink.getHost())) {
								// System.out.println("URL = " + key);
								processCrawlRequest(key, pageLink);
							}
						}
					} else {

						String emailID = extractEmail(key);
						if (emailID != null) {
							if (!emailIDsFound.contains(emailID)) {
								emailIDsFound.add(emailID);
								bw.write(inputCrawlURL + "," + pageurl + ","
										+ emailID);
								bw.newLine();
								if (emailIDsFound.size() % 50 == 0) {
									bw.flush();
								}
							}
						}

					}
					previousToken = key;
				}
			}
			bw.flush();
			in.close();

		} catch (UnknownHostException uhe) {
			System.err
					.println("Check is the provided URL is correct and the "
							+ "Internet connection is available is this is the external URL");
		} catch (Exception e) {

		}
	}

	/**
	 * If using the same instance of Crawl for multiple crawlPage request,
	 * it will hold the details of already discovered email ids and page links.
	 * So, it won't search those links and write already discovered mailids to the file.
	 * If needs to clear the discovered mailIDs and page links, call this method. 
	 * 
	 */
	public void clearData() {
		URLProcessed.clear();
		emailIDsFound.clear();
	}

}