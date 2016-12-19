package serilogj.parsing;

import java.util.ArrayList;

import serilogj.core.IMessageTemplateParser;
import serilogj.events.MessageTemplate;

public class MessageTemplateParser implements IMessageTemplateParser {
	private class ParseResult {
		private ParseResult(int nextIndex, MessageTemplateToken token) {
			this.nextIndex = nextIndex;
			this.token = token;
		}

		private int nextIndex;
		private MessageTemplateToken token;
	}

	public MessageTemplate parse(String messageTemplate) {
		if (messageTemplate == null) {
			throw new IllegalArgumentException("messageTemplate");
		}

		return new MessageTemplate(messageTemplate, tokenize(messageTemplate));
	}

	private ArrayList<MessageTemplateToken> tokenize(String messageTemplate) {
		ArrayList<MessageTemplateToken> tokens = new ArrayList<MessageTemplateToken>();

		if (messageTemplate.equals("")) {
			tokens.add(new TextToken(""));
			return tokens;
		}

		ParseResult result;
		int nextIndex = 0;
		while (true) {
			result = parseTextToken(nextIndex, messageTemplate);
			if (result.nextIndex > nextIndex) {
				tokens.add(result.token);
			}

			if (result.nextIndex == messageTemplate.length()) {
				return tokens;
			}

			nextIndex = result.nextIndex;
			result = parsePropertyToken(nextIndex, messageTemplate);
			if (result.nextIndex > nextIndex) {
				tokens.add(result.token);
			}

			if (result.nextIndex == messageTemplate.length()) {
				return tokens;
			}
			nextIndex = result.nextIndex;
		}
	}

	private ParseResult parseTextToken(int startAt, String messageTemplate) {
		int first = startAt;

		StringBuilder accum = new StringBuilder();
		do {
			char nc = messageTemplate.charAt(startAt);
			if (nc == '{') {
				if (startAt + 1 < messageTemplate.length() && messageTemplate.charAt(startAt + 1) == '{') {
					accum.append(nc);
				} else {
					break;
				}
			} else {
				accum.append(nc);
				// Check for double }}
				if (nc == '}') {
					if (startAt + 1 < messageTemplate.length() && messageTemplate.charAt(startAt + 1) == '}') {
						startAt++;
					}
				}
			}

			startAt++;
		} while (startAt < messageTemplate.length());

		return new ParseResult(startAt, new TextToken(accum.toString(), first));
	}

	private ParseResult parsePropertyToken(int startAt, String messageTemplate) {
		int first = startAt;
		startAt++;
		while (startAt < messageTemplate.length() && isValidInPropertyTag(messageTemplate.charAt(startAt))) {
			startAt++;
		}

		if (startAt == messageTemplate.length() || messageTemplate.charAt(startAt) != '}') {
			return new ParseResult(startAt, new TextToken(messageTemplate.substring(first, startAt), first));
		}

		int next = startAt + 1;

		String rawText = messageTemplate.substring(first, next);
		String tagContent = messageTemplate.substring(first + 1, first + 1 + next - (first + 2));
		if (tagContent.length() == 0 || !isValidInPropertyTag(tagContent.charAt(0))) {
			return new ParseResult(next, new TextToken(rawText, first));
		}

		SplitTagContentResult result = trySplitTagContent(tagContent);
		String propertyName = result.propertyNameAndDestructuring;
		String format = result.format;
		String alignment = result.alignment;
		if (!result.isValid) {
			return new ParseResult(next, new TextToken(rawText, first));
		}

		Destructuring destructuring = tryGetDestructuringHint(propertyName.charAt(0));
		if (destructuring != Destructuring.Default) {
			propertyName = propertyName.substring(1);
		}

		if (propertyName.equals("") || !isValidInPropertyName(propertyName.charAt(0))) {
			return new ParseResult(next, new TextToken(rawText, first));
		}

		for (int i = 0; i < propertyName.length(); ++i) {
			char c = propertyName.charAt(i);
			if (!isValidInPropertyName(c)) {
				return new ParseResult(next, new TextToken(rawText, first));
			}
		}

		if (format != null) {
			for (int i = 0; i < format.length(); ++i) {
				char c = format.charAt(i);
				if (!isValidInFormat(c)) {
					return new ParseResult(next, new TextToken(rawText, first));
				}
			}
		}

		Alignment alignmentValue = null;
		if (alignment != null) {
			for (int i = 0; i < alignment.length(); ++i) {
				char c = alignment.charAt(i);
				if (!isValidInAlignment(c)) {
					return new ParseResult(next, new TextToken(rawText, first));
				}
			}

			int lastDash = alignment.lastIndexOf('-');
			if (lastDash > 0) {
				return new ParseResult(next, new TextToken(rawText, first));
			}

			int width = lastDash == -1 ? Integer.parseInt(alignment) : Integer.parseInt(alignment.substring(1));
			if (width == 0) {
				return new ParseResult(next, new TextToken(rawText, first));
			}

			AlignmentDirection direction = lastDash == -1 ? AlignmentDirection.Right : AlignmentDirection.Left;

			alignmentValue = new Alignment(direction, width);
		}

		return new ParseResult(next,
				new PropertyToken(propertyName, rawText, format, alignmentValue, destructuring, first));
	}

