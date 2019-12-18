import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileInputStream;



import jhazm.Lemmatizer;
import jhazm.Normalizer;
import jhazm.Stemmer;
import jhazm.tokenizer.WordTokenizer;

public class Main {
	public static void main(String[] args) throws IOException {
//		 Array array = new Array();
//		 array.build(new File("News.xls"), null, null, null);
//		array.addWord("علی", 1, 1);
//		array.addWord("علی", 1, 2);
//		array.addWord("علی", 1, 3);
//		array.addWord("علی", 3, 1);
//		array.addWord("علی", 3, 2);
//		array.addWord("علی", 5, 3);
//		array.addWord("علی", 5, 4);
//		
//		array.addWord("رضا", 1, 4);
//		array.addWord("رضا", 1, 5);
//		array.addWord("رضا", 1, 6);
//		array.addWord("رضا", 2, 4);
//		array.addWord("رضا", 2, 5);
//		array.addWord("رضا", 3, 6);
//		array.addWord("رضا", 3, 7);
		
//		array.search("علی رضا");
		 
		
		String input = "<div itemtype=\"https://schema.org/ImageObject\" itemprop=\"image\" itemscope=\"\"> \n" + 
				" <a itemprop=\"url\" href=\"https://www.ilna.ir/بخش-%D9%88%D8%B1%D8%B2%D8%B4%DB%8C-7/802889-%D8%B9%D8%A7%D9%82%D8%A8%D8%AA-%D9%85%D8%AF%DB%8C%D8%B1%D8%B9%D8%A7%D9%85%D9%84%DB%8C-%D9%81%D8%AA%D8%AD%DB%8C-%D8%AF%D8%B1-%D8%A7%D8%B3%D8%AA%D9%82%D9%84%D8%A7%D9%84-%DA%AF%D8%B2%D8%A7%D8%B1%D8%B4\" title=\"عاقبت مدیرعاملی فتحی در استقلال (گزارش)\" class=\"block mb8\"> <img itemprop=\"contentUrl representativeOfPage\" src=\"https://static1.ilna.ir/thumbnail/9CtdOoD43G8e/zD2ly951Lbe1X4vBXmeYxnwvBKd2o-upCBoRpuXWWQ3xVYIwBtE8SJc5CgG8w1SR1HhaeHPcWe7Z4QQxwQ5aCQuC6bW65Yo2vzNA1A_oYcME-xuDHnCGIL88GkvFaMums2lp5Xzk-eErjUYezqJx1zbHthZiSGB2XuOejRjZDSys4NdjIqNZJEQ6csj1vAj0PYzofVrI7-0,/bujD3OOJbgkf.jpg\" alt=\"عاقبت مدیرعاملی فتحی در استقلال (گزارش)\" class=\"mauto block\" /> </a> \n" + 
				"</div> \n" + 
				"<p class=\"fn14 news_lead pr8 pl8 pt8 pb8\" itemprop=\"description\"> فتحی این روزها در موقعیت بغرنجی است. موقعیت عجیبی که حتی شکست‌های احتمالی استقلال به جای اینکه باعث انتقاد به کادر فنی شود، به پای فتحی نوشته می‌شود. امیرحسین فتحی این روزها حتی نمی‌تواند درباره مترجم تیم با قاطعیت تصمیم‌گیری کند و آبی‌ها اولین تیم ایران هستند که دو مترجم دارند اما هیچ کدام از آنها مترجم زبان مادری مربی (ایتالیایی) نیستند! </p> \n" + 
				"<!-- Content --> \n" + 
				"<p style=\"text-align:justify\">به گزارش ایلنا، امیرحسین فتحی برخلاف خیلی مدیران فوتبالی که تا قبل از ورود به این عرصه ساده‌ترین قوانین فوتبال را نمی‌دانستند و از بد حادثه (شاید هم خوش حادثه) گذارشان به این رشته افتاده، عاشق فوتبال است.&nbsp;</p> \n" + 
				"<p style=\"text-align:justify\">رفقایش می‌گویند استقلالی قدیمی متعصبی است. فتحی از دو دهه پیش پست‌های مدیریتی و اجرایی را انتخاب کرد ولی اگر هوس می‌کرد مربی فوتبال شود، احتمالاً از آن مربیانی می‌شد که تیمش در تمام طول بازی فقط یازده نفره دفاع می‌کرد و بی‌هیچ برنامه‌ای برای حمله، منتظر اتفاق و حادثه برای گل زدن می‌ماند. در دسته‌‌بندی مدیران، فتحی در شمار &laquo;مدیران منفعل&raquo; قرار می‌گیرد. مدیرانی که ویژگی بارز سبک مدیریتی‌شان، این است که سعی می‌کنند همه را راضی نگه دارند. مدیران منفعل، از گرفتن تصمیم‌های بزرگ هراس دارند، برای هر تغییر و تحولی مردد هستند و به جای حل مشکل، آن را به دست زمان می‌سپرند تا مشکل &laquo;کهنه&raquo; شود و تاریخ مصرفش به پایان برسد.</p> \n" + 
				"<p style=\"text-align:justify\">از روزی که امیرحسین فتحی جانشین رضا افتخاری شد، حدود 13 ماه می‌گذرد. هشت ماه ابتدایی دوران فتحی، چندان پرتنش و متلاطم نبود. نهایت هجمه‌ای که در این مقطع به فتحی می‌شد یک مشت انتقاد بی‌مایه از گروه‌های بی‌هویت تلگرامی بود که می‌گفتند &laquo;فتحی از سوی وزیر پرسپولیسی مأمور شده که استقلال را نابود کند تا تیم حکومتی قهرمان شود!&raquo; اما روز دهم فروردین 98، یک نقطه عطف در دوره مدیریتی اوست. روزی که استقلال دربی را باخت، شفر دیگر روی سکوها محبوب نبود و تیمی که با برد مقابل پرسپولیس می‌توانست صدرنشین شود با یک پنالتی پرماجرا، وارد بحران شد. از همینجاست که فتحی خواسته یا ناخواسته از عرصه &laquo;تصمیم‌سازی&raquo; کنار می‌رود و علی خطیر پشت فرمان می‌نشیند.</p> \n" + 
				"<p style=\"text-align:justify\">داستان اختلاف خطیر و شفر که اظهرمن‌الشمس است، پروژه اخراج پیرمرد آلمانی از شب دربی کلید خورده ولی باز هم توان تصمیم گیری وجود ندارد. تیم درهم شکسته استقلال با سایپا مساوی می‌کند، به پدیده می‌بازد و عملاً شانس قهرمانی‌اش به صفر می‌رسد تا تصمیمی که یک ماه پیش گرفته شده عملی شود؛ اخراج شفر. مربی‌ای که در زمستان 97 از سوی فتحی لقب &laquo;بهترین مربی تاریخ استقلال&raquo; را گرفته بود، فقط در پنج ماه به پیرمردی تبدیل می‌شود که &laquo;بضاعتش همین بوده&raquo;. خطیر هم یک گام جلوتر می‌رود و با چسباندن انگ دلالی به شفر، مدیریت پروژه را در دست می‌گیرد.</p> \n" + 
				"<p style=\"text-align:justify\">فصل تمام شده، فتحی نمی‌داند که با فرهاد مجیدی چه باید بکند؟ فرهاد کنار گذاشته شود؟ در یک تصمیم شجاعانه باید تیم فصل آینده را به فرهاد واگذار کرد یا اینکه ستاره همچنان محبوب استقلال باید با عنوان دستیار روی نیمکت بنشیند؟ در این سه راهی تصمیم گیری، باز هم هیچ کنشی در کار نیست. آنقدر زمان می‌گذرد که فایل صوتی فرهاد مجیدی منتشر شود و همزمان فرهاد به تیم ملی امید برود.</p> \n" + 
				"<p style=\"text-align:justify\">انفعال فتحی در دوران نقل و انتقالات هم ادامه دارد. فتحی تمایلی به تصمیم‌گیری ندارد. در روزهایی که ستاره‌های استقلال یکی پس از دیگری بیرون رانده می‌شوند یا حراج می‌شوند، مدیرعامل استقلال به تماشای عرض اندام معاون ورزشی‌اش مشغول است. خطیر بی‌آنکه تجربه‌ای در تیم بستن داشته باشد، مشغول افشاگری و خلوت کردن تیم است. ستاره‌های ملی‌پوش و خارجی‌های مؤثری مثل پاتوسی و طارق همام از باشگاه بیرون رانده می‌شوند و در عوض سیاوش یزدانی مهم‌ترین خرید استقلال می‌شود که در 27 سالگی حتی سابقه یک روز دعوت به اردوی تیم ملی را ندارد. این پازل با ورود استراماچونی تکمیل می‌شود. خبر به خدمت گرفتن استراماچونی در حالی در صفحه شخصی علی خطیر منتشر می‌شود و فتحی درباره اینکه چرا باید استقلال مربی با چنین ویژگی را استخدام کند، باز هم ساکت است. در پروژه پرونده شکایت پروپژیچ، موفقیت استقلال در پرداخت جریمه از پول بلوکه شده در فیفا به پای معاون ورزشی نوشته می‌شود و اینجا هم خبری از مدیرعامل نیست.</p> \n" + 
				"<p style=\"text-align:justify\">فتحی به حاشیه رانده شده اما پس از استعفای خطیر به جای اینکه مدیرعامل دوباره تصمیم ساز اصلی تیم شود، انفعال فتحی نمود بیشتری پیدا می‌کند. استراماچونی دستیار ایرانی نمی‌پذیرد، تهدید به استعفا می‌کند، عوامل رسانه‌ای مربی ایتالیایی، استقلال را به زندان تشبیه می‌کنند که استراماچونی را در آن حبس کرده اما باز هم هیچ خبری از اقتداری که از یک مدیر ورزشی انتظار می‌رود، نیست. فتحی تکذیب می‌کند و می‌گوید &laquo;هیچ مشکلی نیست&raquo;، &laquo;شیطنت بعضی رسانه‌هاست&raquo;، &laquo;رابطه‌ام با مربی ایتالیایی خیلی هم خوب است&raquo; اما واقعیت این است که در استقلال ساده‌ترین مسائل به بحران تبدیل می‌شود. مثل همین ماجرای ورود هروویه میلیچ به ایران و دیپورت شدن او از فرودگاه. مسأله‌ای بسیار ساده و پیش پا افتاده که بهانه‌ای می‌شود استراماچونی با حمایت لشکر هواداران تلگرامی- اینستاگرامی به مدیریت استقلال بتازد.</p> \n" + 
				"<p style=\"text-align:justify\">فتحی این روزها در موقعیت بغرنجی است. موقعیت عجیبی که حتی شکست‌های احتمالی استقلال به جای اینکه باعث انتقاد به کادر فنی شود، به پای فتحی نوشته می‌شود. امیرحسین فتحی این روزها حتی نمی‌تواند درباره مترجم تیم با قاطعیت تصمیم‌گیری کند و آبی‌ها اولین تیم ایران هستند که دو مترجم دارند اما هیچ کدام از آنها مترجم زبان مادری مربی (ایتالیایی) نیستند!</p> \n" + 
				"<p style=\"text-align:justify\">مدیرعامل استقلال این روزها آماج حملات رسانه‌ای است، شاید با استعفا/ اخراج استراماچونی (که هنوز نمی‌دانم این همه محبوبیت را از کجا آورده)، وضعیت به گونه‌ای پیش برود که صندلی‌های مدیریتی فتحی لرزان‌تر از همیشه شود. این نتیجه سبک مدیریتی منفعلی است که فتحی در استقلال انتخاب کرده. اگر فتحی بر استراتژی قبلی خودش اصرار داشته باشد، شاید فردا اوضاع برای او دشوارتر از امروز باشد.</p>";
		
		String noTag = Jsoup.parse(input).text();
		System.out.println(noTag);
		
		String noPunctuationTemp = noTag.replaceAll("\\p{Punct}", "");
		System.out.println(noPunctuationTemp);
		
		String noPunctuation = noPunctuationTemp.replaceAll("،", "");
		System.out.println(noPunctuation);
		
		List<String> strs = new WordTokenizer().tokenize(new Normalizer().run(noPunctuation));
		for (String s : strs) {
			String output = new Lemmatizer().lemmatize(s);
			System.out.println(output);
		}
		
		
		
	}
}