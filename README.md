SUDRFScraper

DESCRIPTION:

Among russian independent journalists official aggregator bsr.sudrf.ru known as "Pravosudie" is associated with only one feeling - pain.
It is script-based, it is slow, it is off half of the time. And with all these issues it even often does its primary job badly.

So solution was as obvious as necessary. Just re-do aggregation from the courts websites instead of using script built on our taxes.

Though there still were no open-code project to do this thing. 
And it is not much of a surprise: there are no crowds aggregating court cases and the task is not as easy as it seemed to me at the start.

Websites of courts are using different interfaces. Some are using scripts, so selenium is needed anyway.
More important for a user: some courts are with CAPTCHA, though implementation as dumb as possible (basically you need to type one captcha for a whole region).

Nevertheless, SUDRFScraper keeps on winning in compression with "Pravosudie".
It is faster due to its mainly request nature and multi-threading.
It is more stable: there are always courts which are down, but it is bearable considering that whole "Pravosudie" tends to be down.
And, what to be honest, surprises me, it is more complete. 
Every testing shows SUDRFScraper leadership in number of both scraped cases and decision texts.

But this project is my first non-educational project so there is quite a bunch of problems.
It also should be definitely broadened. So the work only begins here.

IMPORTANT THINGS TO KNOW BEFORE USING:

Scraper has been tested only in IntelliJ Idea IDE both on Windows and Linux OS.

Scraper uses FireFox WebDriver, so you should have firefox browser.
If you see errors about WebDriver in log file, follow the instruction that may help:
1. Visit "https://github.com/mozilla/geckodriver/releases" and download driver for your OS.
2. Replace driver in ./src/main/resources/"name-of-your-OS"/.

Scraped cases are only first-instance considered (no appellations).
Scraped cases are among courts of general jurisdiction excluding Mosgorsud and garrison courts.

You are highly not recommended to use for a search only text-in-decision field. 
Most of the courts don't support this field so search will be executed among ALL cases with published decision till this very day.
Use this field only in a bundle with others.

You can search only through one type of articles. If it is not set, search executes among criminal articles.

You are highly not recommended to use bold administrative article search.
Administrative and Civil cases are aggregated simultaneously in many courts, so you need to specify CAS article or filter your results.

EXECUTION INFO:

As for execution you have these search options: 
1. Result date starting from and till. "Result" means case is finished though there might not be a published decision. They are only available date params for now. Though I understand how important can be entry date for some cases. Don't use these fields if you want unfinished cases (for some reason).
2. Text-in-Decision field. Obvious. Just don't forget info introduced in previous chapter.
3. The main option: article. SUDRFScraper supports Criminal Articles, Administrative Offense Articles and Administrative articles. Just don't forget info introduced in previous chapter (x2).

There are only two dump types supported: MySQL-table and line-by-line JSON document.

By the end of execution you are given summery info. It is a list of occurred issues, so you may know how many cases you could miss during the scraping. You also can check logs to find issues info.
If there are many issues that are not include server problems (Inactive court, Connection error) like possible different interfaces I will be glad if you contact me for further improvement of the system.

APPLICATION CONFIGURATION:

You can find configuration properties file by path "/src/main/resources/application.properties".
Things you can configure include:

Directory of dump "basic.result.path": change  value if default way doesn't satisfy you.

Logging of court history "use.court.history".
Don't change this param if you want your File System to be clean.
It logs court issues, time of execution and search request. It can be of any use only for developer.

MySQL Server Connection Info. Fields "sql.usr", "sql.db_url", "sql.password" for user, jdbc database url, password.
You don't have to fill in these fields. You can fill this information while in execution. 

PLANS:

1. Broaden search with region field.
2. Make possible to continue scraping. For example, if scraping was forcibly ended (some scraping requests can last for days) or you need to scrap the second time to get cases from previously inactive courts.
3. Make simple scraping analyzer tool. For common requests like "cases-per-region" or "get number of texts".
4. Make simple default test, comparing Pravosudie with Scraper.
5. Make configuration flexible.
6. Add more tests.

UPDATES:

ver.0.1.4 CAS-UPDATE. Feat: implementation of search based on administrative articles. Small bug fixes and refactoring.

ver 0.1.5 configuration-update. You can change results dump directory modifying application.properties. Small bug fixes and refactoring.

ver.0.1.6 configuration-update. Modified configuration with "use.court.history" configuration param. Now user won't be trashed with thousands of files. Added some tests.
Massive refactoring.

ver.0.1.7 configuration-update. Modified configuration with default sql connection parameters. Massive refactoring.

CONTACT ME:

Email: sudarkinandrew@gmail.com