	private class SplitTagContentResult {
		private SplitTagContentResult() {
			isValid = false;
			propertyNameAndDestructuring = null;
			format = null;
			alignment = null;
		}

		private boolean isValid;
		private String propertyNameAndDestructuring;
		private String format;
		private String alignment;
	}

	private SplitTagContentResult trySplitTagContent(String tagContent) {
		SplitTagContentResult result = new SplitTagContentResult();

		int formatDelim = tagContent.indexOf(':');
		int alignmentDelim = tagContent.indexOf(',');
		if (formatDelim == -1 && alignmentDelim == -1) {
			result.propertyNameAndDestructuring = tagContent;
		} else {
			if (alignmentDelim == -1 || (formatDelim != -1 && alignmentDelim > formatDelim)) {
				result.propertyNameAndDestructuring = tagContent.substring(0, formatDelim);
				result.format = formatDelim == tagContent.length() - 1 ? null : tagContent.substring(formatDelim + 1);
				result.alignment = null;
			} else {
				result.propertyNameAndDestructuring = tagContent.substring(0, alignmentDelim);
				if (formatDelim == -1) {
					if (alignmentDelim == tagContent.length() - 1) {
						return result;
					}

					result.format = null;
					result.alignment = tagContent.substring(alignmentDelim + 1);
				} else {
					if (alignmentDelim == formatDelim - 1) {
						return result;
					}

					result.alignment = tagContent.substring(alignmentDelim + 1,
							alignmentDelim + 1 + formatDelim - alignmentDelim - 1);
					result.format = formatDelim == tagContent.length() - 1 ? null
							: tagContent.substring(formatDelim + 1);
				}
			}
		}

		result.isValid = true;
		return result;
	}

	private static Destructuring tryGetDestructuringHint(char c) {
		switch (c) {
		case '@':
			return Destructuring.Destructure;
		case '$':
			return Destructuring.Stringify;
		default:
			return Destructuring.Default;
		}
	}

	private static boolean isPunctuation(char c) {
		return c == ',' || c == '.' || c == '!' || c == '?' || c == ':' || c == ';';
	}

	private static boolean isValidInPropertyTag(char c) {
		return isValidInDestructuringHint(c) || isValidInPropertyName(c) || isValidInFormat(c) || c == ':';
	}

	private static boolean isValidInPropertyName(char c) {
		return Character.isLetterOrDigit(c) || c == '_';
	}

	private static boolean isValidInDestructuringHint(char c) {
		return c == '@' || c == '$';
	}

	private static boolean isValidInAlignment(char c) {
		return Character.isDigit(c) || c == '-';
	}

	private static boolean isValidInFormat(char c) {
		return c != '}' && (Character.isLetterOrDigit(c) || isPunctuation(c) || c == ' ' || c == '-');
	}
}
