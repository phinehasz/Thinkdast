package thinkdast;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WikiPhilosophy {

	final static List<String> visited = new ArrayList<String>();
	final static WikiFetcher wf = new WikiFetcher();
	private static final String WIKI_ORIGINAL = "https://en.wikipedia.org";

	/**
	 * Tests a conjecture about Wikipedia and Philosophy.
	 *
	 * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
	 *
	 * 1. Clicking on the first non-parenthesized, non-italicized link
	 * 2. Ignoring external links, links to the current page, or red links
	 * 3. Stopping when reaching "Philosophy", a page with no links or a page
	 *    that does not exist, or when a loop occurs
	 *
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String destination = "https://en.wikipedia.org/wiki/Philosophy";
		String dest = "https://en.wikipedia.org/wiki/Mathematics";
		String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";

		testConjecture(destination, dest, 10);
	}

	/**
	 * Starts from given URL and follows first link until it finds the destination or exceeds the limit.
	 *
	 * @param destination
	 * @param source
	 * @throws IOException
	 */
	public static void testConjecture(String destination, String source, int limit) throws IOException {
		String url = source;
		boolean res = false;
		for (int i = limit; i > 0; i--) {
			Elements elements = wf.fetchWikipedia(url);
			url = parseParentNodes(elements);
			if (url != null && url.equals(destination)) {
				res = true;
				break;
			}
		}
		if (res) {
			System.out.println("Success");
		} else {
			System.out.println("Fail");
		}
	}

	private static String parseParentNodes(Elements elements) {
		String url = null;

		for (Element elem : elements) {
//			System.out.println(elem);
			url = checkBrackets(elem);
			if (url != null) {
				break;
			}
		}
		return url;
	}

	private static String manageBrackets(String str, int first) {
		Deque<Character> stack = new ArrayDeque<>();
		char[] array = str.toCharArray();
		stack.push(array[first]);
		int i = first + 1;
		for ( ; i < array.length; i++) {
			if (array[i] == ')') {
				stack.pop();
			} else if (array[i] == '(') {
				stack.push(array[i]);
			}
			if (array[i] == '<' && array[i + 1] == 'a' && stack.size() == 0) {
				break;
			}
		}
		str = str.substring(i);
		str = getLink(str);
		return str;
	}

	private static String checkBrackets(Element element) {
		String str = element.toString();
		int firstBracket = str.indexOf("(");
		int firstLink = str.indexOf("<a href=");
		if (firstBracket > -1 && firstBracket < firstLink) {
			return manageBrackets(str, firstBracket);
		} else {
			return parseChildNodes(element.children());
		}
	}

	private static String parseChildNodes(Elements childNodes) {
		String url = null;
		for (Element child : childNodes) {
//            System.out.println(child);
			if (child.tagName().equals("a")) {
				url = getLink(child.toString());
				break;
			}
		}
		return url;
	}

	private static String getLink(String str) {
		int firstComma = str.indexOf("\"");
		int secondComma = str.indexOf("\"", firstComma + 1);
		if (firstComma == -1 || secondComma == -1) {
			return null;
		}
		str = str.substring(firstComma + 1, secondComma);
		return WIKI_ORIGINAL + str;
	}
}
